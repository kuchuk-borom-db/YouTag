package dev.kuku.youtagserver.user_video.api.events;

import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;

import java.util.List;

public record DeletedUserVideo(List<UserVideoDTO> userVideoDTO) {

}
