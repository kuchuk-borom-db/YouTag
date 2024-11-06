package dev.kuku.youtagserver.user_video_tag.infrastructure;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.shared.models.VideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.application.UserVideoTagCommandHandler;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/authenticated/user_video_tag")
@RequiredArgsConstructor
public class UserVideoTagController {
    final UserVideoTagCommandHandler commandHandler;

    @PostMapping("/")
    ResponseEntity<ResponseModel<Boolean>> AddTagsToVideo(@PathParam("id") String id, @PathParam("tags") String tags) throws ResponseException {
        commandHandler.addTagsToVideo(id, Arrays.stream(tags.split(",")).map(s -> s.trim().toLowerCase()).toArray(String[]::new));
        return ResponseEntity.ok(new ResponseModel<>(true, "added tags"));
    }

    @DeleteMapping("/")
    ResponseEntity<ResponseModel<Boolean>> removeTagsFromVideo(@PathParam("id") String id, @PathParam("tags") String tags) throws ResponseException {
        commandHandler.removeTagsFromVideo(id, Arrays.stream(tags.split(",")).map(s -> s.trim().toLowerCase()).toArray(String[]::new));
        return ResponseEntity.ok(new ResponseModel<>(true, "Removed tags from video"));
    }

    @GetMapping("/")
    ResponseEntity<ResponseModel<List<VideoTagDTO>>> getVideosWithTags(@PathParam("tags") String tags) throws ResponseException {
        String[] tagsArray = Arrays.stream(tags.split(",")).map(s -> s.trim().toLowerCase()).toArray(String[]::new);
        List<VideoTagDTO> videoTagDTOS = commandHandler.getVideosOfUserWithTags(tagsArray);
        return ResponseEntity.ok(new ResponseModel<>(videoTagDTOS, ""));
    }

}
