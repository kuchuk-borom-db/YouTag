package dev.kuku.youtagserver.user_video.api.events;

import java.util.List;

public record DeleteSpecificVideosFromAllUsers(List<String> affectedUsers,
                                               List<String> videoIds) {
}
