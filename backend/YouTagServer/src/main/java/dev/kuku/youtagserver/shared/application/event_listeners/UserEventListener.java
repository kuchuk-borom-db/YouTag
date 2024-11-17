package dev.kuku.youtagserver.shared.application.event_listeners;

import dev.kuku.youtagserver.tag.api.services.TagService;
import dev.kuku.youtagserver.user.api.events.UserAddedEvent;
import dev.kuku.youtagserver.user.api.events.UserDeletedEvent;
import dev.kuku.youtagserver.user.api.events.UserUpdatedEvent;
import dev.kuku.youtagserver.user_video.api.UserVideoService;
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
public class UserEventListener {
    private final UserVideoService userVideoService;
    private final TagService tagService;

    @Async
    @TransactionalEventListener
    void on(UserAddedEvent event) {
        log.debug("UserAddedEvent: {}", event);
    }

    @Async
    @TransactionalEventListener
    void on(UserUpdatedEvent event) {
        log.debug("UserUpdatedEvent: {}", event);
    }

    /**
     * Deletes all entries from user_video table with same userID. <br>
     * Deletes all entries from tags table with same userID.
     */
    @Async
    @TransactionalEventListener
    void on(UserDeletedEvent event) {
        log.debug("UserDeletedEvent: {}", event);
        log.debug("Deleting all entries from user_video with userID {}", event.userId());
        userVideoService.unlinkAllVideosFromUser(event.userId());
        log.debug("Deleting all entries from tags with userID {}", event.userId());
        tagService.deleteAllTagsOfUser(event.userId());

    }
}
