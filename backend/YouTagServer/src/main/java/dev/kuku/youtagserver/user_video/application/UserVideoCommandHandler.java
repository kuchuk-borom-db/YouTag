package dev.kuku.youtagserver.user_video.application;

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
import dev.kuku.youtagserver.user_video.api.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.UserVideoService;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.services.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserVideoCommandHandler {
    final UserVideoService userVideoService;
    final VideoService videoService;
    final AuthService authService;
    final TagService tagService;
    final UserService userService;

    public List<VideoInfoTagDTO> getVideosOfUser(int skip, int limit) throws NoAuthenticatedYouTagUser, TagDTOHasNullValues, VideoInfoTagDTOHasNullValues, UserDTOHasNullValues, EmailNotFound {
        String email = authService.getCurrentUser().email();
        userService.getUser(email);
        log.debug("getVideosOfUser");
        List<UserVideoDTO> userVideoDTOs = userVideoService.getVideosOfUser(email, skip, limit);
        List<VideoDTO> videoDTOS = videoService.getVideoInfos(userVideoDTOs.stream().map(UserVideoDTO::videoId).toList());
        List<VideoInfoTagDTO> videoInfoTagDTOS = new ArrayList<>();

        //Get tags for each video and add them to videoInfoTags
        for (var v : videoDTOS) {
            List<String> tags = tagService.getAllJunctionOfUser(email, 0, 999999).stream().map(TagDTO::getTag).toList();
            videoInfoTagDTOS.add(new VideoInfoTagDTO(v, tags));
        }
        return videoInfoTagDTOS;
    }

}
