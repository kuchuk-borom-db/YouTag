package dev.kuku.youtagserver.video.infrastructure.controller;

import dev.kuku.youtagserver.shared.helper.UserHelper;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoAlreadyLinked;
import dev.kuku.youtagserver.video.application.VideoServiceInternal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authenticated/video")
@RequiredArgsConstructor
class VideoController {
    final VideoServiceInternal videoServiceInternal;
    final UserHelper userHelper;

    /**
     * Adds the video to database
     * Links the video to the user
     *
     * @param link link to the video
     * @return true if added successfully
     */
    @PostMapping("/{link}")
    ResponseEntity<ResponseModel<String>> addVideo(@PathVariable String link) {
        try {
            videoServiceInternal.addVideoForUser(link, userHelper.getCurrentUserDTO().email());
        } catch (UserAndVideoAlreadyLinked e) {
            return ResponseEntity.status(e.getCode()).body(new ResponseModel<>("Failed to add video", e.getMessage()));
        }
        return ResponseEntity.ok(new ResponseModel<>("Added Video", ""));
    }
}
