package dev.kuku.youtagserver.video.domain.eventListener;


import dev.kuku.youtagserver.user_video.api.events.VideoUnlinkedEvent;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserVideoEventListenerVideo {

    final UserVideoService userVideoService;
    final VideoService videoService;

    @TransactionalEventListener
    @Async
    void on(VideoUnlinkedEvent e) {
        if (userVideoService.getUserVideosContainingVideoId(e.videoId()).isEmpty()) {
            log.info("Removing vide {}. No user has it linked.", e.videoId());
            try {
                videoService.deleteVideo(e.videoId());
            } catch (VideoNotFound ex) {
                log.warn(ex.getMessage());
            }
        }
    }
}
