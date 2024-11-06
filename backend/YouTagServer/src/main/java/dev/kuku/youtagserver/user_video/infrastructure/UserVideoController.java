package dev.kuku.youtagserver.user_video.infrastructure;


import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.shared.models.VideoTagDTO;
import dev.kuku.youtagserver.user_video.application.UserVideoCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/authenticated/user_video")
@RequiredArgsConstructor
class UserVideoController {

    final UserVideoCommandHandler commandHandler;

    /**
     * Link a video to a user
     *
     * @param videoId videoID to link
     * @return true if linked successfully
     */
    @PostMapping("/")
    ResponseEntity<ResponseModel<Object>> linkVideoToUser(@RequestParam(value = "id", required = false) String videoId) throws ResponseException {
        if (videoId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseModel<>(null, "id query parameter missing"));
        }
        commandHandler.linkVideoToUser(videoId);
        return ResponseEntity.ok(new ResponseModel<>(true, String.format("Linked Video %s", videoId)));
    }

    @DeleteMapping("/")
    ResponseEntity<ResponseModel<Object>> unlinkVideoFromUser(@RequestParam(value = "id", required = false) String videoId) throws ResponseException {
        if (videoId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseModel<>(null, "id query parameter missing"));
        }
        commandHandler.unlinkVideoFromUser(videoId);
        return ResponseEntity.ok(new ResponseModel<>(true, String.format("Unlinked Video %s", videoId)));
    }

    @GetMapping("/")
    ResponseEntity<ResponseModel<List<VideoTagDTO>>> getVideosOfUser(@RequestParam(value = "video_id", required = false) String videoId) throws ResponseException {
        List<VideoTagDTO> videoTagDTOS = new ArrayList<>();
        if (videoId != null) {
            videoTagDTOS.add(commandHandler.getVideoOfUser(videoId));
        } else {
            videoTagDTOS = commandHandler.getVideosOfUser();
        }
        return ResponseEntity.ok(new ResponseModel<>(videoTagDTOS, ""));
    }


}
