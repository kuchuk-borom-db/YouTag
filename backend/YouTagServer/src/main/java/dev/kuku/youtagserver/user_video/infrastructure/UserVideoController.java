package dev.kuku.youtagserver.user_video.infrastructure;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.junction.api.exceptions.TagDTOHasNullValues;
import dev.kuku.youtagserver.shared.exceptions.VideoInfoTagDTOHasNullValues;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.shared.models.VideoInfoTagDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;
import dev.kuku.youtagserver.user_video.application.UserVideoCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/authenticated/user_video")
@RequiredArgsConstructor
class UserVideoController {
    final UserVideoCommandHandler commandHandler;

    /*
    1. Get videos of users with pagination support
     */
    @GetMapping("/")
    ResponseEntity<ResponseModel<List<VideoInfoTagDTO>>> getUserVideos(
            @RequestParam(value = "skip", defaultValue = "0", required = false) int skip,
            @RequestParam(value = "limit", defaultValue = "10", required = false) int limit
    ) throws NoAuthenticatedYouTagUser, VideoInfoTagDTOHasNullValues, TagDTOHasNullValues, UserDTOHasNullValues, EmailNotFound {
        log.debug("Get videos of user hit");
        List<VideoInfoTagDTO> videoInfoTagDTOS = commandHandler.getVideosOfUser(skip, limit);
        return ResponseEntity.ok(ResponseModel.build(videoInfoTagDTOS, ""));
    }
}
