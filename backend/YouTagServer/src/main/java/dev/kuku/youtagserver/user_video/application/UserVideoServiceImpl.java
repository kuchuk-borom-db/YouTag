package dev.kuku.youtagserver.user_video.application;

import dev.kuku.youtagserver.shared.helper.CacheSystem;
import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.events.VideoUnlinkedEvent;
import dev.kuku.youtagserver.user_video.api.exception.VideoAlreadyLinkedToUser;
import dev.kuku.youtagserver.user_video.api.services.UserVideoLinkNotFound;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.user_video.domain.entity.UserVideo;
import dev.kuku.youtagserver.user_video.infrastructure.UserVideoRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserVideoServiceImpl implements UserVideoService {
    final UserVideoRepo repo;
    final CacheSystem cacheSystem;
    final ApplicationEventPublisher eventPublisher;

    @Override
    public void linkVideoToUser(String videoId, String userId) throws VideoAlreadyLinkedToUser {
        log.info("Linking Video {} to user {}", videoId, userId);
        if (isVideoLinkedToUser(videoId, userId)) {
            throw new VideoAlreadyLinkedToUser(userId, videoId);
        }
        repo.save(new UserVideo(userId, videoId, LocalDateTime.now()));
    }

    @Override
    public void unlinkVideoFromUser(String videoId, String userId) throws UserVideoLinkNotFound {
        log.info("Unlinking Video {} from user {}", videoId, userId);
        if (!isVideoLinkedToUser(userId, videoId)) {
            throw new UserVideoLinkNotFound(videoId, userId);
        }
        repo.deleteByUserIdAndVideoId(userId, videoId);
        eventPublisher.publishEvent(new VideoUnlinkedEvent(userId, videoId));
    }

    @Override
    public UserVideoDTO getUserVideo(String userId, String videoId) throws UserVideoLinkNotFound {
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
    public List<UserVideoDTO> getUserVideosOfUser(String userId) {
        log.info("Getting videos of user {}", userId);
        return repo.findByUserId(userId).stream().map(this::toDTO).toList();
    }

    @Override
    public boolean isVideoLinkedToUser(String email, String id) {
        try {
            getUserVideo(email, id);
            return true;
        } catch (UserVideoLinkNotFound e) {
            return false;
        }
    }

    @Override
    public List<UserVideoDTO> getUserVideosContainingVideoId(String videoId) {
        log.info("Getting user videos which contains video {}", videoId);
        return repo.findAllByVideoId(videoId);
    }

    public UserVideoDTO toDTO(UserVideo userVideo) {
        return new UserVideoDTO(userVideo.getUserId(), userVideo.getVideoId(), userVideo.getCreated());
    }
}
