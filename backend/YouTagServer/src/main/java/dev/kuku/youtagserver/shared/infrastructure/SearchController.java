package dev.kuku.youtagserver.shared.infrastructure;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.user_tag.api.UserTagService;
import dev.kuku.youtagserver.user_video.api.UserVideoService;
import dev.kuku.youtagserver.user_video_tag.api.UserVideoTagService;
import dev.kuku.youtagserver.video.api.services.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authenticated/search")
@RequiredArgsConstructor
public class SearchController {
    final AuthService authService;
    final VideoService videoService;
    final UserTagService userTagService;
    final UserVideoService userVideoService;
    final UserVideoTagService userVideoTagService;

    /**
     * Get tags containing keyword
     */
    @GetMapping("/tag/{keyword}")
    ResponseEntity<ResponseModel<List<String>>> getTagsContaining(
            @PathVariable() String keyword,
            @RequestParam(value = "skip", defaultValue = "0") int skip,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) throws NoAuthenticatedYouTagUser {
        String userId = authService.getCurrentUser().email();
        List<String> tags = userTagService.getTagsOfUserContaining(userId, keyword, skip, limit);
        return ResponseEntity.ok(ResponseModel.build(tags, null));
    }

    /**
     * Get videos containing keyword
     */
    @GetMapping("/video/{keyword}")
    ResponseEntity<ResponseModel<List<String>>> getVideosContaining(
            @PathVariable() String keyword,
            @RequestParam(value = "skip", defaultValue = "0") int skip,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) throws NoAuthenticatedYouTagUser {
        String userId = authService.getCurrentUser().email();
        List<String> videoIds = userVideoService.getSavedVideosOfUserContaining(userId, keyword, skip, limit);
        return ResponseEntity.ok(ResponseModel.build(videoIds, null));
    }
}
//TODO Adding tags should all be lowercase and trimmed.
//TODO ADd to readme