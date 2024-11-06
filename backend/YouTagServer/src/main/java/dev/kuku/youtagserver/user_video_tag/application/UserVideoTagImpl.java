package dev.kuku.youtagserver.user_video_tag.application;

import dev.kuku.youtagserver.shared.helper.CacheSystem;
import dev.kuku.youtagserver.user_video_tag.api.dto.UserVideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.exceptions.UserVideoTagAlreadyExists;
import dev.kuku.youtagserver.user_video_tag.api.exceptions.UserVideoTagNotFound;
import dev.kuku.youtagserver.user_video_tag.api.services.UserVideoTagService;
import dev.kuku.youtagserver.user_video_tag.domain.entity.UserVideoTag;
import dev.kuku.youtagserver.user_video_tag.infrastructure.UserVideoTagRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        try {
            getUserVideoTagByVideoIdUserIdAndTag(id, userId, tag);
            throw new UserVideoTagAlreadyExists(userId, id, tag);
        } catch (UserVideoTagNotFound e) {
            log.info("Adding tag {} to video {} for user {}", tag, id, userId);
        }
        repo.save(new UserVideoTag(userId, id, tag));
    }

    @Override
    public List<UserVideoTagDTO> getVideosOfUserWithTag(String userId, String tag) {
        log.info("Getting videos with tag {} for user {}", tag, userId);
        var vids = repo.findAllByUserIdAndTag(userId.trim(), tag);
        log.info("Found : {}", vids);
        List<UserVideoTagDTO> dtos = new ArrayList<>();
        for (var v : vids) {
            dtos.add(toDto(v));
        }
        return dtos;
    }

    @Override
    public void deleteTagsFromVideo(String id, String email, String[] tagsToRemove) {
        log.info("Deleting tag {} from video {} for user {}", tagsToRemove, id, email);
        for (String tag : tagsToRemove) {
            repo.deleteAllByUserIdAndVideoIdAndTag(email.trim(), id, tag);
        }
    }

    @Override
    public UserVideoTagDTO getUserVideoTagByVideoIdUserIdAndTag(String id, String userId, String tag) throws UserVideoTagNotFound {
        UserVideoTag videoTag = cacheSystem.getObject(String.format("%s%s%s", userId, id, tag), UserVideoTag.class);
        if (videoTag == null) {
            videoTag = repo.findByUserIdAndTagAndVideoId(userId, tag, id);
            cacheSystem.cache(String.format("%s%s%s", userId, id, tag), videoTag);
        }
        if (videoTag == null) {
            throw new UserVideoTagNotFound(userId, id, tag);
        }
        return toDto(videoTag);
    }

    @Override
    public String[] getTagsOfVideo(String videoId, String userId) {
        log.info("Getting tags of video {} for user {}", videoId, userId);
        List<String> tags = new ArrayList<>();
        repo.findAllByUserIdAndVideoId(userId, videoId).forEach(userVideoTag -> tags.add(userVideoTag.getTag()));
        return tags.toArray(new String[0]);
    }

    private UserVideoTagDTO toDto(UserVideoTag userVideoTag) {
        return new UserVideoTagDTO(userVideoTag.getUserId(), userVideoTag.getVideoId(), userVideoTag.getTag());
    }
}
