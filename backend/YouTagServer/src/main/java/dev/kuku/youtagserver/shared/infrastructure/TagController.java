package dev.kuku.youtagserver.shared.infrastructure;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.shared.application.OrchestratorService;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.user_tag.api.UserTagService;
import dev.kuku.youtagserver.user_video.api.UserVideoService;
import dev.kuku.youtagserver.user_video_tag.api.UserVideoTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    final UserTagService userTagService;
    final OrchestratorService orchestratorService;

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
        List<String> videoIds = Arrays.stream(videosRaw.split(",")).map(String::trim).toList();
        List<String> tags = Arrays.stream(tagsRaw.split(",")).map(s -> s.trim().toLowerCase()).toList();

        //Check if the video is saved for user
        videoIds = userVideoService.getSpecificSavedVideosOfUser(getCurrentUserId(), videoIds);
        if (videoIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ResponseModel.build(null, "videos provided in query parameters are not saved to user"));
        }

        //Save the tags in user_tag table if it doesn't exist yet.
        userTagService.addTagsToUser(getCurrentUserId(), tags);
        //Save the tagIds to userVideoTag table
        userVideoTagService.addTagsToSpecificSavedVideosOfUser(getCurrentUserId(), videoIds, tags);
        return ResponseEntity.ok(ResponseModel.build(null, String.format("Saved tags %s for videos %s of user %s", tags, videoIds, getCurrentUserId())));
    }

    @DeleteMapping("/")
    ResponseEntity<ResponseModel<Object>> removeTagsFromSavedVideosOfUser(
            @RequestParam(value = "tags", defaultValue = "") String tagsRaw,
            @RequestParam(value = "videos", defaultValue = "") String videosRaw
    ) throws NoAuthenticatedYouTagUser {

        /*
        If both are null it's invalid
         */
        if (tagsRaw.isBlank() && videosRaw.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseModel.build(null, "tags and/or videos query parameters are missing"));
        }
        Set<String> tags = Arrays.stream(tagsRaw.trim().split(",")).map(s -> s.trim().toLowerCase()).collect(Collectors.toSet());
        Set<String> videoIds = Arrays.stream(videosRaw.trim().split(",")).map(String::trim).collect(Collectors.toSet());

        String msg = "";
        /*
        If both tags and videos are present delete tags from the video
         */
        if (!tagsRaw.isBlank() && !videosRaw.isBlank()) {
            orchestratorService.deleteSpecificTagsFromSpecificSavedVideosOfUser(getCurrentUserId(), videoIds, tags);
            msg = String.format("Deleted tags %s from videos %s", tags, videoIds);
        }

        /*
        If tags are present but not videos delete the tags from all videos
         */
        if (!tagsRaw.isBlank() && videosRaw.isBlank()) {
            orchestratorService.deleteSpecificTagsFromAllSavedVideosOfUser(getCurrentUserId(), tags);
            msg = String.format("Deleted tags %s from all videos", tags);
        }

        /*
        If videos are present but not tags then delete all tags from the videos
         */
        if (tagsRaw.isBlank() && !videosRaw.isBlank()) {
            orchestratorService.deleteAllTagsFromSpecificSavedVideosOfUser(getCurrentUserId(), videoIds);
            msg = String.format("Deleted all tags from videos %s", videoIds);
        }
        return ResponseEntity.ok(ResponseModel.build(null, msg));
    }

    @GetMapping("/")
    ResponseEntity<ResponseModel<Object>> getTagsVideosOfUser(
            @RequestParam(value = "tags", defaultValue = "") String tagsRaw,
            @RequestParam(value = "videos", defaultValue = "") String videosRaw,
            @RequestParam(value = "skip", defaultValue = "0") int skip,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) throws NoAuthenticatedYouTagUser {
        List<String> tags = Arrays.stream(tagsRaw.split(",")).map(s -> s.trim().toLowerCase()).toList();
        List<String> videoIds = Arrays.stream(videosRaw.split(",")).map(String::trim).toList();

        /*
        If both tags and videos are present then its invalid.
         */
        if (!tagsRaw.isEmpty() && !videosRaw.isEmpty()) {
            log.debug("Checking if tags {} exists for videos {} saved for user {}", tags, videoIds, getCurrentUserId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseModel.build(null, "Both tags and videos query parameter can't be passed simultaneously"));
        }

        /*
        If none is provided return all tags of user
         */
        if (tagsRaw.isEmpty() && videosRaw.isEmpty()) {
            log.debug("Getting all tags of user {}", getCurrentUserId());
            List<String> tagsOfUser = userTagService.getAllTagsOfUser(getCurrentUserId(), skip, limit);
            return ResponseEntity.ok(ResponseModel.build(tagsOfUser, null));
        }

        /*
        If tags is present and videos is missing. Return all videos with the tag
         */
        if (!tagsRaw.isEmpty() && videosRaw.isEmpty()) {
            log.debug("Getting all videos with tag {} for user {}", tags, getCurrentUserId());
            Set<String> videoIdsWithTags = userVideoTagService.getAllSavedVideosOfUserWithTags(getCurrentUserId(), tags, skip, limit);
            return ResponseEntity.ok(ResponseModel.build(videoIdsWithTags, null));
        }

        /*
        If tags are missing and videos are present. Return all tags from the videos combined
         */
        log.debug("Getting all tags from videos {} saved for user {}", tags, getCurrentUserId());
        Set<String> tagsOfVideos = userVideoTagService.getAllTagsOfSavedVideosOfUser(getCurrentUserId(), videoIds, skip, limit);
        return ResponseEntity.ok(ResponseModel.build(tagsOfVideos, null));
    }

    @GetMapping("/count")
    ResponseEntity<ResponseModel<Long>> tagCountOfUser() throws NoAuthenticatedYouTagUser {
        String userId = getCurrentUserId();
        log.debug("Getting tags count of user {}", userId);
        long tagCount = userTagService.getTagCountOfUser(userId);
        return ResponseEntity.ok(ResponseModel.build(tagCount, null));
        //TODO Add endpoint to README
    }
}
