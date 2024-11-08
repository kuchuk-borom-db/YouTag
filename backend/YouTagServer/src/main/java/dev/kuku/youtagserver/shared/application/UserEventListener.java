package dev.kuku.youtagserver.shared.application;


import dev.kuku.youtagserver.junction.api.exceptions.JunctionDTOHasNullValues;
import dev.kuku.youtagserver.junction.api.services.JunctionService;
import dev.kuku.youtagserver.user.api.events.UserAddedEvent;
import dev.kuku.youtagserver.user.api.events.UserDeletedEvent;
import dev.kuku.youtagserver.user.api.events.UserUpdatedEvent;
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
public class UserEventListener {
    final JunctionService junctionService;

    @Async
    @TransactionalEventListener
    void on(UserAddedEvent event) {
        log.debug("User Added Event :- {}", event);
    }

    @Async
    @TransactionalEventListener
    void on(UserUpdatedEvent event) {
        log.debug("User Updated Event :- {}", event);
    }

    /**
     * Delete entries from junction table where userId match
     */
    @Async
    @TransactionalEventListener
    void on(UserDeletedEvent event) {
        log.debug("User Deleted Event :- {}", event);
        try {
            junctionService.deleteAllVideosAndTags(event.userId());
        } catch (JunctionDTOHasNullValues _) {

        }
    }
}
