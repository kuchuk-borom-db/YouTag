package dev.kuku.youtagserver.user_video_tag.api.events;

import dev.kuku.youtagserver.user_video_tag.api.dto.UserVideoTagDTO;

import java.util.List;

public record DeletedUserVideoTag(List<UserVideoTagDTO> dto) {
}
