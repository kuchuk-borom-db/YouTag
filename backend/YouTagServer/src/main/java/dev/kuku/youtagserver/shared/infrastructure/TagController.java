package dev.kuku.youtagserver.shared.infrastructure;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.user_video.api.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.UserVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * - Add tag(s) to saved video(s) (of user)
 * - Remove tag(s) from video(s) (of user)
 * - Remove all tags from video(s) (of user)
 * - Remove tag(s) from All videos (of user)
 * - Get videos with tag(s) (of user)
 * - Get tags of video(s) (of user)
 * - Get tags containing "X" (of user) (for searching) LATER
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/authenticated/tag")
public class TagController {
    final AuthService authService;
    final UserVideoService userVideoService;
    final UserVideoTagService userVideoTagService;

    String getCurrentUserId() throws NoAuthenticatedYouTagUser {
        return authService.getCurrentUser().email();
    }

    /**
     * Determine if the videos are saved for user. If not save it first.
     * If saved then we directly add the new tags
     */
    @PostMapping("/")
    ResponseEntity<ResponseModel<Object>> addTagsToSavedVideosOfUser(
            @RequestParam(defaultValue = "", value = "tags") String tagsRaw,
            @RequestParam(value = "videos", defaultValue = "") String videosRaw
    ) throws NoAuthenticatedYouTagUser {
        log.debug("Adding tags {} to saved video {} of user {}", tagsRaw, videosRaw, getCurrentUserId());
        //Save the videos in user table
        List<String> videoIds = Arrays.stream(videosRaw.split(",")).map(String::trim).toList();
        userVideoService.saveVideosToUser(getCurrentUserId(), videoIds);
        //Save the tags in user_video_tag table
        List<String> tags = Arrays.stream(tagsRaw.split(",")).map(s -> s.trim().toLowerCase()).toList();
        userVideoTagService.saveTagsForSavedVideosOfUser(getCurrentUserId(), tags, videoIds);
        return ResponseEntity.ok(ResponseModel.build(null, String.format("Saved tags %s for videos %s of user %s", tags, videoIds, getCurrentUserId())));
    }

    @DeleteMapping("/")
    ResponseEntity<ResponseModel<Object>> removeTagsFromSavedVideosOfUser(
            @RequestParam(value = "tags", defaultValue = "") String tagsRaw,
            @RequestParam(value = "tags", defaultValue = "") String videosRaw
    ) throws NoAuthenticatedYouTagUser {
        if (tagsRaw.isEmpty() && videosRaw.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseModel.build(null, "tags and/or videos query parameters are missing"));
        }
        List<String> tags = Arrays.stream(tagsRaw.split(",")).map(s -> s.trim().toLowerCase()).toList();
        List<String> videoIds = Arrays.stream(videosRaw.split(",")).map(String::trim).toList();
        /*
        If both tags and videos are present delete tags from the video
         */
        if (!tagsRaw.isEmpty() && !videosRaw.isEmpty()) {

            userVideoTagService.deleteTagsForSavedVideosOfUser(getCurrentUserId(), tags, videoIds);
        }

        /*
        If tags are present but not videos delete the tags from all videos
         */
        if (!tagsRaw.isBlank() && videosRaw.isBlank()) {
            userVideoTagService.deleteTagsFromAllSavedVideosOfUser(getCurrentUserId(), tags);
        }

        /*
        If videos are present but not tags then delete all tags from the videos
         */
        if (tagsRaw.isEmpty() && !videosRaw.isEmpty()) {
            userVideoTagService.deleteAllTagsFromSavedVideosOfUser(getCurrentUserId(), videosRaw);
        }
        return ResponseEntity.ok(null);
    }

    @GetMapping("/")
    ResponseEntity<ResponseModel<Object>> getTagsVideosOfUser(
            String tagsRaw,
            String videosRaw,
            int skip,
            int limit
    ) throws NoAuthenticatedYouTagUser {

        List<String> tags = Arrays.stream(tagsRaw.split(",")).map(s -> s.trim().toLowerCase()).toList();
        List<String> videoIds = Arrays.stream(videosRaw.split(",")).map(String::trim).toList();

        /*
        If both tags and videos are present then we return true or false depending if the user has the tags for the saved videos.
        Useful for determining if a user has a specific set of tags for videos.
         */
        if (!tagsRaw.isEmpty() && !videosRaw.isEmpty()) {
            log.debug("Checking if tags {} exists for videos {} saved for user {}", tags, videoIds, getCurrentUserId());
            boolean exists = userVideoService.doesTagsExistForVideos(getCurrentUserId(), tags, videoIds);
            return ResponseEntity.ok(ResponseModel.build(exists, null));
        }

        /*
        If none is provided return all tags of user
         */
        if (tagsRaw.isEmpty() && videosRaw.isEmpty()) {
            log.debug("Getting all tags of user {}", getCurrentUserId());
            List<String> tags = userTagService.getAllTagsOfUser(getCurrentUserId(), skip, limit);
            return ResponseEntity.ok(ResponseModel.build(tags, null));
        }

        /*
        If tags is present and videos is missing. Return all videos with the tag
         */
        if (!tagsRaw.isEmpty() && videosRaw.isEmpty()) {
            log.debug("Getting all videos with tag {} for user {}", tags, getCurrentUserId());
            List<UserVideoTagDTO> userVideoTagDTOS = userVideoTagService.getAllVideosWithTags(getCurrentUserId(), tags, skip, limit);
            //TODO get video info tag from it.
            return ResponseEntity.ok(ResponseModel.build(userVideoTagDTOS, null));
        }

        /*
        If tags are missing and videos are present. Return all tags from the videos combined
         */
        log.debug("Getting all tags from videos {} saved for user {}", tags, getCurrentUserId());
        List<UserVideoDTO> userVideoDTOList = userVideoTagService.getAllTagsOfVideos(getCurrentUserId(), videoIds, skip, limit);
        //TODO get video info tag from it.
        return ResponseEntity.ok(ResponseModel.build(userVideoTagDTOS, null));

    }
}
