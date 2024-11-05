package dev.kuku.youtagserver.webscraper.domain.event;

import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.events.VideoAddedEvent;
import dev.kuku.youtagserver.video.api.events.VideoRequestedEvent;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import dev.kuku.youtagserver.webscraper.api.services.YoutubeScrapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoEventListener {
    final YoutubeScrapperService youtubeScrapperService;
    final VideoService videoService;

    @TransactionalEventListener
    @Async
    void on(VideoAddedEvent event) {
        String videoID = event.getVideoId();
        updateVidInfo(videoID);
    }

    @Async
    @TransactionalEventListener
    void on(VideoRequestedEvent event) {
        String videoID = event.getId();
        updateVidInfo(videoID);
    }

    void updateVidInfo(String videoID) {
        log.info("Updating video info for video ID {}", videoID);
        var videoInfo = youtubeScrapperService.getYoutubeVideoInfo(videoID);
        try {
            videoService.updateVideoInfo(new VideoDTO(videoID, videoInfo.title(), videoInfo.description(), "no thumbnail for now"));
        } catch (VideoNotFound e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }
}
