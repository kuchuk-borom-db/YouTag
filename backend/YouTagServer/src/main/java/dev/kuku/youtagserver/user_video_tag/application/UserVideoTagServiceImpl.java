package dev.kuku.youtagserver.user_video_tag.application;

import dev.kuku.youtagserver.user_tag.api.dtos.UserTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.dtos.UserVideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.services.UserVideoTagService;
import dev.kuku.youtagserver.user_video_tag.domain.UserVideoTag;
import dev.kuku.youtagserver.user_video_tag.infrastructure.UserVideoTagRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserVideoTagServiceImpl implements UserVideoTagService {
    final UserVideoTagRepo repo;
    final ApplicationEventPublisher eventPublisher;

    @Override
    public List<UserVideoTagDTO> getTagsOfSavedVideoOfUser(String userId, String videoId) {
        log.info("Getting all tags of saved video {} of user {}", videoId, userId);
        List<UserVideoTag> userVideoTags = repo.findAllByUserIdAndVideoId(userId, videoId);
        List<UserVideoTagDTO> userVideoTagDTOS = userVideoTags.stream().map(userVideoTag -> new UserVideoTagDTO(userVideoTag.getUserId(),userVideoTag.getVideoId(),userVideoTag.getTagId())).toList();
        log.debug("Got tag IDs {}", userVideoTagDTOS);
        return userVideoTagDTOS;
    }

    @Override
    public void addTagsForSavedVideosOfUser(String userId, List<String> tagIds, List<String> videoIds) {
        log.debug("Adding tagIds {} of saved videos {} of user {}", tagIds, videoIds, userId);
        repo.
    }

    @Override
    public void deleteTagsForSavedVideosOfUser(String userId, List<String> tags, List<String> videoIds) {

    }

    @Override
    public void deleteTagsFromAllSavedVideosOfUser(String userId, List<String> tags) {

    }

    @Override
    public void deleteAllTagsFromSavedVideosOfUser(String userId, List<String> videoIds) {

    }

    @Override
    public List<String> getAllVideosWithTags(String userId, List<String> tags, int skip, int limit) {
        return List.of();
    }

    @Override
    public Set<String> getAllTagsOfVideos(String userId, List<String> videoIds, int skip, int limit) {
        return Set.of();
    }

    @Override
    public boolean doesTagsExistForVideos(String userId, List<String> tags, List<String> videoIds) {
        return false;
    }
}
