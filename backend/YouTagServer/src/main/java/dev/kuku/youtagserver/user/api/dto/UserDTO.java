package dev.kuku.youtagserver.user.api.dto;

import java.time.LocalDateTime;

public record UserDTO(String email, String name, String pic, LocalDateTime created) {
}
