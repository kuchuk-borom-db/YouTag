package dev.kuku.youtagserver.user_video_tags.application;

import dev.kuku.youtagserver.user_video_tags.api.dto.UserVidTagDto;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoLinkNotFound;
import dev.kuku.youtagserver.user_video_tags.api.services.UserVidTagService;
import dev.kuku.youtagserver.user_video_tags.domain.entity.UserVidTag;
import dev.kuku.youtagserver.user_video_tags.infrastructure.repo.UserVidTagRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserVidTagServiceImpl implements UserVidTagService {
    private final UserVidTagRepo repo;

    @Override
    public void linkUserAndVideo(String user, String video) throws UserAndVideoAlreadyLinked {
        log.debug("Attempting to link user {} with video {}", user, video);
        try {
            getUserAndVideoTag(user, video);
            log.error("Link already exists between user {} and video {}", user, video);
            throw new UserAndVideoAlreadyLinked(video, user);
        } catch (UserAndVideoLinkNotFound e) {
            log.info("Creating new link between user {} and video {}", user, video);
            UserVidTag newLink = new UserVidTag(user, video, new ArrayList<>());
            repo.save(newLink);
            log.info("Successfully linked user {} and video {}", user, video);
        }
    }

    @Override
    public void addTag(String user, String video, String tag) throws UserAndVideoLinkNotFound {
        log.debug("Attempting to add tag {} for user {} and video {}", tag, user, video);
        UserVidTag userVidTag = toEntity(getUserAndVideoTag(user, video));

        if (userVidTag.getTags() == null) {
            log.debug("Initializing tags list for user {} and video {}", user, video);
            userVidTag.setTags(new ArrayList<>());
        }

        if (userVidTag.getTags().contains(tag)) {
            log.debug("Tag {} already exists for user {} and video {}", tag, user, video);
            return;
        }

        userVidTag.getTags().add(tag);
        repo.save(userVidTag);
        log.info("Successfully added tag {} for user {} and video {}", tag, user, video);
    }

    @Override
    public UserVidTagDto getUserAndVideoTag(String user, String video) throws UserAndVideoLinkNotFound {
        log.debug("Fetching link for user {} and video {}", user, video);
        var userVidTag = repo.findUserVidTagByUserEmailAndVideoId(user, video);
        if (userVidTag == null) {
            throw new UserAndVideoLinkNotFound(user, video);
        }
        return toDto(userVidTag);
    }

    private UserVidTagDto toDto(UserVidTag userVidTag) {
        return new UserVidTagDto(userVidTag.getUserEmail(), userVidTag.getVideoId(), userVidTag.getTags());
    }

    private UserVidTag toEntity(UserVidTagDto userVidTagDto) {
        return new UserVidTag(userVidTagDto.user_email(), userVidTagDto.video_id(), userVidTagDto.tags());
    }
}