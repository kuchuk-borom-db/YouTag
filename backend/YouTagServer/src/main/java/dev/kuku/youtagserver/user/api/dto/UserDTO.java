package dev.kuku.youtagserver.user.api.dto;

import java.time.LocalDateTime;

/**
 * @param email Getters
 */
public record UserDTO(String email, String name, String pic, LocalDateTime created) {

}
