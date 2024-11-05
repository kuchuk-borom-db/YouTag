package dev.kuku.youtagserver.video.application;

import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.services.UserService;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video_tags.api.services.UserVidTagService;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.InvalidVideoIDException;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.domain.entity.Video;
import dev.kuku.youtagserver.video.infrastructure.repo.VideoRepo;
import dev.kuku.youtagserver.webscraper.api.services.YoutubeScrapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
    final YoutubeScrapperService scrapperService;

    @Override
    public void addVideoForUser(String video, String userEmail) throws UserAndVideoAlreadyLinked, InvalidVideoIDException {
        try {
            //Check if the video and user is valid
            getVideo(video);
            userService.getUser(userEmail);
        } catch (EmailNotFound e) {
            log.error("User Email not found {}", userEmail);
            return;
        } catch (VideoNotFound videoNotFound) {
            log.warn("Video not found {}. Adding it to database", video);
            addVideoToDb(video);
        }
        userVidTagService.linkUserAndVideo(userEmail, video);
    }

    @Override
    public void updateVideoInfo(VideoDTO videoDTO) throws VideoNotFound {
        var vidDto = getVideo(videoDTO.id());
        var video = toEntity(vidDto);
        log.info("Updating video {}", videoDTO);
        videoRepo.save(video);
    }

    private void addVideoToDb(String videoID) throws InvalidVideoIDException {
        log.info("Adding videoID {} to database", videoID);
        videoIDValidator(videoID);
        var vidInfo = scrapperService.getYoutubeVideoInfo(videoID);
        videoRepo.save(new Video(videoID, vidInfo.title(), vidInfo.description(), "NA"));
    }

    private VideoDTO getVideo(String id) throws VideoNotFound {
        //TODO: Update video info in bg using event.
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

    private Video toEntity(VideoDTO videoDTO) {
        return new Video(videoDTO.id(), videoDTO.title(), videoDTO.description(), videoDTO.thumbnail());
    }

    private void videoIDValidator(String videoID) throws InvalidVideoIDException {
        String url = "https://www.youtube.com/watch?v=" + videoID;
        try {
            Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new InvalidVideoIDException(videoID);
        }
    }


}
