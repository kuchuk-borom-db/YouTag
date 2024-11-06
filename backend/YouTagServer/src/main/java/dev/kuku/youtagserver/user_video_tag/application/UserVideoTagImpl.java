package dev.kuku.youtagserver.user_video_tag.application;

import dev.kuku.youtagserver.shared.helper.CacheSystem;
import dev.kuku.youtagserver.user_video_tag.api.dto.UserVideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.exceptions.UserVideoTagAlreadyExists;
import dev.kuku.youtagserver.user_video_tag.api.services.UserVideoTagService;
import dev.kuku.youtagserver.user_video_tag.infrastructure.UserVideoTagRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserVideoTagImpl implements UserVideoTagService {
    final UserVideoTagRepo repo;
    final CacheSystem cacheSystem;


    @Override
    public void addTagToVid(String id, String userId, String tag) throws UserVideoTagAlreadyExists {

    }

    @Override
    public UserVideoTagDTO get(String userId, String videoId, String tag) {
        return null;
    }

    @Override
    public List<UserVideoTagDTO> getWithUserId(String userId) {
        return List.of();
    }

    @Override
    public List<UserVideoTagDTO> getWithVideoId(String videoId) {
        return List.of();
    }

    @Override
    public List<UserVideoTagDTO> getWithTag(String tag) {
        return List.of();
    }

    @Override
    public List<UserVideoTagDTO> getWithUserIdAndVideoId(String userId, String videoId) {
        return List.of();
    }

    @Override
    public List<UserVideoTagDTO> getWithUserIdAndTag(String userId, String tag) {
        return List.of();
    }

    @Override
    public List<UserVideoTagDTO> getWithVideoIdAndTag(String videoId, String tag) {
        return List.of();
    }

    @Override
    public void delete(String userId, String videoId, String tag) {

    }

    @Override
    public void deleteWithUserId(String userId) {

    }

    @Override
    public void deleteWithVideoId(String videoId) {

    }

    @Override
    public void deleteWithTag(String tag) {

    }

    @Override
    public void deleteWithUserIdAndVideoId(String userId, String videoId) {

    }

    @Override
    public void deleteWithUserIdAndTag(String userId, String tag) {

    }

    @Override
    public void deleteWithVideoIdAndTag(String videoId, String tag) {

    }
}
