package dev.kuku.youtagserver.user_video.infrastructure;


import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.shared.models.VideoTagDTO;
import dev.kuku.youtagserver.user.api.services.UserService;
import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.user_video_tag.api.services.UserVideoTagService;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/authenticated/user_video")
@RequiredArgsConstructor
class UserVideoController {

    final VideoService videoService;
    final UserVideoService userVideoService;
    final UserVideoTagService userVideoTagService;
    final AuthService authService;
    final UserService userService;

    private String getCurrentUserId() throws ResponseException {
        //Get current user
        var userId = authService.getCurrentUser().email();
        //Check if current user is present in db
        userService.getUser(userId);
        return userId;
    }

    /**
     * Link a video to a user
     *
     * @param videoId videoID to link
     * @return true if linked successfully
     */
    @PostMapping("/")
    ResponseEntity<ResponseModel<Boolean>> linkVideoToUser(@RequestParam("id") String videoId) throws ResponseException {
        String userId = getCurrentUserId();
        //Check if video exists in db
        try {
            videoService.getVideo(videoId);
        } catch (VideoNotFound e) {
            //If video was not found it has to be added to database.
            videoService.addVideo(videoId);
        }
        userVideoService.linkVideoToUser(videoId, userId);
        return ResponseEntity.ok(new ResponseModel<>(true, ""));
    }

    /**
     * Remove Link between a video and a user. <br>
     * Video will be removed from video table if no user has the video linked
     *
     * @param videoId ID of the video to unlink
     * @return true if removed successfully
     */
    @DeleteMapping("/")
    ResponseEntity<ResponseModel<Boolean>> unlinkVideoFromUser(@RequestParam("id") String videoId) throws ResponseException {
        String userId = getCurrentUserId();
        userVideoService.unlinkVideoFromUser(videoId, userId);
        return ResponseEntity.ok(new ResponseModel<>(true, ""));
    }

    /**
     * Get video(s) linked to the user
     *
     * @param id optional param. If provided will only return video info of that specific video
     * @return list of videos
     */
    @GetMapping("/")
    ResponseEntity<ResponseModel<List<VideoTagDTO>>> getVideosLinkedToUser(@RequestParam(required = false) String id) throws ResponseException {
        String userId = getCurrentUserId();
        List<UserVideoDTO> userVidDtos;
        if (id == null)
            userVidDtos = userVideoService.getUserVideosOfUser(userId);
        else {
            userVidDtos = List.of(userVideoService.getUserVideo(userId, id));
        }
        List<VideoTagDTO> videoDTOS = new ArrayList<>();
        for (var dto : userVidDtos) {
            try {
                var vid = videoService.getVideo(dto.videoId());
                String[] tags = userVideoTagService.getTagsOfVideo(vid.id(), userId);
                videoDTOS.add(new VideoTagDTO(vid, tags));
            } catch (VideoNotFound e) {
                //TODO Remove link and tags
                log.warn("Video not found in database.");
            }
        }
        return ResponseEntity.ok(new ResponseModel<>(videoDTOS, ""));
    }
}
