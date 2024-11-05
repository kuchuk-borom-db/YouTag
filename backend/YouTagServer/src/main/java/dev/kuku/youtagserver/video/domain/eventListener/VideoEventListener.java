package dev.kuku.youtagserver.video.domain.eventListener;

import dev.kuku.youtagserver.video.api.events.VideoAddedEvent;
import dev.kuku.youtagserver.video.domain.entity.Video;
import dev.kuku.youtagserver.video.infrastructure.repo.VideoRepo;
import dev.kuku.youtagserver.webscraper.api.services.YoutubeScrapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class VideoEventListener {
    final YoutubeScrapperService scrapperService;
    final VideoRepo videoRepo;

    @TransactionalEventListener
    @Async
    void on(VideoAddedEvent addedVideoID) {
        String vidID = addedVideoID.getVideoId();
        var vidInfo = scrapperService.getYoutubeVideoInfo(vidID);
        log.info("Updating video info {}", vidInfo);
        videoRepo.save(new Video(vidID, vidInfo.title(), vidInfo.description(), vidInfo.thumbnail(), LocalDateTime.now()));
    }
}
