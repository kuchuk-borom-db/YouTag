package dev.kuku.youtagserver.user_video.application;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import dev.kuku.youtagserver.shared.models.VideoTagDTO;
import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.services.UserVideoLinkNotFound;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.user_video_tag.api.dto.UserVideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.services.UserVideoTagService;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * controller delegates requests to command handler
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserVideoCommandHandler {
    private final AuthService authService;
    private final UserVideoService userVideoService;
    private final VideoService videoService;
    private final UserVideoTagService userVideoTagService;

    public void linkVideoToUser(String videoId) throws ResponseException {
        String userId = authService.getCurrentUser().email();
        log.info("Linking Video {} to user {}", videoId, userId);
        userVideoService.create(userId, videoId);
    }

    public void unlinkVideoFromUser(String videoId) throws ResponseException {
        String userId = authService.getCurrentUser().email();
        log.info("Unlinking Video {} from user {}", videoId, userId);
        userVideoService.delete(userId, videoId);
    }

    public VideoTagDTO getVideoOfUser(String videoId) throws NoAuthenticatedYouTagUser, UserVideoLinkNotFound, VideoNotFound {
        String userId = authService.getCurrentUser().email();
        UserVideoDTO userVideoDTO = userVideoService.get(userId, videoId);
        VideoDTO videoDTO = videoService.getVideo(userVideoDTO.videoId());
        String[] tags = userVideoTagService.getWithUserIdAndVideoId(userId, videoId).stream().map(UserVideoTagDTO::tag).toArray(String[]::new);
        var videoTagDto = new VideoTagDTO(videoDTO, tags);
        log.info("Getting Video {} for user {}", videoTagDto, userId);
        return videoTagDto;
    }

    public List<VideoTagDTO> getVideosOfUser() throws ResponseException {
        String userId = authService.getCurrentUser().email();
        List<VideoTagDTO> videoTagDTOS = new ArrayList<>();
        for (var v : userVideoService.getWithUserId(userId)) {
            videoTagDTOS.add(getVideoOfUser(v.videoId()));
        }
        return videoTagDTOS;
    }
}
