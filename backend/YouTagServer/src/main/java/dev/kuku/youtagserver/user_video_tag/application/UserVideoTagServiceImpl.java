package dev.kuku.youtagserver.user_video_tag.application;

import dev.kuku.youtagserver.user_video_tag.api.dto.UserVideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.events.AddedTagsToVideo;
import dev.kuku.youtagserver.user_video_tag.api.events.DeletedUserVideoTag;
import dev.kuku.youtagserver.user_video_tag.api.exceptions.UserVideoTagNotFound;
import dev.kuku.youtagserver.user_video_tag.api.services.UserVideoTagService;
import dev.kuku.youtagserver.user_video_tag.domain.entity.UserVideoTag;
import dev.kuku.youtagserver.user_video_tag.domain.entity.UserVideoTagId;
import dev.kuku.youtagserver.user_video_tag.infrastructure.UserVideoTagRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
//TODO Caching
public class UserVideoTagServiceImpl implements UserVideoTagService {
    final UserVideoTagRepo repo;
    final ApplicationEventPublisher eventPublisher;

    @Override
    public void addTagsToVid(String userId, String videoId, List<String> tags) {
        tags = tags.stream().map(t -> t.trim().toLowerCase()).toList();
        List<UserVideoTag> userVideoTags = new ArrayList<>();

        for (String tag : tags) {
            try {
                get(userId, videoId, tag);
                userVideoTags.add(new UserVideoTag(userId, videoId, tag));
            } catch (UserVideoTagNotFound e) {
                log.error(e.getMessage());
            }
        }
        log.info("Adding tags {} to video {}", tags, videoId);
        repo.saveAll(userVideoTags);
        eventPublisher.publishEvent(new AddedTagsToVideo(userId, videoId, tags));
    }

    @Override
    public UserVideoTagDTO get(String userId, String videoId, String tag) throws UserVideoTagNotFound {
        var data = repo.findById(new UserVideoTagId(userId, videoId, tag));
        if (data.isEmpty()) {
            throw new UserVideoTagNotFound(userId, videoId, tag);
        }
        log.info("Getting userVideoTagDTO {}", data);
        return toDTO(data.get());
    }

    @Override
    public List<UserVideoTagDTO> getWithUserId(String userId) {
        List<UserVideoTag> datas = repo.findAllByUserId(userId);
        log.info("Getting videos of userId {}-> {}", userId, datas);
        return datas.stream().map(this::toDTO).toList();
    }

    @Override
    public List<UserVideoTagDTO> getWithUserIdAndVideoId(String userId, String videoId) {
        List<UserVideoTag> datas = repo.findAllByUserIdAndVideoId(userId, videoId);
        log.info("Getting data with user {} and videoId {} -> {}", userId, videoId, datas);
        return datas.stream().map(this::toDTO).toList();
    }

    @Override
    public List<UserVideoTagDTO> getWithUserIdAndTags(String userId, String[] tags) {
        List<UserVideoTag> datas = repo.findAllByUserIdAndTagIn(userId, Arrays.stream(tags).toList());
        log.info("getting data with user {} and tags {} -> {}", userId, Arrays.toString(tags), datas);
        return datas.stream().map(this::toDTO).toList();
    }


    @Override
    public void deleteWithUserIdAndTag(String userId, String[] tags) {
        var deleted = repo.deleteAllByUserIdAndTagIn(userId, Arrays.stream(tags).map(t -> t.trim().toLowerCase()).toList());
        var deletedDto = deleted.stream().map(this::toDTO).toList();
        eventPublisher.publishEvent(new DeletedUserVideoTag(deletedDto));
    }

    @Override
    public void deleteWithUserIdAndVideoIdAndTagIn(String userId, String videoId, List<String> tags) {
        var deleted = repo.deleteAllByUserIdAndVideoIdAndTagIn(userId, videoId, tags);
        var deletedDto = deleted.stream().map(this::toDTO).toList();
        eventPublisher.publishEvent(new DeletedUserVideoTag(deletedDto));
    }


    private UserVideoTagDTO toDTO(UserVideoTag userVideoTag) {
        return new UserVideoTagDTO(userVideoTag.getUserId(), userVideoTag.getVideoId(), userVideoTag.getTag());
    }
}
