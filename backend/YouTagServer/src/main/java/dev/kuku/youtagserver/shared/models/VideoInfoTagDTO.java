package dev.kuku.youtagserver.shared.models;

import dev.kuku.youtagserver.video.api.dto.VideoDTO;

public record VideoInfoTagDTO(VideoDTO videoDTO, String[] tags) {
}
