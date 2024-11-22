package dev.kuku.youtagserver.shared.infrastructure;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.shared.api.events.UpdateVideoInfoOrder;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.shared.models.VideoInfoTagDTO;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.user_video_tag.api.dtos.UserVideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.services.UserVideoTagService;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        if (userVideoService.getSpecificSavedVideosOfUser(getCurrentUser(), List.of(videoId)).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseModel.build(null, String.format("Video %s already saved to user %s", videoId, getCurrentUser())));
        }

        //Check if video is already saved in videos table with updated values or doesn't exist yet.
        try {
            //Video exists in repository
            var repoDb = videoService.getVideoInfo(videoId);
            //Compare latest scrapped data and data from repository
            var scrappedVideoInfo = scrapperService.getYoutubeVideoInfo(videoId);
            if (repoDb.getDescription().equals(scrappedVideoInfo.description()) && repoDb.getTitle().equals(scrappedVideoInfo.title())) {
                log.debug("Existing video {} found and doesn't require updating {}", repoDb, scrappedVideoInfo);
            } else {
                //repo data is outdated and needs updating.
                log.debug("Existing video found but requires updating. Updating....");
                eventPublisher.publishEvent(new UpdateVideoInfoOrder(new VideoDTO(videoId, scrappedVideoInfo.title(), scrappedVideoInfo.description(), scrappedVideoInfo.thumbnail())));
            }
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
        userVideoService.saveVideoToUser(getCurrentUser(), videoId);

        return ResponseEntity.ok(ResponseModel.build(null, String.format("Saved video %s to user %s", videoId, getCurrentUser())));
    }

    /**
     * @param videosRaw list of videos separated by ,
     * @throws NoAuthenticatedYouTagUser if authenticated user is invalid
     */
    @DeleteMapping("/")
    ResponseEntity<ResponseModel<Object>> deleteVideos(@RequestParam(value = "videos", defaultValue = "") String videosRaw) throws NoAuthenticatedYouTagUser {
        if (videosRaw == null || videosRaw.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseModel.build(null, "videos query parameter is missing"));
        }
        log.debug("Deleting videos {} from user {}", videosRaw, getCurrentUser());
        List<String> videoIds = Arrays.stream(videosRaw.split(",")).map(s -> s.trim().toLowerCase()).toList();
        userVideoService.removeSavedVideosFromUser(getCurrentUser(), videoIds);
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
    ResponseEntity<ResponseModel<List<VideoInfoTagDTO>>> getAllSavedVideosOfUser(@RequestParam(value = "skip", defaultValue = "0") int skip, @RequestParam(value = "limit", defaultValue = "10") int limit, @RequestParam(value = "videos", defaultValue = "") String videosRaw) throws NoAuthenticatedYouTagUser {
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
        List<VideoInfoTagDTO> videoInfoTagDTOS = new ArrayList<>();
        savedVideoIdsOfUser.forEach(videoId -> {
            //Get info of the video
            try {
                VideoDTO videoDTO = videoService.getVideoInfo(videoId);
                List<UserVideoTagDTO> videoTags = userVideoTagService.getTagsOfSavedVideoOfUser(getCurrentUser(), videoDTO.getId());
                videoInfoTagDTOS.add(new VideoInfoTagDTO(videoDTO, videoTags));
            } catch (VideoNotFound _) {
                //TODO Store in a local list so that the saved video can be removed from user by event publishing
            } catch (NoAuthenticatedYouTagUser _) {
                //Can ignore. This should never happen as we already do this check at the start of the function.
            }
        });
        return ResponseEntity.ok(ResponseModel.build(videoInfoTagDTOS, null));
    }

}

