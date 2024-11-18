package dev.kuku.youtagserver.shared.infrastructure;

import com.nimbusds.jose.JOSEException;
import dev.kuku.youtagserver.auth.api.exceptions.InvalidOAuthRedirect;
import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.shared.models.VideoInfoTagDTO;
import dev.kuku.youtagserver.tag.api.dtos.TagDTO;
import dev.kuku.youtagserver.tag.api.services.TagService;
import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;
import dev.kuku.youtagserver.user.api.services.UserService;
import dev.kuku.youtagserver.user_video.api.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.UserVideoService;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoNotFound;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import dev.kuku.youtagserver.webscraper.api.dto.YoutubeVideoInfoDto;
import dev.kuku.youtagserver.webscraper.api.exceptions.InvalidVideoId;
import dev.kuku.youtagserver.webscraper.api.services.YoutubeScrapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
abstract class BaseController {
    protected final AuthService authService;

    protected String getCurrentUserId() throws NoAuthenticatedYouTagUser {
        return authService.getCurrentUser().email();
    }
}

@RestController
@RequestMapping("/api/public/auth")
@RequiredArgsConstructor
@Slf4j
class PublicAuthController {
    private final AuthService authService;

    @GetMapping("/login/google")
    ResponseEntity<ResponseModel<String>> getGoogleLogin() {
        return ResponseEntity.ok(new ResponseModel<>(authService.getGoogleAuthorizationURL(), "Success"));
    }

    @GetMapping("/redirect/google")
    ResponseEntity<ResponseModel<String>> googleRedirectEndpoint(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state
    ) throws InvalidOAuthRedirect, JOSEException {
        log.debug("Redirect google oauth with state {} and code {}", state, code);

        if (code == null || state == null) {
            throw new InvalidOAuthRedirect("Invalid Google OAuth redirect because code and/or state is null");
        }

        var user = authService.getUserFromGoogleToken(code, state);
        String token = authService.generateJwtTokenForUser(user.email(), new HashMap<>());

        return ResponseEntity.ok(new ResponseModel<>(token, ""));
    }
}

@RestController
@RequestMapping("/api/authenticated/auth")
@Slf4j
class AuthenticatedAuthController extends BaseController {
    private final UserService userService;

    AuthenticatedAuthController(AuthService authService, UserService userService) {
        super(authService);
        this.userService = userService;
    }

    @GetMapping("/user")
    ResponseEntity<ResponseModel<UserDTO>> getUserInfo() throws UserDTOHasNullValues, EmailNotFound, NoAuthenticatedYouTagUser {
        String userId = getCurrentUserId();
        log.debug("Getting user info {}", userId);
        return ResponseEntity.ok(ResponseModel.build(userService.getUser(userId), ""));
    }
}

@RestController
@RequestMapping("/api/authenticated/video")
@Slf4j
class VideoController extends BaseController {
    private final UserVideoService userVideoService;
    private final VideoHelperService videoHelperService;

    VideoController(
            AuthService authService,
            UserVideoService userVideoService,
            VideoHelperService videoHelperService
    ) {
        super(authService);
        this.userVideoService = userVideoService;
        this.videoHelperService = videoHelperService;
    }

    @GetMapping("/")
    ResponseEntity<ResponseModel<List<VideoInfoTagDTO>>> getVideosOfUser(
            @RequestParam(value = "skip", defaultValue = "0") int skip,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) throws NoAuthenticatedYouTagUser {
        String userId = getCurrentUserId();
        List<String> videoIds = userVideoService.getVideosOfUser(userId, skip, limit)
                .stream()
                .map(UserVideoDTO::videoId)
                .toList();
        return ResponseEntity.ok(ResponseModel.build(videoHelperService.createVideoInfoTagDTO(videoIds), null));
    }

    @GetMapping("/{id}")
    ResponseEntity<ResponseModel<VideoInfoTagDTO>> getVideosOfUser(@PathVariable String id) throws NoAuthenticatedYouTagUser {
        String userId = getCurrentUserId();
        userVideoService.isVideoLinkedWithUser(userId, id);
        return ResponseEntity.ok(ResponseModel.build(videoHelperService.createVideoInfoTagDTO(id), null));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ResponseModel<Object>> deleteVideoOfUser(@PathVariable String id) throws NoAuthenticatedYouTagUser, UserVideoNotFound {
        String userId = getCurrentUserId();
        userVideoService.unlinkVideoFromUser(userId, id);
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/")
    ResponseEntity<Object> deleteAllVideosOfUser() throws NoAuthenticatedYouTagUser {
        String userId = getCurrentUserId();
        userVideoService.unlinkAllVideosFromUser(userId);
        return ResponseEntity.ok(null);
    }
}

@RestController
@RequestMapping("/api/authenticated/video-tag")
@Slf4j
class VideoTagController extends BaseController {
    private final UserVideoService userVideoService;
    private final TagService tagService;
    private final VideoService videoService;
    private final YoutubeScrapperService scrapperService;

    VideoTagController(
            AuthService authService,
            UserVideoService userVideoService,
            TagService tagService,
            VideoService videoService,
            YoutubeScrapperService scrapperService
    ) {
        super(authService);
        this.userVideoService = userVideoService;
        this.tagService = tagService;
        this.videoService = videoService;
        this.scrapperService = scrapperService;
    }

    @PostMapping("/{videoId}")
    ResponseEntity<Object> addTagsToVideo(
            @PathVariable String videoId,
            @RequestParam(required = false) String tagRaw
    ) throws InvalidVideoId, VideoAlreadyExists, NoAuthenticatedYouTagUser {
        log.debug("Adding tags {} to video {}", tagRaw, videoId);
        String userId = getCurrentUserId();

        try {
            videoService.getVideoInfo(videoId);
        } catch (VideoNotFound e) {
            log.debug("Video not found in video table. Adding it {}", videoId);
            YoutubeVideoInfoDto videoInfoDto = scrapperService.getYoutubeVideoInfo(videoId);
            videoService.addVideo(new VideoDTO(videoId, videoInfoDto.title(), videoInfoDto.description(), videoInfoDto.thumbnail()));
        }

        try {
            userVideoService.linkVideoToUser(userId, videoId);
        } catch (UserVideoAlreadyLinked e) {
            log.debug("Video already linked.");
        }

        if (tagRaw == null || tagRaw.split(",").length == 0) {
            return ResponseEntity.ok(null);
        }

        List<String> tagsToAdd = Arrays.stream(tagRaw.split(",")).toList();
        tagService.addTagsToVideo(userId, videoId, tagsToAdd);
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/")
    ResponseEntity<ResponseModel<List<VideoInfoTagDTO>>> deleteTagsAndOrVideos(
            @RequestParam(value = "tags", required = false) String tagsRaw,
            @RequestParam(value = "videos", required = false) String videosRaw
    ) throws NoAuthenticatedYouTagUser {
        log.debug("Delete videos triggered with tags {}, videos {}", tagsRaw, videosRaw);
        if ((tagsRaw == null || tagsRaw.isEmpty()) && (videosRaw == null || videosRaw.isEmpty())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseModel.build(null, "tags and/or videos query parameters are missing"));
        }

        String userId = getCurrentUserId();
        if (tagsRaw != null && tagsRaw.split(",").length > 0) {
            if (videosRaw == null || videosRaw.split(",").length == 0) {
                tagService.DeleteTagsFromAllVideosOfUser(userId, Arrays.stream(tagsRaw.split(",")).toList());
            } else {
                tagService.deleteTagsFromVideosOfUser(
                        userId,
                        Arrays.stream(tagsRaw.split(",")).toList(),
                        Arrays.stream(videosRaw.split(",")).toList()
                );
            }
            return ResponseEntity.ok(null);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseModel.build(null, "tags query missing"));
        }
    }

    @GetMapping("/")
    ResponseEntity<ResponseModel<List<String>>> getAllTagsOfUser(
            @RequestParam(required = false, defaultValue = "0") int skip,
            @RequestParam(required = false, defaultValue = "10") int limit
    ) throws NoAuthenticatedYouTagUser {
        log.debug("Getting all tags of user skip {} and limit {}", skip, limit);
        String userId = getCurrentUserId();
        List<String> tags = tagService.getAllTagsOfUser(userId, skip, limit)
                .stream()
                .map(TagDTO::getTag)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ResponseModel.build(tags, null));
    }

    @GetMapping("/{videoId}")
    ResponseEntity<ResponseModel<List<String>>> getTagsOfVideoOfUser(
            @PathVariable String videoId
    ) throws NoAuthenticatedYouTagUser {
        log.debug("Getting tags of video {} of current user", videoId);
        String userId = getCurrentUserId();
        List<String> tags = tagService.getTagsOfVideo(userId, videoId)
                .stream()
                .map(TagDTO::getTag)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ResponseModel.build(tags, null));
    }
}

@RestController
@RequestMapping("/api/authenticated/search")
@Slf4j
class SearchController extends BaseController {
    private final TagService tagService;
    private final VideoHelperService videoHelperService;

    SearchController(
            AuthService authService,
            TagService tagService,
            VideoHelperService videoHelperService
    ) {
        super(authService);
        this.tagService = tagService;
        this.videoHelperService = videoHelperService;
    }

    @GetMapping("/")
    ResponseEntity<ResponseModel<List<VideoInfoTagDTO>>> search(
            @RequestParam(value = "tags") String tagsRaw,
            @RequestParam(value = "skip", defaultValue = "0") int skip,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) throws NoAuthenticatedYouTagUser {
        log.debug("Getting all videos with tag {}", tagsRaw);
        if (tagsRaw == null || tagsRaw.isEmpty() || tagsRaw.split(",").length == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseModel.build(null, "tags query missing"));
        }

        String userId = getCurrentUserId();
        List<String> videoIds = tagService.getVideosWithTag(userId, Arrays.stream(tagsRaw.split(",")).toList(), skip, limit)
                .stream()
                .map(TagDTO::getVideoId)
                .collect(Collectors.toList());

        List<VideoInfoTagDTO> videoInfos = videoHelperService.createVideoInfoTagDTO(videoIds);
        return ResponseEntity.ok(ResponseModel.build(videoInfos, null));
    }
}

@Service
@Slf4j
class VideoHelperService extends BaseController {
    private final VideoService videoService;
    private final TagService tagService;

    VideoHelperService(AuthService authService, VideoService videoService, TagService tagService) {
        super(authService);
        this.videoService = videoService;
        this.tagService = tagService;
    }

    List<VideoInfoTagDTO> createVideoInfoTagDTO(List<String> videoIds) throws NoAuthenticatedYouTagUser {
        List<VideoInfoTagDTO> videoInfoTagDTOs = new ArrayList<>();
        for (String videoId : videoIds) {
            var videoInfo = createVideoInfoTagDTO(videoId);
            if (videoInfo != null) {
                videoInfoTagDTOs.add(videoInfo);
            }
        }
        return videoInfoTagDTOs;
    }

    VideoInfoTagDTO createVideoInfoTagDTO(String videoId) throws NoAuthenticatedYouTagUser {
        String userId = getCurrentUserId();
        VideoDTO videoInfo;
        try {
            videoInfo = videoService.getVideoInfo(videoId);
        } catch (VideoNotFound _) {
            log.warn("Video {} not found", videoId);
            return null;
        }
        List<String> tags = tagService.getTagsOfVideo(userId, videoId)
                .stream()
                .map(TagDTO::getTag)
                .toList();
        return new VideoInfoTagDTO(videoInfo, tags);
    }
}