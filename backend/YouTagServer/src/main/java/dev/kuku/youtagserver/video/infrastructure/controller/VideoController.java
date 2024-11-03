package dev.kuku.youtagserver.video.infrastructure.controller;

import dev.kuku.youtagserver.shared.helper.UserHelper;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.user.domain.exception.InvalidEmailException;
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
    ResponseEntity<ResponseModel<Boolean>> addVideo(@PathVariable String link) throws InvalidEmailException {
        boolean success = videoServiceInternal.addVideoForUser(link, userHelper.getCurrentUserDTO().email());
        return ResponseEntity.ok(new ResponseModel<>(success, ""));
    }
}
