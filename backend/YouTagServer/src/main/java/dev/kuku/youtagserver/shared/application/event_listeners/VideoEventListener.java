package dev.kuku.youtagserver.shared.application.event_listeners;

import dev.kuku.youtagserver.user_tag.api.services.TagService;
import dev.kuku.youtagserver.user_video.api.UserVideoService;
import dev.kuku.youtagserver.video.api.events.VideoAddedEvent;
import dev.kuku.youtagserver.video.api.events.VideoDeletedEvent;
import dev.kuku.youtagserver.video.api.events.VideoUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VideoEventListener {

    final UserVideoService userVideoService;
    final TagService tagService;

    @Async
    @TransactionalEventListener
    void on(VideoAddedEvent event) {
        log.debug("Video Added Event : {}", event);
    }

    @Async
    @TransactionalEventListener
    void on(VideoUpdatedEvent event) {
        log.debug("Video Updated Event : {}", event);
    }

    /**
     * Remove entries in user_video table with matching videoID
     * Remove entries in tags table with matching videoId
     */
    @Async
    @TransactionalEventListener
    void on(VideoDeletedEvent event) {
        log.debug("Video Deleted Event : {}", event);
        log.debug("Deleting entries in user_video table with videoID {}", event.id());
        userVideoService.removeConnectionFromAllUsers(event.id());
        log.debug("Deleting entries in tags table with videoID {}", event.id());
        tagService.deleteAllTagsOfAllUsersOfVideo(event.id());
    }
}
