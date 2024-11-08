package dev.kuku.youtagserver.junction.application;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.junction.api.dtos.JunctionDTO;
import dev.kuku.youtagserver.junction.api.exceptions.JunctionDTOHasNullValues;
import dev.kuku.youtagserver.junction.api.services.JunctionService;
import dev.kuku.youtagserver.shared.exceptions.VideoInfoTagDTOHasNullValues;
import dev.kuku.youtagserver.shared.models.VideoInfoTagDTO;
import dev.kuku.youtagserver.video.api.exceptions.VideoDTOHasNullValues;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j // For logging
@Service
@RequiredArgsConstructor
public class CommandHandler {
    private final JunctionService junctionService;
    private final AuthService authService;
    private final VideoService videoService;

    /**
     * Adds videos without specified tags (uses default '*' tag).
     */
    public void addVideosWithNoTags(String[] videoIds) throws NoAuthenticatedYouTagUser, JunctionDTOHasNullValues {
        String userId = authService.getCurrentUser().email();
        log.debug("Adding videos with no tags for user: {}", userId);

        // Adds videos with '*' as the default tag representing no tags.
        junctionService.addVideosWithTags(userId, Arrays.stream(videoIds).toList(), List.of("*"));
        log.info("Added videos without specific tags for user: {}, Videos: {}", userId, videoIds);
    }

    /**
     * Adds specified videos with specified tags.
     */
    public void addVideosWithTags(String[] videoIds, String[] tagsArray) throws NoAuthenticatedYouTagUser, JunctionDTOHasNullValues {
        String userId = authService.getCurrentUser().email();
        log.debug("Adding videos with tags for user: {}, Videos: {}, Tags: {}", userId, videoIds, tagsArray);

        // Adds the specified videos with the provided tags.
        junctionService.addVideosWithTags(userId, Arrays.stream(videoIds).toList(), Arrays.stream(tagsArray).toList());
        log.debug("Successfully added videos with tags {} for user: {}", tagsArray, userId);
    }

    /**
     * Deletes all videos and tags for the authenticated user.
     */
    public void deleteAllVideosAndTags() throws NoAuthenticatedYouTagUser, JunctionDTOHasNullValues {
        String userId = authService.getCurrentUser().email();
        log.debug("Deleting all videos and tags for user: {}", userId);

        // Deletes all associations of videos and tags for the user.
        junctionService.deleteAllVideosAndTags(userId);
        log.info("All videos and tags deleted for user: {}", userId);
    }

    /**
     * Deletes specified tags from specified videos.
     */
    public void deleteTagsFromVideos(String[] videos, String[] tags) throws NoAuthenticatedYouTagUser, JunctionDTOHasNullValues {
        String userId = authService.getCurrentUser().email();
        log.debug("Deleting specified tags from videos for user: {}, Videos: {}, Tags: {}", userId, videos, tags);

        junctionService.deleteTagsFromVideos(userId, Arrays.stream(videos).toList(), Arrays.stream(tags).toList());
        log.info("Deleted specified tags from videos for user: {}", userId);
    }

    /**
     * Deletes all videos associated with the specified tags.
     */
    public void deleteTagsFromAllVideos(String[] tags) throws NoAuthenticatedYouTagUser, JunctionDTOHasNullValues {
        String userId = authService.getCurrentUser().email();
        log.debug("Deleting all videos associated with tags for user: {}, Tags: {}", userId, tags);

        junctionService.deleteTagsFromAllVideos(userId, Arrays.stream(tags).toList());
        log.info("Deleted all videos associated with tags for user: {}", userId);
    }

    /**
     * Deletes specified videos for the user.
     */
    public void deleteVideosForUser(String[] videos) throws NoAuthenticatedYouTagUser, JunctionDTOHasNullValues {
        String userId = authService.getCurrentUser().email();
        log.debug("Deleting specified videos for user: {}, Videos: {}", userId, videos);

        junctionService.deleteVideosFromUser(userId, Arrays.stream(videos).toList());
        log.info("Deleted specified videos for user: {}", userId);
    }

    /**
     * Retrieves all videos of the user, paginated by skip and limit.
     */
    public List<VideoInfoTagDTO> getAllVideosOfUser(int skip, int limit) throws NoAuthenticatedYouTagUser, JunctionDTOHasNullValues, VideoDTOHasNullValues, VideoNotFound, VideoInfoTagDTOHasNullValues {
        String userId = authService.getCurrentUser().email();
        log.debug("Fetching all videos for user: {}, Skip: {}, Limit: {}", userId, skip, limit);

        var junctionDTOs = junctionService.getAllJunctionOfUser(userId, skip, limit);
        log.info("Retrieved all videos for user: {}. Total videos: {}", userId, junctionDTOs.size());
        return junctionDtoToVideoInfoTagDTO(junctionDTOs);
    }

    /**
     * Retrieves videos associated with the specified tags, paginated.
     */
    public List<VideoInfoTagDTO> getVideosWithTags(String[] tags, int skip, int limit) throws NoAuthenticatedYouTagUser, VideoDTOHasNullValues, VideoNotFound, VideoInfoTagDTOHasNullValues, JunctionDTOHasNullValues {
        String userId = authService.getCurrentUser().email();
        log.debug("Fetching videos with tags for user: {}, Tags: {}, Skip: {}, Limit: {}", userId, tags, skip, limit);

        List<JunctionDTO> junctionDTOs = junctionService.getAllVideosWithTags(userId, Arrays.stream(tags).toList(), skip, limit);
        log.info("Fetched videos with tags for user: {}. Total results: {}", userId, junctionDTOs.size());
        return junctionDtoToVideoInfoTagDTO(junctionDTOs);
    }

    /**
     * Retrieves specific videos by IDs, paginated.
     */
    public List<VideoInfoTagDTO> getVideos(String[] videos, int skip, int limit) throws NoAuthenticatedYouTagUser, JunctionDTOHasNullValues, VideoDTOHasNullValues, VideoNotFound, VideoInfoTagDTOHasNullValues {
        String userId = authService.getCurrentUser().email();
        log.debug("Fetching specified videos for user: {}, Videos: {}, Skip: {}, Limit: {}", userId, videos, skip, limit);

        List<JunctionDTO> junctionDTOs = junctionService.getVideosOfUser(userId, Arrays.stream(videos).toList(), skip, limit);
        log.info("Fetched specified videos for user: {}. Total results: {}", userId, junctionDTOs.size());
        return junctionDtoToVideoInfoTagDTO(junctionDTOs);
    }

    /**
     * Converts a list of JunctionDTOs to a list of VideoInfoTagDTOs.
     */
    private List<VideoInfoTagDTO> junctionDtoToVideoInfoTagDTO(List<JunctionDTO> junctionDTO) throws VideoInfoTagDTOHasNullValues, VideoDTOHasNullValues, VideoNotFound {
        Map<String, VideoInfoTagDTO> videoToInfoMap = new HashMap<>();
        log.debug("Converting JunctionDTO list to VideoInfoTagDTO list. Total JunctionDTOs: {}", junctionDTO.size());

        for (var j : junctionDTO) {
            String videoId = j.getVideoId();
            String currentTag = j.getTag();
            log.debug("Processing video ID: {}, Tag: {}", videoId, currentTag);

            // Add or update the video entry in the map
            if (videoToInfoMap.containsKey(videoId)) {
                var existing = videoToInfoMap.get(videoId);
                if (!existing.getTags().contains(currentTag)) {
                    existing.getTags().add(currentTag);
                    log.debug("Added tag '{}' to existing video entry for video ID: {}", currentTag, videoId);
                }
            } else {
                var vidInfo = videoService.getVideo(videoId);
                videoToInfoMap.put(videoId, new VideoInfoTagDTO(vidInfo, List.of(currentTag)));
                log.debug("Created new video entry for video ID: {}", videoId);
            }
        }

        log.info("Conversion completed. Total unique videos processed: {}", videoToInfoMap.size());
        return videoToInfoMap.values().stream().toList();
    }
}
