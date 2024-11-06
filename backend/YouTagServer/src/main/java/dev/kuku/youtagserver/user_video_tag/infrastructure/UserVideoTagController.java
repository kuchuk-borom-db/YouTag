package dev.kuku.youtagserver.user_video_tag.infrastructure;

import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.shared.models.VideoTagDTO;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.user_video_tag.api.exceptions.UserVideoTagAlreadyExists;
import dev.kuku.youtagserver.user_video_tag.api.services.UserVideoTagService;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/authenticated/user_video_tag")
@RequiredArgsConstructor
public class UserVideoTagController {
    final UserVideoService userVideoService;
    final UserVideoTagService userVideoTagService;
    final VideoService videoService;
    final AuthService authService;

    @PostMapping("/")
    ResponseEntity<ResponseModel<Boolean>> AddTagToVideo(@PathParam("id") String id, @PathParam("tags") String tags) throws ResponseException {
        String email = authService.getCurrentUser().email();
        //Check if it's linked
        userVideoService.getUserVideo(email, id); //Throws exception if not linked.
        String[] tagArray = tags.split(",");
        for (String tag : tagArray) {
            try {
                userVideoTagService.addTagToVid(id, email, tag.trim().toLowerCase());
            } catch (UserVideoTagAlreadyExists e) {
                log.error(e.getMessage());
            }
        }
        return ResponseEntity.ok(new ResponseModel<>(true, ""));
    }

    @DeleteMapping("/")
    ResponseEntity<ResponseModel<Boolean>> deleteTagFromVideo(@PathParam("id") String id, @PathParam("tags") String tags) throws ResponseException {
        String email = authService.getCurrentUser().email();
        String[] tagsArray = Arrays.stream(tags.split(",")).map(s -> s.trim().toLowerCase()).toArray(String[]::new);
        userVideoTagService.deleteTagsFromVideo(id, email, tagsArray);
        return ResponseEntity.ok(new ResponseModel<>(true, "Deleted tag"));
    }

    //TODO Get by TAGS
    @GetMapping("/")
    ResponseEntity<ResponseModel<List<VideoTagDTO>>> GetVideosWithTagForUser(@PathParam("tag") String tag) throws ResponseException {
        String email = authService.getCurrentUser().email();
        var dtos = userVideoTagService.getVideosOfUserWithTag(email, tag.toLowerCase());
        List<VideoTagDTO> vids = new ArrayList<>();
        for (var v : dtos) {
            try {
                var vid = videoService.getVideo(v.videoId());
                String[] tags = userVideoTagService.getTagsOfVideo(v.videoId(), email);
                vids.add(new VideoTagDTO(vid, tags));
            } catch (VideoNotFound e) {
                //TODO: Remove from link and tags
                log.warn("Vide not found");
            }
        }
        return ResponseEntity.ok(new ResponseModel<>(vids, ""));
    }
}
