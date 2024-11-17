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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
class EndpointController {
    private final UserService userService;
    private final AuthService authService;
    private final UserVideoService userVideoService;
    private final TagService tagService;
    private final VideoService videoService;
    private final YoutubeScrapperService scrapperService;

    String getCurrentUserId() throws NoAuthenticatedYouTagUser {
        return authService.getCurrentUser().email();
    }

    @RestController
    @RequestMapping("/public")
    class PublicEndpointController {

        /**
         * Get login url. <br>
         * Get jwt token from refresh token. <br>
         */
        @RestController
        @RequestMapping("/auth")
        class PublicAuthController {

            @GetMapping("/login/google")
            ResponseEntity<ResponseModel<String>> getGoogleLogin() {
                return ResponseEntity.ok(new ResponseModel<>(authService.getGoogleAuthorizationURL(), "Success"));
            }

            @GetMapping("/redirect/google")
            ResponseEntity<ResponseModel<String>> googleRedirectEndpoint(@RequestParam(required = false) String code, @RequestParam(required = false) String state) throws InvalidOAuthRedirect, JOSEException {
                log.debug("Redirect google oauth with state {} and code {}", state, code);

                if (code == null || state == null) {
                    throw new InvalidOAuthRedirect("Invalid Google OAuth redirect because code and/or state is null");
                }

                var user = authService.getUserFromGoogleToken(code, state);
                String token = authService.generateJwtTokenForUser(user.email(), new HashMap<>());

                return ResponseEntity.ok(new ResponseModel<>(token, ""));
            }


        }
    }

    @RestController
    @RequestMapping("/authenticated")
    class AuthenticatedEndpointController {

        /**
         * Get authenticated user's info <br>
         */
        @RestController
        @RequestMapping("/auth")
        class AuthenticatedAuthController {

            /// Get authenticated user info
            @GetMapping("/user")
            ResponseEntity<ResponseModel<UserDTO>> getUserInfo() throws UserDTOHasNullValues, EmailNotFound, NoAuthenticatedYouTagUser {
                return ResponseEntity.ok(ResponseModel.build(userService.getUser(getCurrentUserId()), ""));
            }
        }

        /**
         * Get videos of a user. <br>
         * Get video of a user by ID. <br>
         * Delete video of a user by ID. <br>
         * Delete all videos of a user <br>
         */
        @RestController
        @RequestMapping("/video")
        class VideoController {
            /// Get videos of user
            @GetMapping("/")
            ResponseEntity<ResponseModel<List<VideoInfoTagDTO>>> getVideosOfUser(@RequestParam(value = "skip", defaultValue = "0") int skip, @RequestParam(value = "limit", defaultValue = "10") int limit) throws NoAuthenticatedYouTagUser {
                String userId = getCurrentUserId();
                List<String> videoIds = userVideoService.getVideosOfUser(userId, skip, limit).stream().map(UserVideoDTO::videoId).toList();
                return ResponseEntity.ok(ResponseModel.build(createVideoInfoTagDTO(videoIds), null));
            }

            /// Get video of user by id
            @GetMapping("/{id}")
            ResponseEntity<ResponseModel<VideoInfoTagDTO>> getVideosOfUser(@PathVariable String id) throws NoAuthenticatedYouTagUser {
                String userId = getCurrentUserId();
                userVideoService.isVideoLinkedWithUser(userId, id); //Checking if the video is linked to user. Will throw exception if not linked
                return ResponseEntity.ok(ResponseModel.build(createVideoInfoTagDTO(id), null));
            }

            /// Delete video of user by ID
            @DeleteMapping("/{id}")
            ResponseEntity<ResponseModel<Object>> deleteVideoOfUser(@PathVariable String id) throws NoAuthenticatedYouTagUser, UserVideoNotFound {
                String userId = getCurrentUserId();
                userVideoService.unlinkVideoFromUser(userId, id);
                return ResponseEntity.ok(null);
            }

            /// Delete all videos of user
            @DeleteMapping("/")
            ResponseEntity<Object> deleteAllVideosOfUser() throws NoAuthenticatedYouTagUser {
                String userId = getCurrentUserId();
                userVideoService.unlinkAllVideosFromUser(userId);
                return ResponseEntity.ok(null);
            }
        }

        /**
         * Add tag(s) to video. If video is missing it needs to be created. <br>
         * Delete tag(s) from all or specific video. <br>
         * Get tags of user with pagination.<br>
         * get tags of video. <br>
         */
        @RestController
        @RequestMapping("/video-tag")
        class VideoTagController {

            /**
             * Adds tag(s) to a video.
             * <p>
             * If the video is not linked to the user then it will be linked first.
             * <p>
             * If video doesn't exist in record it will be added to record first.
             */
            @PostMapping("/{videoId}")
            ResponseEntity<Object> addTagsToVideo(
                    @PathVariable String videoId,
                    @RequestParam(required = false) String tagRaw
            ) throws InvalidVideoId, VideoAlreadyExists, NoAuthenticatedYouTagUser {
                log.debug("Adding tags {} to video {}", tagRaw, videoId);
                String userId = getCurrentUserId();
                //Check if video exists
                try {
                    videoService.getVideoInfo(videoId);
                } catch (VideoNotFound e) {
                    //Video doesn't exist in record. Adding it.
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

                //Add tags
                List<String> tagsToAdd = Arrays.stream(tagRaw.split(",")).toList();
                tagService.addTagsToVideo(userId, videoId, tagsToAdd);
                return ResponseEntity.ok(null);
            }

            /**
             * Delete tags from specific videos or all videos
             */
            @DeleteMapping("/")
            ResponseEntity<ResponseModel<List<VideoInfoTagDTO>>> deleteTagsAndOrVideos(
                    @RequestParam(value = "tags", required = false) String tagsRaw,
                    @RequestParam(value = "videos", required = false) String videosRaw
            ) throws NoAuthenticatedYouTagUser {
                log.debug("Delete videos triggered with tags {}, videos {}", tagsRaw, videosRaw);
                if ((tagsRaw == null || tagsRaw.isEmpty()) && (videosRaw == null || videosRaw.isEmpty())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseModel.build(null, "tags and/or videos query parameters are missing"));
                }
                String userId = getCurrentUserId();
                if (tagsRaw != null && tagsRaw.split(",").length > 0) {

                    //If no videos are passed. Get all videos with the given tag
                    if (videosRaw == null || videosRaw.split(",").length == 0) {
                        tagService.DeleteTagsFromAllVideosOfUser(userId, Arrays.stream(tagsRaw.split(",")).toList());
                    } else {

                        //If videos are passed. Delete tags from the given videos
                        tagService.deleteTagsFromVideosOfUser(userId, Arrays.stream(tagsRaw.split(",")).toList(), Arrays.stream(videosRaw.split(",")).toList());
                    }
                    return ResponseEntity.ok(null);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseModel.build(null, "tags query missing"));
                }
            }

            /**
             * Get all tags of user
             */
            @GetMapping("/")
            ResponseEntity<ResponseModel<List<String>>> getAllTagsOfUser(
                    @RequestParam(required = false, defaultValue = "0") int skip,
                    @RequestParam(required = false, defaultValue = "10") int limit
            ) throws NoAuthenticatedYouTagUser {
                log.debug("Getting all tags of user skip {} and limit {}", skip, limit);
                String userId = getCurrentUserId();
                List<String> tags = tagService.getAllTagsOfUser(userId, skip, limit).stream().map(TagDTO::getTag).collect(Collectors.toList());
                return ResponseEntity.ok(ResponseModel.build(tags, null));
            }

            /**
             * Get tags of specified video ID of user
             */
            @GetMapping("/{videoId}")
            ResponseEntity<ResponseModel<List<String>>> getTagsOfVideoOfUser(
                    @PathVariable String videoId
            ) throws NoAuthenticatedYouTagUser {
                log.debug("Getting tags of video {} of current user", videoId);
                String userId = getCurrentUserId();
                List<String> tags = tagService.getTagsOfVideo(userId, videoId).stream().map(TagDTO::getTag).collect(Collectors.toList());
                return ResponseEntity.ok(ResponseModel.build(tags, null));
            }
        }

        @RestController
        @RequestMapping("/search")
        class SearchController {

            /**
             * Get videos with given tags
             */
            @GetMapping("/")
            ResponseEntity<ResponseModel<List<VideoInfoTagDTO>>> search(
                    @RequestParam(value = "tags") String tagsRaw,
                    @RequestParam(value = "skip", defaultValue = "0") int skip,
                    @RequestParam(value = "limit", defaultValue = "10") int limit
            ) throws NoAuthenticatedYouTagUser {
                log.debug("Getting all videos with tag {}", tagsRaw);
                if (tagsRaw == null || tagsRaw.isEmpty() || tagsRaw.split(",").length == 0) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseModel.build(null, "tags query missing"));
                }
                String userId = getCurrentUserId();
                List<String> videoIds = tagService.getVideosWithTag(userId, Arrays.stream(tagsRaw.split(",")).toList(), skip, limit).stream().map(TagDTO::getVideoId).collect(Collectors.toList());
                List<VideoInfoTagDTO> videoInfos = createVideoInfoTagDTO(videoIds);
                return ResponseEntity.ok(ResponseModel.build(videoInfos, null));
            }
        }
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
        List<String> tags = tagService.getTagsOfVideo(userId, videoId).stream().map(TagDTO::getTag).toList();
        return new VideoInfoTagDTO(videoInfo, tags);
    }
}