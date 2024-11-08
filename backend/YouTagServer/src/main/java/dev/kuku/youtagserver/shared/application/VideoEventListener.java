package dev.kuku.youtagserver.shared.application;

import dev.kuku.youtagserver.junction.api.services.JunctionService;
import dev.kuku.youtagserver.video.api.events.VideoAddedEvent;
import dev.kuku.youtagserver.video.api.events.VideoDeletedEvent;
import dev.kuku.youtagserver.video.api.events.VideoUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VideoEventListener {
    final JunctionService junctionService;

    @Async
    @TransactionalEventListener
    void on(VideoAddedEvent event) {
        log.debug("Video added event :- {}", event);
    }

    @Async
    @TransactionalEventListener
    void on(VideoUpdatedEvent event) {
        log.debug("Video updated event :- {}", event);
    }

    /**
     * Delete every entries with matching videoId in junction
     */
    @Async
    @TransactionalEventListener
    void on(VideoDeletedEvent event) {
        log.debug("Video deleted event :- {}", event);
        junctionService.deleteVideos(List.of(event.id()));
    }
}
