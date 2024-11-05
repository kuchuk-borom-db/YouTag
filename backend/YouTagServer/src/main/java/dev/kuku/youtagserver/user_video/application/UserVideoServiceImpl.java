package dev.kuku.youtagserver.user_video.application;

import dev.kuku.youtagserver.shared.helper.CacheSystem;
import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.exception.VideoAlreadyLinkedToUser;
import dev.kuku.youtagserver.user_video.api.services.UserVideoLinkNotFound;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.user_video.domain.entity.UserVideo;
import dev.kuku.youtagserver.user_video.infrastructure.UserVideoRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserVideoServiceImpl implements UserVideoService {
    final UserVideoRepo repo;
    final CacheSystem cacheSystem;

    @Override
    public void linkVideoToUser(String videoId, String userId) throws VideoAlreadyLinkedToUser {
        log.info("Linking Video {} to user {}", videoId, userId);
        if (isVideoLinkedToUser(videoId, userId)) {
            throw new VideoAlreadyLinkedToUser(userId, videoId);
        }
        repo.save(new UserVideo(userId, videoId, LocalDateTime.now()));
    }

    @Override
    public UserVideoDTO getUserVideoByUserIdAndVideo(String userId, String videoId) throws UserVideoLinkNotFound {
        UserVideo userVideo = (UserVideo) cacheSystem.getObject(this.getClass().toString(), userId);
        if (userVideo == null) {
            userVideo = repo.findByUserIdAndVideoId(userId, videoId);
            cacheSystem.cache(this.getClass().toString(), String.format("%s%s", userId, videoId), userVideo);
        }
        if (userVideo == null) {
            throw new UserVideoLinkNotFound(userId, videoId);
        }
        return toDTO(userVideo);
    }

    @Override
    public boolean isVideoLinkedToUser(String email, String id) {
        try {
            getUserVideoByUserIdAndVideo(email, id);
            return true;
        } catch (UserVideoLinkNotFound e) {
            return false;
        }
    }

    private UserVideoDTO toDTO(UserVideo userVideo) {
        return new UserVideoDTO(userVideo.getUserId(), userVideo.getVideoId(), userVideo.getCreated());
    }
}
