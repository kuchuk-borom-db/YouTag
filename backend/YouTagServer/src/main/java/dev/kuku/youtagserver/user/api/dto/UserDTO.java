package dev.kuku.youtagserver.user.api.dto;

import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserDTO {

    // Getters
    private final String email;
    private final String name;
    private final String pic;
    private final LocalDateTime created;

    // Constructor
    public UserDTO(String email, String name, String pic, LocalDateTime created) throws UserDTOHasNullValues {
        if (email == null || email.isEmpty()) {
            throw new UserDTOHasNullValues(this);
        }
        if (name == null || name.isEmpty()) {
            throw new UserDTOHasNullValues(this);
        }
        if (created == null) {
            throw new UserDTOHasNullValues(this);
        }

        if (pic == null || pic.isEmpty()) {
            throw new UserDTOHasNullValues(this);
        }

        this.email = email;
        this.name = name;
        this.pic = pic;
        this.created = created;
    }

}
