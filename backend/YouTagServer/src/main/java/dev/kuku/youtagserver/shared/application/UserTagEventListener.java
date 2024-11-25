package dev.kuku.youtagserver.shared.application;

import dev.kuku.youtagserver.user_tag.api.events.DeleteAllTagsOfUser;
import dev.kuku.youtagserver.user_tag.api.events.DeleteSpecifiedTagsOfUser;
import dev.kuku.youtagserver.user_tag.api.events.DeleteSpecifiedTagsOfUsers;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserTagEventListener {
    @Async
    @TransactionalEventListener
    void on(DeleteSpecifiedTagsOfUsers event) {

    }

    @Async
    @TransactionalEventListener
    void on(DeleteAllTagsOfUser event) {

    }

    @Async
    @TransactionalEventListener
    void on(DeleteSpecifiedTagsOfUser event) {
    }
}
