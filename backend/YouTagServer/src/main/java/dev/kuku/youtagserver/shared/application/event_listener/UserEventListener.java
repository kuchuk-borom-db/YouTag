package dev.kuku.youtagserver.shared.application.event_listener;

import dev.kuku.youtagserver.user.api.events.UserDeletedEvent;
import dev.kuku.youtagserver.user_tag.api.services.UserTagService;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.user_video_tag.api.UserVideoTagService;
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
public class UserEventListener {
    final UserVideoService userVideoService;
    final UserTagService userTagService;
    final UserVideoTagService userVideoTagService;

    /**
     * Delete entries from user_video, user_tag, user_video_tag
     */
    @Async
    @TransactionalEventListener
    void on(UserDeletedEvent event) {
        log.debug("User Deleted Event : {}", event);
        String userId = event.email();
        userVideoService.deleteAllSavedVideosFromUser(userId);
        userTagService.deleteAllTagsOfUser(userId);
        userVideoTagService.deleteAllTagsFromAllVideosOfUser(userId);
    }
}
