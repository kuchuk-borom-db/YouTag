package dev.kuku.youtagserver.user.domain.event;

import dev.kuku.youtagserver.auth.api.events.GotUserFromTokenEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Slf4j
class AuthEventListener {
    @Async
    @TransactionalEventListener //Required to persist the event in database. The event broadcaster also needs to be marked as @transactional
    void on(GotUserFromTokenEvent event) {
        log.info("Got userJson from token {}", event.userJson());
    }
}
