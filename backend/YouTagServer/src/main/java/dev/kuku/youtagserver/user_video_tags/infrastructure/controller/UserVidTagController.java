package dev.kuku.youtagserver.user_video_tags.infrastructure.controller;

import dev.kuku.youtagserver.shared.helper.UserHelper;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.services.UserService;
import dev.kuku.youtagserver.user_video_tags.api.dto.UserVidTagDto;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoLinkNotFound;
import dev.kuku.youtagserver.user_video_tags.api.services.UserVidTagService;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.InvalidVideoIDException;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/authenticated/user-vid-tag")
@RequiredArgsConstructor
class UserVidTagController {
    final UserVidTagService userVidTagService;
    final UserHelper userHelper;
    final VideoService videoService;
    final UserService userService;

    /**
     * Link video and user
     *
     * @param id videoID
     * @return true if successful
     */
    @PostMapping("/link/{id}")
    ResponseEntity<ResponseModel<Boolean>> linkVidToUser(@PathVariable String id) {
        //Get user data from security context and validate it
        String userID = userHelper.getCurrentUserDTO().email();
        try {
            userService.getUser(userID);
        } catch (EmailNotFound e) {
            return ResponseEntity.status(e.getCode()).body(new ResponseModel<>(false, e.getMessage()));
        }

        //Check if video is in database
        VideoDTO videoDTO;
        try {
            videoDTO = videoService.getVideo(id);
        } catch (VideoNotFound e) {
            //If video doesn't exist. Create new one
            log.warn("Video not found. Adding one to database : {}", id);
            try {
                videoDTO = videoService.addVideo(id);
            } catch (InvalidVideoIDException f) {
                return ResponseEntity.status(e.getCode()).body(new ResponseModel<>(false, e.getMessage()));
            } catch (VideoAlreadyExists f) {
                throw new RuntimeException(f);
            }
        }
        try {
            userVidTagService.linkUserAndVideo(userID, videoDTO.id());
        } catch (UserAndVideoAlreadyLinked e) {
            return ResponseEntity.status(e.getCode()).body(new ResponseModel<>(false, e.getMessage()));
        }
        return ResponseEntity.ok(new ResponseModel<>(true, ""));
    }

    /**
     * add tags to video for user that is logged in(through token)
     *
     * @param tags tags to add
     * @param id   video id
     * @return true if successful
     */
    @PostMapping("/tag")
    ResponseEntity<ResponseModel<Boolean>> addTags(@RequestParam String tags, @RequestParam String id) {
        //Get user data from security context and validate it
        String userID = userHelper.getCurrentUserDTO().email();
        try {
            userService.getUser(userID);
        } catch (EmailNotFound e) {
            return ResponseEntity.status(404).body(new ResponseModel<>(false, e.getMessage()));
        }
        String[] tagsArray = tags.split(",");
        try {
            userVidTagService.addTags(userID, id, tagsArray);
        } catch (UserAndVideoLinkNotFound e) {
            return ResponseEntity.status(e.getCode()).body(new ResponseModel<>(false, e.getMessage()));
        }
        return ResponseEntity.ok(new ResponseModel<>(true, ""));
    }

    @GetMapping("/link")
    ResponseEntity<ResponseModel<List<UserVidTagDto>>> getVideo(@RequestParam(required = false) String tags, @RequestParam(required = false, defaultValue = "0") int skip, @RequestParam(required = false, defaultValue = "100") int limit) {
        String userID = userHelper.getCurrentUserDTO().email();
        try {
            userService.getUser(userID);
        } catch (EmailNotFound e) {
            return ResponseEntity.status(e.getCode()).body(new ResponseModel<>(List.of(), e.getMessage()));
        }
        if (tags == null || tags.isEmpty()) {
            var dtos = userVidTagService.getUserAndVideoTagsByUser(userID);
            return ResponseEntity.ok(new ResponseModel<>(dtos, ""));
        } else {
            var dtos = userVidTagService.getUserAndVideoTagsByUserAndTag(userID, tags);
            return ResponseEntity.ok(new ResponseModel<>(dtos, ""));
        }
    }


}
