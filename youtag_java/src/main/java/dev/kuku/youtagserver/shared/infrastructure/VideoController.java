package dev.kuku.youtagserver.shared.infrastructure;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.shared.api.events.RemoveVideosOrder;
import dev.kuku.youtagserver.shared.api.events.UpdateVideoInfosOrder;
import dev.kuku.youtagserver.shared.application.OrchestratorService;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.shared.models.VideoInfoTagDTO;
import dev.kuku.youtagserver.user_video.api.UserVideoService;
import dev.kuku.youtagserver.user_video_tag.api.UserVideoTagService;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import dev.kuku.youtagserver.webscraper.api.exceptions.InvalidVideoId;
import dev.kuku.youtagserver.webscraper.api.services.YoutubeScrapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * - Save video(s) (to user)
 * - Remove video(s) (from user)
 * - Get all videos of user
 * - Get all videos containing title "X" (of user) (For searching) LATER
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/authenticated/video")
public class VideoController {
    private final VideoService videoService;
    final UserVideoService userVideoService;
    private final AuthService authService;
    private final YoutubeScrapperService scrapperService;
    private final UserVideoTagService userVideoTagService;
    private final ApplicationEventPublisher eventPublisher;
    private final OrchestratorService orchestratorService;

    private String getCurrentUser() throws NoAuthenticatedYouTagUser {
        return authService.getCurrentUser().email();
    }

    /**
     * Check if the video is already saved for user. If it's saved we return.
     * If not, determine if the video is saved in video's table
     * If it's not saved, save it after getting its info.
     * Save the video to the user in user_video table
     *
     * @param videoId id of the video to save
     * @throws NoAuthenticatedYouTagUser if no user was extracted from jwt token provided as authorization header
     * @throws InvalidVideoId            if the videoId is wrong and scrapper failed to get video info
     */
    @PostMapping("/{videoId}")
    ResponseEntity<ResponseModel<Object>> saveVideo(@PathVariable String videoId) throws NoAuthenticatedYouTagUser, InvalidVideoId {
        log.debug("Saving video {} to user {}", videoId, getCurrentUser());

        //Check if it's already saved for the user
        if (userVideoService.getSpecificSavedVideosOfUser(getCurrentUser(), List.of(videoId)).size() == 1) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseModel.build(null, String.format("Video %s already saved to user %s", videoId, getCurrentUser())));
        }

        //Check if video is already saved in videos table. If yes then update its info
        try {
            //Video exists in repository
            videoService.getVideoInfo(videoId);
            eventPublisher.publishEvent(new UpdateVideoInfosOrder(List.of(videoId)));
        } catch (VideoNotFound e) {
            //Existing video was not found in database.
            log.debug("Existing video {} not found. Adding to video table...", videoId);
            //Scrape video info and save it in video table
            var scrappedVideoInfo = scrapperService.getYoutubeVideoInfo(videoId);
            try {
                videoService.addVideo(new VideoDTO(videoId, scrappedVideoInfo.title(), scrappedVideoInfo.description(), scrappedVideoInfo.thumbnail()));
            } catch (VideoAlreadyExists ex) {
                log.error("This exception should have never been thrown. Oh well....... {}", ex.getMessage());
            }
        }

        //Save the video to the user
        userVideoService.saveVideosToUser(getCurrentUser(), List.of(videoId));
        return ResponseEntity.ok(ResponseModel.build(null, String.format("Saved video %s to user %s", videoId, getCurrentUser())));
    }

    /**
     * Delete videos by Id
     *
     * @param videosRaw list of videos separated by ,
     * @throws NoAuthenticatedYouTagUser if authenticated user is invalid
     */
    @DeleteMapping("/")
    ResponseEntity<ResponseModel<Object>> deleteVideos(@RequestParam(value = "videos", defaultValue = "") String videosRaw) throws NoAuthenticatedYouTagUser {
        if (videosRaw == null || videosRaw.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseModel.build(null, "videos query parameter is missing"));
        }
        log.debug("Deleting videos {} from user {}", videosRaw, getCurrentUser());
        Set<String> videoIds = Arrays.stream(videosRaw.trim().split(",")).collect(Collectors.toSet());
        orchestratorService.deleteSpecificSavedVideosOfUser(getCurrentUser(), videoIds);
        return ResponseEntity.ok(ResponseModel.build(null, String.format("Deleted videos from user %s", getCurrentUser())));
    }

    /**
     * Get all or specific videos saved for user.
     *
     * @param skip      how many to skip
     * @param limit     how many to limit
     * @param videosRaw list of video infos to get. Will not be retrieved if it's not saved for user
     */
    @GetMapping("/")
    ResponseEntity<ResponseModel<Set<VideoInfoTagDTO>>> getAllSavedVideosOfUser(@RequestParam(value = "skip", defaultValue = "0") int skip,
                                                                                @RequestParam(value = "limit", defaultValue = "10") int limit,
                                                                                @RequestParam(value = "videos", defaultValue = "") String videosRaw) throws NoAuthenticatedYouTagUser {
        List<String> savedVideoIdsOfUser;
        if (videosRaw == null || videosRaw.isEmpty()) {
            log.debug("Getting all saved video of user {}", getCurrentUser());
            //Get saved videos
            savedVideoIdsOfUser = userVideoService.getAllSavedVideosOfUser(getCurrentUser(), skip, limit);
        } else {
            log.debug("Getting videos {} saved for user {}", videosRaw, getCurrentUser());
            List<String> videoIds = Arrays.stream(videosRaw.split(",")).map(s -> s.trim().toLowerCase()).toList();
            savedVideoIdsOfUser = userVideoService.getSpecificSavedVideosOfUser(getCurrentUser(), videoIds);
        }

        //Get info of the videos and tags, save them in a list and return it.
        Set<String> invalidVideos = new HashSet<>();
        Set<VideoInfoTagDTO> videoInfoTagDTOS = new HashSet<>();
        savedVideoIdsOfUser.forEach(videoId -> {
            //Get info of the video
            try {
                VideoDTO videoDTO = videoService.getVideoInfo(videoId);
                Set<String> videoTags = userVideoTagService.getTagsOfSavedVideoOfUser(getCurrentUser(), videoDTO.getId());
                videoInfoTagDTOS.add(new VideoInfoTagDTO(videoDTO, videoTags));
            } catch (VideoNotFound e) {
                invalidVideos.add(videoId);
            } catch (NoAuthenticatedYouTagUser e) {
                //Can ignore. This should never happen as we already do this check at the start of the function.
            }
        });
        if (!invalidVideos.isEmpty()) {
            eventPublisher.publishEvent(new RemoveVideosOrder(invalidVideos));
        }
        return ResponseEntity.ok(ResponseModel.build(videoInfoTagDTOS, null));
    }

    //TODO Add endpoint to readme
    @GetMapping("/count")
    ResponseEntity<ResponseModel<Long>> getAllVideosCount(
            @RequestParam(value = "tags", defaultValue = "") String tagsRaw
    ) throws NoAuthenticatedYouTagUser {
        String userId = getCurrentUser();
        if (tagsRaw == null || tagsRaw.isEmpty()) {
            log.debug("Getting all videos count of user {}", userId);
            return ResponseEntity.ok(ResponseModel.build(userVideoService.getSavedVideosCountOfUser(userId), null));
        }
        log.debug("Getting videos count with tags {}", tagsRaw);
        return ResponseEntity.ok(ResponseModel.build(userVideoTagService.getCountOfSavedVideosOfUserWithTags(userId, Arrays.stream(tagsRaw.split(",")).toList()), null));
    }
}

