package dev.kuku.youtagserver.user_video.api.events;

import dev.kuku.youtagserver.user_video.domain.UserVideo;

import java.util.List;

public record VideosSavedToUser(String userId, List<UserVideo> missingVideos) {
}
