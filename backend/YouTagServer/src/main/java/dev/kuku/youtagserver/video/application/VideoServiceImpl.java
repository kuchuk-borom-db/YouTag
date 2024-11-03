package dev.kuku.youtagserver.video.application;

import dev.kuku.youtagserver.user.api.services.UserService;
import dev.kuku.youtagserver.user.domain.exception.InvalidEmailException;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.events.VideoAddedEvent;
import dev.kuku.youtagserver.video.domain.entity.Video;
import dev.kuku.youtagserver.video.infrastructure.repo.VideoRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoServiceInternal {
    final VideoRepo videoRepo;
    final UserService userService;
    final ApplicationEventPublisher eventPublisher;

    @Override
    public boolean addVideoForUser(String video, String userEmail) {
        //Check if video exists
        var vidDto = getVideo(video);
        //Check if user is valid
        try {
            userService.getUser(userEmail);
        } catch (InvalidEmailException e) {
            log.error("User Email is invalid {}", userEmail);
            return false;
        }
        if (vidDto == null) {
            //Add video to db if it doesn't exist
            addVideoToDb(video);
        }

        //TODO user_vid_tag module
        return false;
    }

    private void addVideoToDb(String video) {
        videoRepo.save(new Video(video, "NA", "NA", "NA"));
        eventPublisher.publishEvent(new VideoAddedEvent(video));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private VideoDTO getVideo(String id) {
        Optional<Video> vid = videoRepo.findById(id);
        return toDTO(vid.get());
    }

    private VideoDTO toDTO(Video video) {
        if (video == null) {
            return null;
        }
        return new VideoDTO(video.getId(), video.getTitle(), video.getDescription(), video.getThumbnail());
    }
}
