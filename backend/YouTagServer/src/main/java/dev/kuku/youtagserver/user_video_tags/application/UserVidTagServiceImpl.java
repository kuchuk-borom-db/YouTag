package dev.kuku.youtagserver.user_video_tags.application;

import dev.kuku.youtagserver.shared.helper.CacheSystem;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserVidTagServiceImpl implements UserVidTagService {
    private final UserVidTagRepo repo;
    final CacheSystem cacheSystem;

    @Override
    public void linkUserAndVideo(String user, String videoId) throws UserAndVideoAlreadyLinked {
        log.info("Linking user {} with videoId {}", user, videoId);
        log.info("Checking if link already exists");
        try {
            getUserAndVideoTag(user, videoId);

        } catch (UserAndVideoLinkNotFound e) {
            log.info("Previous Link not found. Creating a new one");
            repo.save(new UserVidTag(user, videoId, List.of()));
            return;
        }
        //If UserAndVideoLinkNotFound is not thrown then previous record existed.
        throw new UserAndVideoAlreadyLinked(videoId, user);
    }

    @Override
    public void addTags(String user, String video, String[] tags) throws UserAndVideoLinkNotFound {
        log.info("Attempting to add tags {} for user {} and video {}", tags, user, video);
        var existing = getUserAndVideoTag(user, video);
        if (existing == null) throw new UserAndVideoLinkNotFound(video, user);
        for (var t : tags) {
            if (!existing.tags().contains(t)) {
                existing.tags().add(t);
            }
        }
        cacheSystem.evict(this.getClass().toString(), String.format("%s-%s", user, video));
        repo.save(new UserVidTag(user, video, existing.tags()));
    }

    @Override
    public UserVidTagDto getUserAndVideoTag(String user, String video) throws UserAndVideoLinkNotFound {
        log.info("Fetching link for user {} and video {}", user, video);
        UserVidTag userVidTag = (UserVidTag) cacheSystem.getObject(this.getClass().toString(), String.format("%s-%s", user, video));
        if (userVidTag == null) {
            userVidTag = repo.findUserVidTagByUserEmailAndVideoId(user, video);
            cacheSystem.cache(this.getClass().toString(), String.format("%s-%s", user, video), userVidTag);
        }
        if (userVidTag == null) throw new UserAndVideoLinkNotFound(video, user);
        return toDto(userVidTag);
    }

    private UserVidTagDto toDto(UserVidTag userVidTag) {
        return new UserVidTagDto(userVidTag.getUserEmail(), userVidTag.getVideoId(), userVidTag.getTags());
    }

    private UserVidTag toEntity(UserVidTagDto userVidTagDto) {
        return new UserVidTag(userVidTagDto.user_email(), userVidTagDto.video_id(), userVidTagDto.tags());
    }
}