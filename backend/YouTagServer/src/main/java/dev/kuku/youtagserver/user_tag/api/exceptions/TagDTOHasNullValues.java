package dev.kuku.youtagserver.user_tag.api.exceptions;

import dev.kuku.youtagserver.user_tag.api.dtos.UserTagDTO;
import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class TagDTOHasNullValues extends ResponseException {
    public TagDTOHasNullValues(UserTagDTO userTagDTO) {
        super(HttpStatus.BAD_REQUEST, String.format("Junction has null values : %s", userTagDTO.toString()));
    }
}
