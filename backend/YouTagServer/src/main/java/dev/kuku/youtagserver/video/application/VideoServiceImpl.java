package dev.kuku.youtagserver.video.application;

import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.services.UserService;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video_tags.api.services.UserVidTagService;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.events.VideoAddedEvent;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.domain.entity.Video;
import dev.kuku.youtagserver.video.infrastructure.repo.VideoRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VideoServiceImpl implements VideoServiceInternal {
    final VideoRepo videoRepo;
    final UserService userService;
    final ApplicationEventPublisher eventPublisher;
    final UserVidTagService userVidTagService;

    @Override
    public boolean addVideoForUser(String video, String userEmail) throws UserAndVideoAlreadyLinked {
        try {
            //Check if the video and user is valid
            getVideo(video);
            userService.getUser(userEmail);
        } catch (EmailNotFound e) {
            log.error("User Email not found {}", userEmail);
            return false;
        } catch (VideoNotFound videoNotFound) {
            log.warn("Video not found {}. Adding it to database", video);
            addVideoToDb(video);
        }
        userVidTagService.linkUserAndVideo(userEmail, video);
        return false;
    }

    private void addVideoToDb(String video) {
        videoRepo.save(new Video(video, "NA", "NA", "NA"));
        eventPublisher.publishEvent(new VideoAddedEvent(video));
    }

    private VideoDTO getVideo(String id) throws VideoNotFound {
        Optional<Video> vid = videoRepo.findById(id);
        if (vid.isEmpty()) {
            throw new VideoNotFound(id);
        }
        return toDTO(vid.get());
    }

    private VideoDTO toDTO(Video video) {
        if (video == null) {
            return null;
        }
        return new VideoDTO(video.getId(), video.getTitle(), video.getDescription(), video.getThumbnail());
    }
}
