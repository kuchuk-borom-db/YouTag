package dev.kuku.youtagserver.user_video_tag.domain.event_listener;

import dev.kuku.youtagserver.user_video.api.events.VideoUnlinkedEvent;
import dev.kuku.youtagserver.user_video_tag.infrastructure.UserVideoTagRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserVideoEventListenerUserVideoTag {
    final UserVideoTagRepo repo;

    @TransactionalEventListener
    @Async
    void on(VideoUnlinkedEvent event) {
        log.info("Removing tags for user {} and video {}", event.userId(), event.videoId());
        repo.deleteAllByUserIdAndVideoId(event.userId(), event.videoId());
    }
}
