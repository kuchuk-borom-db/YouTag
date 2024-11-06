package dev.kuku.youtagserver.user_video_tag.application;

import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import dev.kuku.youtagserver.shared.models.VideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.services.UserVideoTagService;
import dev.kuku.youtagserver.video.api.services.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * handles delegated functions of userVideoTag controller
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserVideoTagCommandHandler {
    final AuthService authService;
    final UserVideoTagService userVideoTagService;
    final VideoService videoService;

    public void addTagsToVideo(String videoId, String[] tags) throws ResponseException {
        String userId = authService.getCurrentUser().email();
        //Check if the video is valid and stored in database. If not found it will throw an exception
        videoService.getVideo(videoId);
        log.info("Adding tags to video {} -> {}", videoId, tags);
        //TODO Keep track of how many were added to then fire an event
        for (String t : tags) {
            userVideoTagService.addTagToVid(userId, videoId, t);
        }
    }

    public void removeTagsFromVideo(String videoId, String[] tags) throws ResponseException {
        String userId = authService.getCurrentUser().email();
        //Check if the video is valid and stored in database. If not found it will throw an exception
        videoService.getVideo(videoId);
        log.info("Removing tags from video {} -> {}", videoId, tags);
        //TODO Keep track of how many were added to then fire an event
        for (String t : tags) {
            userVideoTagService.deleteWithUserIdAndTag(userId, t);
        }
    }

    public List<VideoTagDTO> getVideosOfUserWithTags(String[] tags) throws ResponseException {
        String userId = authService.getCurrentUser().email();
        var videos = userVideoTagService.getWithUserIdAndTag(userId, tags);
        List<VideoTagDTO> videoTagDTOS = new ArrayList<>();
        log.info("Getting videos of user {} with tags {}", userId, tags);
        for (var v : videos) {
            var info = videoService.getVideo(v.videoId());
            videoTagDTOS.add(new VideoTagDTO(info, tags));
        }
        return videoTagDTOS;
    }
}
