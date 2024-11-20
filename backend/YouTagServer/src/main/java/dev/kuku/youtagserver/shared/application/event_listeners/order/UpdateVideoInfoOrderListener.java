package dev.kuku.youtagserver.shared.application.event_listeners.order;

import dev.kuku.youtagserver.shared.events.UpdateVideoInfoOrder;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateVideoInfoOrderListener {
    final VideoService videoService;

    @Async
    @TransactionalEventListener
    void on(UpdateVideoInfoOrder order) {
        log.debug("Update video info event with videoID : {}", order.getVideo().getId());
        try {
            videoService.updateVideo(order.getVideo());
        } catch (VideoNotFound e) {
            log.error(e.getMessage());
        }
    }
}
