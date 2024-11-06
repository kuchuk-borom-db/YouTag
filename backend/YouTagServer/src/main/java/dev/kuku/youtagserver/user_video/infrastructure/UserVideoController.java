package dev.kuku.youtagserver.user_video.infrastructure;


import dev.kuku.youtagserver.shared.exceptions.AuthenticatedUserNotFound;
import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import dev.kuku.youtagserver.shared.helper.UserHelper;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.shared.models.VideoTagDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.exception.VideoAlreadyLinkedToUser;
import dev.kuku.youtagserver.user_video.api.services.UserVideoLinkNotFound;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.user_video_tag.api.services.UserVideoTagService;
import dev.kuku.youtagserver.video.api.exceptions.InvalidVideoIDException;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
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

    final UserHelper userHelper;
    final VideoService videoService;
    final UserVideoService userVideoService;
    final UserVideoTagService userVideoTagService;

    /**
     * Link a video to a user
     *
     * @param videoId videoID to link
     * @return true if linked successfully
     */
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

    @DeleteMapping("/")
    ResponseEntity<ResponseModel<Boolean>> unlinkVideoFromUser(@RequestParam("id") String videoId) throws ResponseException {
        String currentUserId = userHelper.getCurrentUserDTO().email();
        userVideoService.unlinkVideoFromUser(videoId, currentUserId);
        return ResponseEntity.ok(new ResponseModel<>(true, ""));
        //TODO Make sure the video is removed from videos db if no user has it linked
    }

    /**
     * Get videos linked to the user
     *
     * @param id optional param. If provided will only return video info of that specific video
     * @return list of videos
     */
    @GetMapping("/")
    ResponseEntity<ResponseModel<List<VideoTagDTO>>> getVideosLinkedToUser(@RequestParam(required = false) String id) throws UserVideoLinkNotFound {
        String currentUserId;
        try {
            currentUserId = userHelper.getCurrentUserDTO().email();
        } catch (EmailNotFound | AuthenticatedUserNotFound e) {
            return ResponseEntity.status(e.getCode()).body(new ResponseModel<>(List.of(), e.getMessage()));
        }
        List<UserVideoDTO> userVidDtos;
        if (id == null)
            userVidDtos = userVideoService.getUserVideosOfUser(currentUserId);
        else {
            userVidDtos = List.of(userVideoService.getUserVideo(currentUserId, id));
        }
        List<VideoTagDTO> videoDTOS = new ArrayList<>();
        for (var dto : userVidDtos) {
            try {
                var vid = videoService.getVideo(dto.videoId());
                String[] tags = userVideoTagService.getTagsOfVideo(vid.id(), currentUserId);
                videoDTOS.add(new VideoTagDTO(vid, tags));
            } catch (VideoNotFound e) {
                //TODO Remove link and tags
                log.warn("Video not found in database.");
            }

        }
        return ResponseEntity.ok(new ResponseModel<>(videoDTOS, ""));
    }
}
