package dev.kuku.youtagserver.shared.infrastructure;

import com.nimbusds.jose.JOSEException;
import dev.kuku.youtagserver.auth.api.exceptions.InvalidOAuthRedirect;
import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.auth.application.GoogleOAuthService;
import dev.kuku.youtagserver.auth.application.JwtService;
import dev.kuku.youtagserver.junction.api.dtos.TagDTO;
import dev.kuku.youtagserver.junction.api.services.TagService;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.shared.models.VideoInfoTagDTO;
import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;
import dev.kuku.youtagserver.user.api.services.UserService;
import dev.kuku.youtagserver.user_video.api.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.UserVideoService;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoNotFound;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.VideoDTOHasNullValues;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
class EndpointController {
    private final GoogleOAuthService googleOAuthService;
    private final JwtService jwtService;
    private final UserService userService;
    private final AuthService authService;
    private final UserVideoService userVideoService;
    private final TagService tagService;
    private final VideoService videoService;

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
        class AuthController {

            @GetMapping("/login/google")
            ResponseEntity<ResponseModel<String>> getGoogleLogin() {
                return ResponseEntity.ok(new ResponseModel<>(googleOAuthService.getAuthorizationURL(), "Success"));
            }

            @GetMapping("/redirect/google")
            ResponseEntity<ResponseModel<String>> googleRedirectEndpoint(@RequestParam(required = false) String code, @RequestParam(required = false) String state) throws InvalidOAuthRedirect, JOSEException {
                log.debug("Redirect google oauth with state {} and code {}", state, code);

                if (code == null || state == null) {
                    throw new InvalidOAuthRedirect("Invalid Google OAuth redirect because code and/or state is null");
                }

                OAuth2AccessToken accessToken = googleOAuthService.getAccessToken(code, state);
                var user = googleOAuthService.getUserFromToken(accessToken);
                String token = jwtService.generateJwtToken(user.email(), new HashMap<>());

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
        class AuthController {

            /// Get authenticated user info
            @GetMapping("/user")
            ResponseEntity<ResponseModel<UserDTO>> getUserInfo() throws UserDTOHasNullValues, EmailNotFound, NoAuthenticatedYouTagUser {
                return ResponseEntity.ok(ResponseModel.build(userService.getUser(getCurrentUserId()), ""));
            }
        }

        /**
         * Get videos of a user. <br>
         * Get video of a user by Id. <br>
         * Delete video of a user by id. <br>
         * Delete videos of a user <br>
         */
        @RestController
        @RequestMapping("/video")
        class VideoController {
            /// Get videos of user
            @GetMapping("/")
            ResponseEntity<ResponseModel<List<VideoInfoTagDTO>>> getVideosOfUser(@RequestParam(value = "skip", defaultValue = "0") int skip, @RequestParam(value = "limit", defaultValue = "10") int limit) throws NoAuthenticatedYouTagUser, VideoDTOHasNullValues, VideoNotFound {
                String userId = getCurrentUserId();
                List<String> videoIds = userVideoService.getVideosOfUser(userId, skip, limit).stream().map(UserVideoDTO::videoId).toList();
                return ResponseEntity.ok(ResponseModel.build(createVideoInfoTagDTO(videoIds), null));
            }

            /// Get video of user by id
            @GetMapping("/{id}")
            ResponseEntity<ResponseModel<VideoInfoTagDTO>> getVideosOfUser(@PathVariable String id) throws NoAuthenticatedYouTagUser, UserVideoNotFound, VideoDTOHasNullValues, VideoNotFound {
                String userId = getCurrentUserId();
                userVideoService.getVideoOfUser(userId, id); //Checking if the video is linked to user. Will throw exception if not linked
                return ResponseEntity.ok(ResponseModel.build(createVideoInfoTagDTO(id), null));
            }

            /// Delete video of user by Id
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
    }

    List<VideoInfoTagDTO> createVideoInfoTagDTO(List<String> videoIds) throws NoAuthenticatedYouTagUser, VideoDTOHasNullValues, VideoNotFound {
        List<VideoInfoTagDTO> videoInfoTagDTOs = new ArrayList<>();
        for (String videoId : videoIds) {
            videoInfoTagDTOs.add(createVideoInfoTagDTO(videoId));
        }
        return videoInfoTagDTOs;
    }

    VideoInfoTagDTO createVideoInfoTagDTO(String videoId) throws VideoDTOHasNullValues, VideoNotFound, NoAuthenticatedYouTagUser {
        String userId = getCurrentUserId();
        VideoDTO videoInfo = videoService.getVideoInfo(videoId);
        List<String> tags = tagService.getTagsOfVideo(userId, videoId).stream().map(TagDTO::getTag).toList();
        return new VideoInfoTagDTO(videoInfo, tags);
    }
}