package dev.kuku.youtagserver.junction.application;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.junction.api.dtos.TagDTO;
import dev.kuku.youtagserver.junction.api.exceptions.TagDTOHasNullValues;
import dev.kuku.youtagserver.junction.api.services.TagService;
import dev.kuku.youtagserver.shared.exceptions.VideoInfoTagDTOHasNullValues;
import dev.kuku.youtagserver.shared.models.VideoInfoTagDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;
import dev.kuku.youtagserver.user.api.services.UserService;
import dev.kuku.youtagserver.user_video.api.UserVideoService;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoAlreadyLinked;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
import dev.kuku.youtagserver.video.api.exceptions.VideoDTOHasNullValues;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import dev.kuku.youtagserver.webscraper.api.dto.YoutubeVideoInfoDto;
import dev.kuku.youtagserver.webscraper.api.exceptions.InvalidVideoId;
import dev.kuku.youtagserver.webscraper.api.services.YoutubeScrapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j // For logging
@Service
@RequiredArgsConstructor
public class CommandHandler {
    private final TagService tagService;
    private final AuthService authService;
    private final VideoService videoService;
    final UserService userService;
    final UserVideoService userVideoService;
    final YoutubeScrapperService scrapperService;

    /**
     * 1. Add videos to video table if it doesn't exist
     * 2. Add entry in user_video if it doesn't exist
     * 3. Add entries in tags
     */
    public void addTagsToVideo(String videoId, List<String> tags) throws UserDTOHasNullValues, EmailNotFound, NoAuthenticatedYouTagUser, InvalidVideoId, VideoDTOHasNullValues {
        //Validation
        String userId = validateAndGetUser();
        log.debug("Adding videos with tags for user: {}, Video: {}, Tags: {}", userId, videoId, tags);

        //Get video info
        YoutubeVideoInfoDto videoInfoDto = scrapperService.getYoutubeVideoInfo(videoId);
        //Add video to video table if it doesn't exist
        try {
            videoService.addVideo(new VideoDTO(videoId, videoInfoDto.title(), videoInfoDto.description(), videoInfoDto.thumbnail()));
        } catch (VideoAlreadyExists _) {
            log.debug("Existing video {} found.", videoId);
        }
        //Add entry to user_video table if it doesn't exist
        try {
            userVideoService.linkVideoToUser(userId, videoId);
        } catch (UserVideoAlreadyLinked _) {
            log.debug("Existing link between user {} and video {} found.", userId, videoId);
        }
        // Adds the specified videos with the provided tags.
        tagService.addTagsToVideo(userId, videoId, tags);
        log.debug("Successfully added videos with tags {} for user: {}", tags, userId);
    }

    /**
     * Converts a list of JunctionDTOs to a list of VideoInfoTagDTOs.
     */
    private List<VideoInfoTagDTO> tagDTOSToVideoInfoTagDTO(List<TagDTO> tagDTO) throws VideoInfoTagDTOHasNullValues, VideoDTOHasNullValues, VideoNotFound, TagDTOHasNullValues {
        Map<String, VideoInfoTagDTO> map = new HashMap<>();
        log.debug("Converting JunctionDTO list to VideoInfoTagDTO list. Total JunctionDTOs: {}", tagDTO.size());

        for (var j : tagDTO) {
            String videoId = j.getVideoId();
            log.debug("Processing video ID: {}", videoId);

            // Add or update the video entry in the map
            if (!map.containsKey(videoId)) {
                var vidInfo = videoService.getVideoInfo(videoId);
                //Get all entries where videoId match. Basically getting all tags
                var tags = tagService.getVideosOfUser(j.getUserId(), List.of(j.getVideoId()), 0, 100).stream().map(TagDTO::getTag).toList();
                map.put(videoId, new VideoInfoTagDTO(vidInfo, tags));
                log.debug("Created new video entry for video ID: {}", videoId);
            }
        }

        log.info("Conversion completed. Total unique videos processed: {}", map.size());
        return map.values().stream().toList();
    }

    private String validateAndGetUser() throws NoAuthenticatedYouTagUser, UserDTOHasNullValues, EmailNotFound {
        //Basic validation
        String userId = authService.getCurrentUser().email();
        userService.getUser(userId);
    }
}
