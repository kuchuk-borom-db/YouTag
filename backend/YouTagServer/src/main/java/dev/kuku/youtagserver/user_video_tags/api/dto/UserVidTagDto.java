package dev.kuku.youtagserver.user_video_tags.api.dto;

import java.util.List;

public record UserVidTagDto(String user_email, String video_id, List<String> tags) {
}
