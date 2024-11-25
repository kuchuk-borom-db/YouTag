package dev.kuku.youtagserver.shared.application;

import dev.kuku.youtagserver.user_tag.api.services.UserTagService;
import dev.kuku.youtagserver.user_video.api.events.DeleteSpecificVideosFromAllUsers;
import dev.kuku.youtagserver.user_video.api.events.DeleteAllSavedVideosFromUser;
import dev.kuku.youtagserver.user_video.api.events.DeleteSavedVideosFromUser;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.user_video_tag.api.UserVideoTagService;
import dev.kuku.youtagserver.video.api.services.VideoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserVideoEventListener {
    final UserVideoTagService userVideoTagService;
    final UserTagService userTagService;
    final UserVideoService userVideoService;
    final VideoService videoService;

    /**
     * Remove all entries with matching userId from User_video_tag table
     * Next, remove all entries from user_tag table as no tag is being used anymore.
     * Next, check if the removed videos are saved for another user. If not then we remove the videos from video table too.
     */
    @Async
    @TransactionalEventListener
    void on(DeleteAllSavedVideosFromUser event) {
        log.debug("Remove all saved videos of user event : {}", event);
        userVideoTagService.deleteAllTagsFromAllVideosOfUser(event.userId());
        userTagService.deleteAllTagsOfUser(event.userId());
        List<String> savedVideos = userVideoService.getSpecificSavedVideosForAllUser(event.deletedVideos());
        var toDeleteVideos = event.deletedVideos().stream().filter(videoId -> !savedVideos.contains(videoId)).toList();
        videoService.deleteSpecifiedVideos(toDeleteVideos);
    }

    /**
     * Remove entries with matching userId and videoIds from user_video_tag table
     * Next, we get the tags that were used in the videos that got removed, check if these tags are used in any other saved videos. If not used then remove them.
     * Next, check if the removed videos are saved for another user. If not, we remove the videos from video table
     */
    @Async
    @TransactionalEventListener
    void on(DeleteSavedVideosFromUser event) {
        log.debug("Remove saved videos of user event : {}", event);
        List<String> deletedTags = userVideoTagService.deleteAllTagsFromSpecificSavedVideosOfUser(event.userId(), event.videoIds());
        userTagService.deleteSpecifiedTagsOfUser(event.userId(), deletedTags);
        //Check if these videos are saved for any all users. This is to determine if the video needs to be deleted
        List<String> savedVideos = userVideoService.getSpecificSavedVideosForAllUser(event.videoIds());
        //Filter out the videoIds that are not saved
        var toDeleteVideos = event.videoIds().stream().filter(videoId -> !savedVideos.contains(videoId)).toList();
        videoService.deleteSpecifiedVideos(toDeleteVideos);
    }


    @Async
    @TransactionalEventListener
    void on(DeleteSpecificVideosFromAllUsers event) {
        log.debug("Delete specified videos from all users event : {}", event);
        List<String> deletedTags = userVideoTagService.deleteAllTagsFromSpecificSavedVideosOfUsers(event.affectedUsers(), event.videoIds());
        userTagService.deleteSpecifiedTagsOfUsers(event.affectedUsers(), deletedTags);
        List<String> savedVideos = userVideoService.getSpecificSavedVideosForAllUser(event.videoIds());
        //Filter out the videoIds that are not saved
        var toDeleteVideos = event.videoIds().stream().filter(videoId -> !savedVideos.contains(videoId)).toList();
        videoService.deleteSpecifiedVideos(toDeleteVideos);
    }
}
