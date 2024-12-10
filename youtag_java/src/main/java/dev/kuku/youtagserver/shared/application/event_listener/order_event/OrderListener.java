package dev.kuku.youtagserver.shared.application.event_listener.order_event;

import dev.kuku.youtagserver.shared.api.events.RemoveVideosOrder;
import dev.kuku.youtagserver.shared.api.events.UpdateVideoInfosOrder;
import dev.kuku.youtagserver.shared.application.OrchestratorService;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.services.VideoService;
import dev.kuku.youtagserver.webscraper.api.exceptions.InvalidVideoId;
import dev.kuku.youtagserver.webscraper.api.services.YoutubeScrapperService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderListener {
    final OrchestratorService orchestratorService;
    final VideoService videoService;
    final YoutubeScrapperService youtubeScrapperService;
    final ApplicationEventPublisher eventPublisher;

    @Async
    @TransactionalEventListener
    void on(RemoveVideosOrder order) {
        log.debug("Remove videos {} event", order);
        orchestratorService.deleteSpecificVideos(order.invalidVideos());
    }

    @Async
    @TransactionalEventListener
    void on(UpdateVideoInfosOrder order) {
        log.debug("Update videoInfos order {}", order.videoIds());
        Set<String> invalidVideos = new HashSet<>();
        List<VideoDTO> videoDTOS = new ArrayList<>();
        for (String id : order.videoIds()) {
            try {
                var videoInfo = youtubeScrapperService.getYoutubeVideoInfo(id);
                videoDTOS.add(new VideoDTO(id, videoInfo.title(), videoInfo.description(), videoInfo.thumbnail()));
            } catch (InvalidVideoId e) {
                //Failed to get video info. Remove it from video using event publisher
                invalidVideos.add(id);
            }
        }
        if (!invalidVideos.isEmpty()) {
            eventPublisher.publishEvent(new RemoveVideosOrder(invalidVideos));
        }
        videoService.updateVideos(videoDTOS);
    }
}
