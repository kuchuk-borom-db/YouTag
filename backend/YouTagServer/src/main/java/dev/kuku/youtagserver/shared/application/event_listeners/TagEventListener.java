package dev.kuku.youtagserver.shared.application.event_listeners;

import dev.kuku.youtagserver.tag.api.events.*;
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
public class TagEventListener {

    @Async
    @TransactionalEventListener
    void on(AddedTagsToVideoEvent event) {
        log.debug("Added tags to video event : {}", event);
    }

    @Async
    @TransactionalEventListener
    void on(DeleteAllTagsFromAllUsersOfVideo event) {
        log.debug("Deleted all tags from all users of video event : {}", event);
    }

    @Async
    @TransactionalEventListener
    void on(DeleteAllTagsFromVideoOfUser event) {
        log.debug("Deleted all tags from of video event : {}", event);
    }

    @Async
    @TransactionalEventListener
    void on(DeleteAllTagsOfUser event) {
        log.debug("Deleted all tags of user event : {}", event);
    }

    @Async
    @TransactionalEventListener
    void on(DeleteTagsFromAllVideosOfUser event) {
        log.debug("Deleted tags from users of videos event : {}", event);
    }

    @Async
    @TransactionalEventListener
    void on(DeleteTagsFromVideosOfUser event) {
        log.debug("Deleted tags from users of videos event : {}", event);
    }
}
