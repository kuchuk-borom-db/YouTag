package dev.kuku.youtagserver.user_video.application;

import dev.kuku.youtagserver.user_video.api.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.UserVideoService;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoNotFound;
import dev.kuku.youtagserver.user_video.domain.UserVideo;
import dev.kuku.youtagserver.user_video.infrastructure.UserVideoRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserVideoServiceImpl implements UserVideoService {
    final UserVideoRepo repo;

    @Override
    public void linkVideoToUser(String userId, String videoId) throws UserVideoAlreadyLinked {
        log.debug("Attempting Linking Video {} -> {}", videoId, userId);
        try {
            //Check if link already exists
            getVideoOfUser(userId, videoId);
            throw new UserVideoAlreadyLinked(userId, videoId);
        } catch (UserVideoNotFound e) {
            log.debug("No existing link found. Proceeding to link...");
            repo.save(new UserVideo(UUID.randomUUID().toString(), userId, videoId));
        }
    }

    @Override
    public void unlinkVideoFromUser(String userId, String videoId) throws UserVideoNotFound {
        log.debug("Attempting Unlinking Video {} -> {}", videoId, userId);
        //Check if video exists
        getVideoOfUser(userId, videoId);
        log.debug("Deleting Link {} -> {}", videoId, userId);
        repo.deleteByUserIdAndVideoId(userId, videoId);
    }

    @Override
    public void unlinkAllVideosFromUser(String userId) {
        log.debug("Unlinking All Videos from user {}", userId);
        repo.deleteByUserId(userId);
    }

    @Override
    public List<UserVideoDTO> getVideosOfUser(String userId, int skip, int limit) {
        log.debug("Getting videos of user {}, skipping {} and limit {}", userId, skip, limit);
        int pageNumber = skip / limit;
        List<UserVideo> videos = repo.getAllByUserId(userId, PageRequest.of(pageNumber, limit));
        log.debug("Found {} videos of user {}", videos, userId);
        return videos.stream().map(this::toDto).toList();
    }

    @Override
    public UserVideoDTO getVideoOfUser(String userId, String videoId) throws UserVideoNotFound {
        log.debug("Getting video with userID {} and videoID {}", userId, videoId);
        var video = repo.getByUserIdAndVideoId(userId, videoId);
        if (video == null) {
            throw new UserVideoNotFound(userId, videoId);
        }
        log.debug("Found video {}", video);
        return toDto(video);
    }

    @Override
    public UserVideoDTO toDto(UserVideo e) {
        return new UserVideoDTO(e.getId(), e.getUserId(), e.getVideoId());
    }

}
