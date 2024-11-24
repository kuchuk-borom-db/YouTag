package dev.kuku.youtagserver.shared.application;

import dev.kuku.youtagserver.user_video.api.events.RemoveAllSavedVideosFromUser;
import dev.kuku.youtagserver.user_video.api.events.RemoveSavedVideosFromUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserVideoEventListener {
    /**
     * Remove all entries with matching userId from User_video_tag table
     * Next, remove all entries from user_tag table as no tag is being used anymore.
     * Next, check if the removed videos are saved for another user. If not then we remove the videos from video table too.
     */
    @Async
    @TransactionalEventListener
    void on(RemoveAllSavedVideosFromUser event) {
        log.debug("Remove all saved videos of user event : {}", event);
    }

    /**
     * Remove entries with matching userId and videoIds from user_video_tag table
     * Next, we get the tags that were used in the videos that got removed, check if these tags are used in any other saved videos. If not used then remove them.
     * Next, check if the removed videos are saved for another user. If not, we remove the videos from video table
     */
    @Async
    @TransactionalEventListener
    void on(RemoveSavedVideosFromUser event) {
        log.debug("Remove saved videos of user event : {}", event);
    }
}
