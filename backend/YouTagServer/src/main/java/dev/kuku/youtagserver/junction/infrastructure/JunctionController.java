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
     * 1. Add tags and associate them with videos
     * - Associates specified tags with provided videos if both are present.
     * - If only tags are provided without videos, returns an error as this is considered invalid.
     * - If only videos are provided without tags, assigns a default '*' tag to each video and removes any pre-existing tags.
     */
    @PostMapping("/")
    ResponseEntity<ResponseModel<Object>> addTagsVideos(
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "videos", required = false) String videos
    ) throws NoAuthenticatedYouTagUser, JunctionDTOHasNullValues {

        // Ensure that at least one of tags or videos is provided
        if (tags == null && videos == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseModel.build(null, "Both tags and videos query parameters can't be missing."));
        }

        // If tags are provided, videos must also be provided
        if (tags != null && !tags.isEmpty()) {
            if (videos == null || videos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseModel.build(null, "videos query parameter can't be missing."));
            }
            // Associate provided tags with the videos
            commandHandler.addVideosWithTags(videos.split(","), tags.split(","));
        } else {
            // If only videos are provided, add each video with a default '*' tag
            // Remove any existing tags from these videos before adding the new association
            commandHandler.deleteVideosForUser(videos.split(","));
            commandHandler.addVideosWithNoTags(videos.split(","));
        }

        return ResponseEntity.ok(ResponseModel.build(null, "success"));
    }

    /**
     * 2. Delete tags and associated videos
     * - If both tags and videos are specified, removes only the specified tags from the specified videos.
     * - If only tags are provided, removes all videos associated with those tags.
     * - If only videos are provided, removes those videos entirely.
     * - If neither tags nor videos are provided, removes all tags and videos for the user.
     */
    @DeleteMapping("/")
    ResponseEntity<ResponseModel<Object>> deleteTagsVideos(
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "videos", required = false) String videos
    ) throws NoAuthenticatedYouTagUser, JunctionDTOHasNullValues {

        // Case 1: Both tags and videos are provided - remove specified tags from the specified videos
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
     * 3. Retrieve videos and their associated tags with pagination support
     * - If neither tags nor videos are specified, returns all videos with their tags for the user.
     * - If only tags are provided, returns all videos associated with the specified tags.
     * - If only videos are provided, returns specified videos with their associated tags.
     * - Returns an error if both tags and videos are provided simultaneously as this is considered invalid.
     */
    @GetMapping("/")
    ResponseEntity<ResponseModel<List<VideoInfoTagDTO>>> getTagsVideos(
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "videos", required = false) String videos,
            @RequestParam(value = "skip", defaultValue = "0") int skip,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) throws ResponseException {

        // Error case: Both tags and videos are provided, which is invalid
        if (tags != null && !tags.isEmpty() && videos != null && !videos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseModel.build(null, "Both tags and videos parameters can't be present at the same time."));
        }

        List<VideoInfoTagDTO> dtoList;
        // Case 1: No tags or videos provided - retrieve all videos with tags for the user
        if (tags == null && videos == null) {
            dtoList = commandHandler.getAllVideosOfUser(skip, limit);
        }
        // Case 2: Only tags are provided - retrieve videos associated with these tags
        else if (tags != null && videos == null) {
            dtoList = commandHandler.getVideosWithTags(tags.split(","), skip, limit);
        }
        // Case 3: Only videos are provided - retrieve specified videos along with their tags
        else {
            dtoList = commandHandler.getVideos(videos.split(","), skip, limit);
        }

        return ResponseEntity.ok(ResponseModel.build(dtoList, "success"));
    }
}
