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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserVidTagServiceImpl implements UserVidTagService {
    private final UserVidTagRepo repo;

    @Override
    public void linkUserAndVideo(String user, String videoId) throws UserAndVideoAlreadyLinked {
        log.info("Linking user {} with videoId {}", user, videoId);
        var existing = repo.findUserVidTagByUserEmailAndVideoId(user, videoId);
        if (existing != null) throw new UserAndVideoAlreadyLinked(videoId, user);
        repo.save(new UserVidTag(user, videoId, List.of()));
    }

    @Override
    public void addTags(String user, String video, String[] tags) throws UserAndVideoLinkNotFound {
        log.info("Attempting to add tags {} for user {} and video {}", tags, user, video);
        var existing = repo.findUserVidTagByUserEmailAndVideoId(user, video);
        if (existing == null) throw new UserAndVideoLinkNotFound(video, user);
        for (var t : tags) {
            if (!existing.getTags().contains(t)) {
                existing.getTags().add(t);
            }
        }
        repo.save(new UserVidTag(user, video, existing.getTags()));
    }

    @Override
    public UserVidTagDto getUserAndVideoTag(String user, String video) throws UserAndVideoLinkNotFound {
        log.info("Fetching link for user {} and video {}", user, video);
        var found = repo.findUserVidTagByUserEmailAndVideoId(user, video);
        if (found == null) throw new UserAndVideoLinkNotFound(video, user);
        return toDto(found);
    }

    private UserVidTagDto toDto(UserVidTag userVidTag) {
        return new UserVidTagDto(userVidTag.getUserEmail(), userVidTag.getVideoId(), userVidTag.getTags());
    }

    private UserVidTag toEntity(UserVidTagDto userVidTagDto) {
        return new UserVidTag(userVidTagDto.user_email(), userVidTagDto.video_id(), userVidTagDto.tags());
    }
}