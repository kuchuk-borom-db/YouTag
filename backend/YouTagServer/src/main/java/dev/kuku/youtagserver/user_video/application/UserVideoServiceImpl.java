package dev.kuku.youtagserver.user_video.application;

import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.events.AddedUserVideoEvent;
import dev.kuku.youtagserver.user_video.api.events.DeletedUserVideo;
import dev.kuku.youtagserver.user_video.api.exception.UserVideoLinkNotFound;
import dev.kuku.youtagserver.user_video.api.exception.VideoAlreadyLinkedToUser;
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
    final ApplicationEventPublisher eventPublisher;

    @Override
    public void create(String videoId, String userId) throws VideoAlreadyLinkedToUser {
        //Check if video is already linked
        try {
            get(userId, videoId);
            throw new VideoAlreadyLinkedToUser(userId, videoId);
        } catch (UserVideoLinkNotFound _) {
        }
        log.info("Creating link between user {} and video {}", userId, videoId);
        repo.save(new UserVideo(userId, videoId, LocalDateTime.now()));
        eventPublisher.publishEvent(new AddedUserVideoEvent(userId, videoId));
    }

    @Override
    public UserVideoDTO get(String userId, String videoId) throws UserVideoLinkNotFound {
        var video = repo.findByUserIdAndVideoId(userId, videoId);
        if (video != null) {
            return toDto(video);
        }
        throw new UserVideoLinkNotFound(userId, videoId);
    }


    @Override
    public List<UserVideoDTO> getWithUserId(String userId) {
        log.info("Getting User Video with userID {}", userId);
        var userVideos = repo.findAllByUserId(userId);
        return userVideos.stream().map(this::toDto).toList();
    }

    @Override
    public void delete(String userId, String videoId) {
        log.info("Deleting User Video with userID {} and video {}", userId, videoId);
        var deleted = repo.deleteByUserIdAndVideoId(userId, videoId);
        eventPublisher.publishEvent(new DeletedUserVideo(List.of(toDto(deleted))));
    }

    @Override
    public void deleteAll(String userId) {
        log.info("Deleting all UserVideo records where user is {}", userId);
        var deleted = repo.deleteByUserId(userId);
        var deletedDto = deleted.stream().map(this::toDto).toList();
        eventPublisher.publishEvent(new DeletedUserVideo(deletedDto));
    }

    private UserVideoDTO toDto(UserVideo video) {
        return new UserVideoDTO(video.getUserId(), video.getVideoId());
    }
}
