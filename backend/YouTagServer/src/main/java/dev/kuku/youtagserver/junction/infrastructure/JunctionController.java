package dev.kuku.youtagserver.junction.infrastructure;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.junction.api.exceptions.JunctionDTOHasNullValues;
import dev.kuku.youtagserver.junction.application.CommandHandler;
import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.shared.models.VideoInfoTagDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authenticated/junction")
@RequiredArgsConstructor
class JunctionController {
    private final CommandHandler commandHandler;

    /**
     * 1. Add tags and videos
     * - Associates the videos with specified tags if both are present.
     * - Returns an error if only tags are present (invalid).
     * - Adds videos with a default '*' tag if only videos are provided.
     */
    @PostMapping("/")
    ResponseEntity<ResponseModel<Object>> addTagsVideos(
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "videos", required = false) String videos
    ) throws NoAuthenticatedYouTagUser, JunctionDTOHasNullValues {

        // Check if both tags and videos are missing (invalid)
        if (tags == null && videos == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseModel.build(null, "Both tags and videos query parameters can't be missing."));
        }

        // If tags are provided, check for videos
        if (tags != null && !tags.isEmpty()) {
            if (videos == null || videos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseModel.build(null, "Videos query parameter can't be missing."));
            }
            // Add videos with specified tags
            commandHandler.addVideosWithTags(videos.split(","), tags.split(","));
        } else {
            // Add videos with no tags if only videos are present
            commandHandler.addVideosWithNoTags(videos.split(","));
        }

        return ResponseEntity.ok(ResponseModel.build(null, "success"));
    }

    /**
     * 2. Delete tags and videos
     * - Removes specified tags from specified videos if both are provided.
     * - Removes all videos associated with provided tags if only tags are specified.
     * - Removes specified videos if only videos are provided.
     * - Removes all videos and tags if neither tags nor videos are specified.
     */
    @DeleteMapping("/")
    ResponseEntity<ResponseModel<Object>> deleteTagsVideos(
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "videos", required = false) String videos
    ) throws NoAuthenticatedYouTagUser, JunctionDTOHasNullValues {

        // Case 1: Both tags and videos are provided - remove specified tags from specified videos
        if (tags != null && !tags.isEmpty() && videos != null && !videos.isEmpty()) {
            commandHandler.deleteTagsFromVideos(videos.split(","), tags.split(","));
        }
        // Case 2: Only tags are provided - remove all videos associated with these tags
        else if (tags != null && videos == null) {
            commandHandler.deleteTagsFromAllVideos(tags.split(","));
        }
        // Case 3: Only videos are provided - remove specified videos for the user
        else if (tags == null && videos != null) {
            commandHandler.deleteVideosForUser(videos.split(","));
        }
        // Case 4: Neither tags nor videos are provided - remove all entries for the user
        else {
            commandHandler.deleteAllVideosAndTags();
        }

        return ResponseEntity.ok(ResponseModel.build(null, "success"));
    }

    /**
     * 3. Get tags and videos with pagination
     * - Returns all videos with tags if neither tags nor videos are specified.
     * - Returns all videos with specified tags if only tags are provided.
     * - Returns specific videos along with their tags if only videos are provided.
     * - Returns an error if both tags and videos are specified (invalid).
     */
    @GetMapping("/")
    ResponseEntity<ResponseModel<List<VideoInfoTagDTO>>> getTagsVideos(
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "videos", required = false) String videos,
            @RequestParam(value = "skip", defaultValue = "0") int skip,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) throws ResponseException {

        // Error case: Both tags and videos are present (invalid)
        if (tags != null && !tags.isEmpty() && videos != null && !videos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseModel.build(null, "Both tags and videos parameters can't be present at the same time."));
        }

        List<VideoInfoTagDTO> dtoList;
        // Case 1: No tags and no videos - return all videos with tags for the user
        if (tags == null && videos == null) {
            dtoList = commandHandler.getAllVideosOfUser(skip, limit);
        }
        // Case 2: Only tags are provided - return videos associated with these tags
        else if (tags != null && videos == null) {
            dtoList = commandHandler.getVideosWithTags(tags.split(","), skip, limit);
        }
        // Case 3: Only videos are provided - return specified videos with their tags
        else {
            dtoList = commandHandler.getVideos(videos.split(","), skip, limit);
        }

        return ResponseEntity.ok(ResponseModel.build(dtoList, "success"));
    }
}
