package dev.kuku.youtagserver.shared.application.event_listeners;

import dev.kuku.youtagserver.user_tag.api.services.TagService;
import dev.kuku.youtagserver.user_video.api.events.LinkedVideoToUser;
import dev.kuku.youtagserver.user_video.api.events.UnlinkedAllVideosFromUser;
import dev.kuku.youtagserver.user_video.api.events.UnlinkedVideoFromUser;
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
public class UserVideoEventListener {

    final TagService tagService;

    @Async
    @TransactionalEventListener
    void on(LinkedVideoToUser event) {
        log.debug("Linked Video To User Event : {}", event);
    }

    /**
     * Remove entries in tags table with the same userID and videoID
     */
    @Async
    @TransactionalEventListener
    void on(UnlinkedVideoFromUser event) {
        log.debug("Unlinked Video From User Event : {}", event);
        tagService.deleteAllTagsOfVideoOfUser(event.userId(), event.videoId());
    }

    /**
     * Remove all entries in tags table with same userID
     */
    @Async
    @TransactionalEventListener
    void on(UnlinkedAllVideosFromUser event) {
        log.debug("Unlinked All Videos From User Event : {}", event);
        tagService.deleteAllTagsOfUser(event.userId());
    }
}
