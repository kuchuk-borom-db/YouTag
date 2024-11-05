package dev.kuku.youtagserver.video.api.dto;

import java.time.LocalDateTime;

public record VideoDTO(String id, String title, String description, String thumbnail, LocalDateTime updated) {
}
