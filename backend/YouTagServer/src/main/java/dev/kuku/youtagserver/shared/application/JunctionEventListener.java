package dev.kuku.youtagserver.shared.application;

import dev.kuku.youtagserver.junction.api.dtos.JunctionDTO;
import dev.kuku.youtagserver.junction.api.events.JunctionAddedEvent;
import dev.kuku.youtagserver.junction.api.events.JunctionDeletedEvent;
import dev.kuku.youtagserver.junction.api.exceptions.JunctionDTOHasNullValues;
import dev.kuku.youtagserver.junction.api.services.JunctionService;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
import dev.kuku.youtagserver.video.api.exceptions.VideoDTOHasNullValues;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import dev.kuku.youtagserver.webscraper.api.exceptions.InvalidVideoId;
import dev.kuku.youtagserver.webscraper.api.services.YoutubeScrapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class JunctionEventListener {
    final VideoService videoService;
    final YoutubeScrapperService youtubeScrapperService;
    final JunctionService junctionService;

    /**
     * Add the videos to videos repo and update its details.
     * If invalid videoIDs are found then delete them from junction
     */
    @TransactionalEventListener
    @Async
    void on(JunctionAddedEvent event) {
        List<JunctionDTO> invalidVideoIds = new ArrayList<>(); //Used to delete invalid videos
        log.debug("Junction added event :- {}", event);

        event.junctions().stream()
                //get videoId
                .map(junctionDTO -> {
                    try {
                        return youtubeScrapperService.getYoutubeVideoInfo(junctionDTO.getVideoId());
                    } catch (InvalidVideoId e) {
                        //if invalid then add to Invalid video list
                        invalidVideoIds.add(junctionDTO);
                        return null;
                    }
                })
                //Filter out null values
                .filter(Objects::nonNull)
                //Add videos if they are not added yet
                .forEach(info -> {
                    VideoDTO videoDto = null;
                    try {
                        videoDto = new VideoDTO(info.videoID(), info.title(), info.description(), info.thumbnail());
                    } catch (VideoDTOHasNullValues _) {

                    }
                    try {
                        videoService.addVideo(info.videoID());
                        videoService.updateVideo(videoDto);
                    } catch (VideoAlreadyExists e) {
                        try {
                            videoService.updateVideo(videoDto);
                        } catch (VideoNotFound | VideoDTOHasNullValues _) {
                        }
                    } catch (VideoDTOHasNullValues | VideoNotFound _) {

                    }
                });

        //Next, we need to delete junction records with invalid videoIds
        invalidVideoIds.forEach(junctionDTO -> junctionService.deleteVideosFromUser(junctionDTO.getUserId(), List.of(junctionDTO.getVideoId())));
    }

    /**
     * Checks if there is any entry with the same videoId.
     * If none found, delete the video from videos table
     */
    @Async
    @TransactionalEventListener
    void on(JunctionDeletedEvent event) throws JunctionDTOHasNullValues {
        log.debug("Junction deleted event :- {}", event);
        for (JunctionDTO deletedDto : event.deletedDtos()) {
            var video = junctionService.getVideosOfUser(deletedDto.getUserId(), List.of(deletedDto.getVideoId()), 0, 1);
            if (video == null || video.isEmpty()) {
                try {
                    videoService.deleteVideo(deletedDto.getVideoId());
                } catch (VideoNotFound e) {
                    log.error("Video not found to delete", e);
                } catch (VideoDTOHasNullValues _) {
                }
            }
        }
    }
}
