package dev.kuku.youtagserver.user_video.infrastructure;


import dev.kuku.youtagserver.shared.exceptions.AuthenticatedUserNotFound;
import dev.kuku.youtagserver.shared.helper.UserHelper;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user_video.api.exception.VideoAlreadyLinkedToUser;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.video.api.exceptions.InvalidVideoIDException;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authenticated/user_video")
@RequiredArgsConstructor
class UserVideoController {

    final UserHelper userHelper;
    final VideoService videoService;
    final UserVideoService userVideoService;

    @PostMapping("/")
    ResponseEntity<ResponseModel<Boolean>> linkVideoToUser(@RequestParam("id") String videoId) {
        String currentUserId;
        try {
            currentUserId = userHelper.getCurrentUserDTO().email();
        } catch (EmailNotFound | AuthenticatedUserNotFound e) {
            return ResponseEntity.status(e.getCode()).body(new ResponseModel<>(false, e.getMessage()));
        }

        //Check if video exists in db
        try {
            videoService.getVideo(videoId);
        } catch (VideoNotFound e) {
            //If video was not found it has to be added to database.
            try {
                videoService.addVideo(videoId);
            } catch (VideoAlreadyExists ex) {
                //This should never happen but you never know.
                throw new RuntimeException(ex);
            } catch (InvalidVideoIDException ex) {
                return ResponseEntity.status(ex.getCode()).body(new ResponseModel<>(false, ex.getMessage()));
            }
        }

        try {
            userVideoService.linkVideoToUser(videoId, currentUserId);
        } catch (VideoAlreadyLinkedToUser e) {
            return ResponseEntity.status(e.getCode()).body(new ResponseModel<>(false, e.getMessage()));
        }
        return ResponseEntity.ok(new ResponseModel<>(true, ""));
    }
}
