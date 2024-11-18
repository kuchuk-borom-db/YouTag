package dev.kuku.youtagserver.shared.infrastructure;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.shared.models.VideoInfoTagDTO;
import dev.kuku.youtagserver.user_tag.api.dtos.TagDTO;
import dev.kuku.youtagserver.user_tag.api.services.TagService;
import dev.kuku.youtagserver.user_video.api.UserVideoService;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoAlreadyLinked;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1>Endpoints</h1>
 *
 * <h2>Auth</h2>
 * - Get google login link (public)
 * - Get jwt token from request token (public)
 * - Get user info
 *
 * <h2>Video</h2>
 * - Save video(s) (to user)
 * - Remove video(s) (from user)
 * - Get all videos of user
 * - Get all videos containing title "X" (of user) (For searching)
 *
 * <h2>Tags</h2>
 * - Add tag(s) to saved video(s) (of user)
 * - Remove tag(s) from video(s) (of user)
 * - Remove all tags from video(s) (of user)
 * - Remove tag(s) from All videos (of user)
 * - Get videos with tag(s) (of user)
 * - Get tags of video(s) (of user)
 * - Get tags containing "X" (of user)
 */

@Slf4j
@RequiredArgsConstructor
abstract class BaseController {
    protected final AuthService authService;

    protected String getCurrentUserId() throws NoAuthenticatedYouTagUser {
        return authService.getCurrentUser().email();
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
            @RequestParam(value = "tags", required = false) String tagRaw
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
            userVideoService.saveVideoToUser(userId, videoId);
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

    /**
     * Get all or tags containing certain words
     */
    @GetMapping("/")
    ResponseEntity<ResponseModel<List<String>>> getAllTagsOfUser(
            @RequestParam(required = false, defaultValue = "0") int skip,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, value = "containing") String containing
    ) throws NoAuthenticatedYouTagUser {
        log.debug("Getting all tags or containing {} of user skip {} and limit {}", containing, skip, limit);
        String userId = getCurrentUserId();
        List<TagDTO> tags;
        if (containing == null || containing.isEmpty()) {
            tags = tagService.getAllTagsOfUser(userId, skip, limit);
        } else {
            tags = tagService.getAllTagsOfUserContaining(userId, containing, skip, limit);
        }

        return ResponseEntity.ok(ResponseModel.build(tags.stream()
                .map(TagDTO::getTag)
                .toList(), null));
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
