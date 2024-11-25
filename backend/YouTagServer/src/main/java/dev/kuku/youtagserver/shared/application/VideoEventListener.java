package dev.kuku.youtagserver.shared.application;

import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.user_video_tag.api.UserVideoTagService;
import dev.kuku.youtagserver.video.api.events.DeleteSpecifiedVideos;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class VideoEventListener {
    final UserVideoService userVideoService;
    final UserVideoTagService userVideoTagService;

    /**
     * Remove entries from user_video
     * Remove entries from user_video_tag
     */
    @Async
    @TransactionalEventListener
    void on(DeleteSpecifiedVideos event) {
        log.debug("Delete specified videos event {}", event);
        userVideoService.deleteSpecificSavedVideosForAllUsers(event.videoIds());
        userVideoTagService.deleteAllTagsFromSpecificSavedVideosForAllUser(event.videoIds());
    }
}
