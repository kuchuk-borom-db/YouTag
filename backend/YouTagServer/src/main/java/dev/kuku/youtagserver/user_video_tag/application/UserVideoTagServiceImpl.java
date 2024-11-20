package dev.kuku.youtagserver.user_video_tag.application;

import dev.kuku.youtagserver.user_video_tag.api.services.UserVideoTagService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class UserVideoTagServiceImpl implements UserVideoTagService {
    @Override
    public List<String> getTagsOfSavedVideoOfUser(String userId, String videoId) {
        return List.of();
    }

    @Override
    public void addTagsForSavedVideosOfUser(String userId, List<String> tags, List<String> videoIds) {

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
}
