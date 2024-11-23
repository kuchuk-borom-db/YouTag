package dev.kuku.youtagserver.user_video.application;

import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.events.RemoveSavedVideosFromUser;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.user_video.domain.UserVideo;
import dev.kuku.youtagserver.user_video.infrastructure.UserVideoRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserVideoServiceImpl implements UserVideoService {
    final UserVideoRepo repo;
    final ApplicationEventPublisher eventPublisher;

    @Override
    public UserVideoDTO toDto(UserVideo e) {
        return new UserVideoDTO(e.getUserId(), e.getVideoId());
    }


    @Override
    public void saveVideoToUser(String userId, List<String> videoIds) {
        log.debug("Save video {} to user {}", videoIds, userId);
        repo.saveAll(videoIds.stream().map(vid -> new UserVideo(userId, vid)).toList());
        //TODO Cache evict
    }

    @Override
    public void removeSavedVideosFromUser(String userId, List<String> videoIds) {
        log.debug("Remove saved video {} from user {}", videoIds, userId);
        repo.deleteAllByUserIdAndVideoIdIn(userId, videoIds);
        //TODO cache evict
        //Remove entry or entries from user_video_tag table with userId and videoIds
        eventPublisher.publishEvent(new RemoveSavedVideosFromUser(userId, videoIds));
    }

    @Override
    public List<String> getAllSavedVideosOfUser(String userId, int skip, int limit) {
        log.debug("Get all saved videos of user {}, skip {} and limit {}", userId, skip, limit);
        List<UserVideo> userVideos = repo.findAllByUserId(userId, PageRequest.of(skip / limit, limit));
        log.debug("Got saved videos {} for user {}", userVideos, userId);
        return userVideos.stream().map(UserVideo::getVideoId).collect(Collectors.toList());
        //TODO cache
    }

    @Override
    public List<String> getSpecificSavedVideosOfUser(String userId, List<String> videoIds) {
        log.debug("Getting specific videos {} saved for user {}", videoIds, userId);
        List<UserVideo> userVideos = repo.findAllByUserIdAndVideoIdIn(userId, videoIds);
        log.debug("Got saved videos {} for user {}", userVideos, userId);
        return userVideos.stream().map(UserVideo::getVideoId).collect(Collectors.toList());
        //TODO cache
    }
}
