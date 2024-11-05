package dev.kuku.youtagserver.user_video_tag.infrastructure;

import dev.kuku.youtagserver.shared.exceptions.AuthenticatedUserNotFound;
import dev.kuku.youtagserver.shared.helper.UserHelper;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.user_video_tag.api.exceptions.UserVideoTagAlreadyExists;
import dev.kuku.youtagserver.user_video_tag.api.services.UserVideoTagService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/authenticated/user_video_tag")
@RequiredArgsConstructor
public class UserVideoTagController {
    final UserHelper userHelper;
    final UserVideoService userVideoService;
    final UserVideoTagService userVideoTagService;

    @PostMapping("/")
    ResponseEntity<ResponseModel<Boolean>> AddTagToVideo(@PathParam("id") String id, @PathParam("tags") String tags) {
        String email;
        try {
            email = userHelper.getCurrentUserDTO().email();
        } catch (EmailNotFound | AuthenticatedUserNotFound e) {
            return ResponseEntity.status(e.getCode()).body(new ResponseModel<>(false, e.getMessage()));
        }
        //Check if it's linked
        if (!userVideoService.isVideoLinkedToUser(email, id)) {
            return ResponseEntity.status(403).body(new ResponseModel<>(false, String.format("Video %s is not linked to user %s", id, email)));
        }
        String[] tagArray = tags.split(",");
        for (String tag : tagArray) {
            try {
                userVideoTagService.addTagToVid(id, email, tag.trim());
            } catch (UserVideoTagAlreadyExists e) {
                log.error(e.getMessage());
            }
        }
        return ResponseEntity.ok(new ResponseModel<>(true, ""));
    }
}
