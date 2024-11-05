package dev.kuku.youtagserver.user_video.api.dto;

import java.time.LocalDateTime;

public record UserVideoDTO(String userId, String videoId, LocalDateTime created) {
}
