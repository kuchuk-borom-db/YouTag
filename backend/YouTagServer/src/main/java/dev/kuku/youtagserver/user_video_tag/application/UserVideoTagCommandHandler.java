package dev.kuku.youtagserver.user_video_tag.application;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import dev.kuku.youtagserver.shared.models.VideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.dto.UserVideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.services.UserVideoTagService;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserVideoTagCommandHandler {
    final AuthService authService;
    final UserVideoTagService userVideoTagService;
    final VideoService videoService;

    public void addTagsToVideo(String videoId, String[] tags) throws NoAuthenticatedYouTagUser, VideoNotFound {
        String userId = authService.getCurrentUser().email();
        //Check if the video is valid and stored in database. If not found it will throw an exception
        videoService.getVideo(videoId);
        log.info("Adding tags to video {} -> {}", videoId, tags);
        userVideoTagService.addTagsToVid(userId, videoId, Arrays.stream(tags).toList());
    }

    public void removeTagsFromVideo(String videoId, String[] tags) throws NoAuthenticatedYouTagUser, VideoNotFound {
        String userId = authService.getCurrentUser().email();
        //Check if the video is valid and stored in database. If not found it will throw an exception
        videoService.getVideo(videoId);
        log.info("Removing tags from video {} -> {}", videoId, tags);
        userVideoTagService.deleteWithUserIdAndVideoIdAndTagIn(userId, videoId, Arrays.stream(tags).toList());

    }

    public void removeTagsFromAllVideosOfUser(String[] tags) throws ResponseException {
        String userId = authService.getCurrentUser().email();
        log.info("Removing tags from all videos of user {} -> {}", userId, Arrays.asList(tags));
        userVideoTagService.deleteWithUserIdAndTag(userId, tags);
    }

    public List<VideoTagDTO> getVideosOfUser() throws NoAuthenticatedYouTagUser {
        String userId = authService.getCurrentUser().email();
        List<UserVideoTagDTO> userVideoTags = userVideoTagService.getWithUserId(userId);
        return createVideoTagDTOs(userVideoTags);
    }

    public List<VideoTagDTO> getVideosOfUserWithTags(String[] tags) throws NoAuthenticatedYouTagUser {
        String userId = authService.getCurrentUser().email();
        List<UserVideoTagDTO> userVideoTags = userVideoTagService.getWithUserIdAndTags(userId, tags);
        return createVideoTagDTOs(userVideoTags);
    }

    private List<VideoTagDTO> createVideoTagDTOs(List<UserVideoTagDTO> userVideoTags) {
        Map<String, VideoTagDTO> videoIdToVideoTagDTO = new HashMap<>();

        for (UserVideoTagDTO userVideoTag : userVideoTags) {
            String videoId = userVideoTag.videoId();
            String currentTag = userVideoTag.tag();

            // If key not found, create new DTO with empty tags
            if (!videoIdToVideoTagDTO.containsKey(videoId)) {
                try {
                    VideoDTO videoInfo = videoService.getVideo(videoId);
                    videoIdToVideoTagDTO.put(videoId, new VideoTagDTO(videoInfo, new String[0]));
                } catch (VideoNotFound e) {
                    log.info("video not found {}", e.getMessage());
                    continue;
                }
            }

            List<String> currentTags = new ArrayList<>(Arrays.asList(videoIdToVideoTagDTO.get(videoId).tags()));
            if (!currentTags.contains(currentTag)) {
                currentTags.add(currentTag);
            }

            VideoDTO videoInfo = videoIdToVideoTagDTO.get(videoId).videoDTO();
            videoIdToVideoTagDTO.put(videoId, new VideoTagDTO(videoInfo, currentTags.toArray(new String[0])));
        }

        return new ArrayList<>(videoIdToVideoTagDTO.values());
    }

    public List<String> getTagsOfUser() throws NoAuthenticatedYouTagUser {
        String userId = authService.getCurrentUser().email();
        var dtos = userVideoTagService.getWithUserId(userId);
        Map<String, Object> map = new HashMap<>();
        for (var d : dtos) {
            if (map.containsKey(d.tag())) {
                continue;
            }
            map.put(d.tag(), null);
        }
        return map.keySet().stream().toList();
    }
}
