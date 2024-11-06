package dev.kuku.youtagserver.user_video_tag.infrastructure;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.shared.models.VideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.application.UserVideoTagCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/authenticated/user_video_tag")
@RequiredArgsConstructor
class UserVideoTagController {
    final UserVideoTagCommandHandler commandHandler;

    /**
     * Add tags to video
     */
    @PostMapping("/")
    ResponseEntity<Object> addTagsToVideoOfUser(@RequestParam("tags") String tags, @RequestParam("video_id") String videoId) throws ResponseException {
        commandHandler.addTagsToVideo(videoId, tags.split(","));
        return ResponseEntity.ok(null);
    }

    /**
     * Delete tags from all or one video
     */
    @DeleteMapping("/")
    ResponseEntity<Object> removeTagsFromOneOrMoreVideoOfUser(@RequestParam("tags") String tags, @RequestParam(value = "video_id", required = false) String videoId) throws ResponseException {
        if (videoId == null) {
            commandHandler.removeTagsFromAllVideosOfUser(tags.split(","));
        } else {
            commandHandler.removeTagsFromVideo(videoId, tags.split(","));
        }
        return ResponseEntity.ok(null);
    }

    /**
     * get video with tags or get all videos
     */
    @GetMapping("/")
    ResponseEntity<ResponseModel<List<VideoTagDTO>>> getVideosWithTags(@RequestParam(value = "tags", required = false) String tags) throws ResponseException {
        if (tags != null && !tags.isEmpty() && tags.split(",").length > 0) {
            var vids = commandHandler.getVideosOfUserWithTags(tags.split(","));
            return ResponseEntity.ok(new ResponseModel<>(vids, ""));
        }
        return ResponseEntity.ok(new ResponseModel<>(commandHandler.getVideosOfUser(), ""));
    }

    /**
     * get tags created by user
     */
    @GetMapping("/tags")
    ResponseEntity<ResponseModel<List<String>>> getTags() throws ResponseException {
        return ResponseEntity.ok(new ResponseModel<>(commandHandler.getTagsOfUser(), ""));
    }
}
