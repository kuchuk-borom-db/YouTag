package dev.kuku.youtagserver.user_video.application;

import dev.kuku.youtagserver.user_video.api.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.UserVideoService;
import dev.kuku.youtagserver.user_video.api.events.LinkedVideoToUser;
import dev.kuku.youtagserver.user_video.api.events.UnlinkedAllVideosFromUser;
import dev.kuku.youtagserver.user_video.api.events.UnlinkedVideoFromUser;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video.domain.UserVideo;
import dev.kuku.youtagserver.user_video.infrastructure.UserVideoRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserVideoServiceImpl implements UserVideoService {
    final UserVideoRepo repo;
    final ApplicationEventPublisher eventPublisher;
    private final Map<String, List<UserVideoDTO>> videosCache = new ConcurrentHashMap<>();
    private final Map<String, Boolean> linkExistsCache = new ConcurrentHashMap<>();

    /**
     * Generates a cache key for user's video list based on userId and pagination parameters.
     */
    private String generateVideoListCacheKey(String userId, int skip, int limit) {
        return "videos:" + userId + ":" + skip + ":" + limit;
    }

    /**
     * Generates a cache key for checking if a video is linked to a user.
     */
    private String generateLinkExistsCacheKey(String userId, String videoId) {
        return "link:" + userId + ":" + videoId;
    }

    /**
     * Evicts all cache entries related to a specific user.
     */
    private void evictCache(String userId) {
        // Evict video list cache entries
        videosCache.entrySet().removeIf(entry -> entry.getKey().startsWith("videos:" + userId));

        // Evict link exists cache entries
        linkExistsCache.entrySet().removeIf(entry -> entry.getKey().startsWith("link:" + userId));

        log.debug("Evicted cache entries for user {}", userId);
    }

    /**
     * Evicts cache entries for a specific user-video combination.
     */
    private void evictCache(String userId, String videoId) {
        evictCache(userId); // Evict all user's cache since video list might change
        linkExistsCache.remove(generateLinkExistsCacheKey(userId, videoId));
        log.debug("Evicted cache entries for user {} and video {}", userId, videoId);
    }

    @Override
    public void linkVideoToUser(String userId, String videoId) throws UserVideoAlreadyLinked {
        log.debug("Attempting Linking Video {} -> {}", videoId, userId);

        if (isVideoLinkedWithUser(userId, videoId)) {
            throw new UserVideoAlreadyLinked(userId, videoId);
        } else {
            log.debug("No existing link found. Proceeding to link...");
            var saved = repo.save(new UserVideo(UUID.randomUUID().toString(), userId, videoId));
            evictCache(userId, videoId);
            eventPublisher.publishEvent(new LinkedVideoToUser(saved));
        }
    }

    @Override
    public void unlinkVideoFromUser(String userId, String videoId) {
        isVideoLinkedWithUser(userId, videoId);
        log.debug("Deleting link between {}->{}", userId, videoId);
        repo.deleteByUserIdAndVideoId(userId, videoId);
        evictCache(userId, videoId);
        eventPublisher.publishEvent(new UnlinkedVideoFromUser(userId, videoId));
    }

    @Override
    public void unlinkAllVideosFromUser(String userId) {
        log.debug("Unlinking All Videos from user {}", userId);
        repo.deleteByUserId(userId);
        evictCache(userId);
        eventPublisher.publishEvent(new UnlinkedAllVideosFromUser(userId));
    }

    @Override
    public void unlinkVideoFromAllUsers(String videoId) {
        log.debug("Unlinking video {} from all users", videoId);
        repo.deleteAllByVideoId(videoId);
        evictCache(videoId);
    }

    @Override
    public List<UserVideoDTO> getVideosOfUser(String userId, int skip, int limit) {
        log.debug("Getting videos of user {}, skipping {} and limit {}", userId, skip, limit);
        String cacheKey = generateVideoListCacheKey(userId, skip, limit);

        return videosCache.computeIfAbsent(cacheKey, _ -> {
            int pageNumber = skip / limit;
            List<UserVideo> videos = repo.getAllByUserId(userId, PageRequest.of(pageNumber, limit));
            log.debug("Found {} videos of user {}", videos, userId);
            return videos.stream().map(this::toDto).toList();
        });
    }

    @Override
    public boolean isVideoLinkedWithUser(String userId, String videoId) {
        log.debug("Getting video with userID {} and videoID {}", userId, videoId);
        String cacheKey = generateLinkExistsCacheKey(userId, videoId);

        return linkExistsCache.computeIfAbsent(cacheKey, _ -> {
            var video = repo.getByUserIdAndVideoId(userId, videoId);
            boolean exists = video != null;
            log.debug("Found video link exists: {}", exists);
            return exists;
        });
    }

    @Override
    public UserVideoDTO toDto(UserVideo e) {
        return new UserVideoDTO(e.getId(), e.getUserId(), e.getVideoId());
    }
}