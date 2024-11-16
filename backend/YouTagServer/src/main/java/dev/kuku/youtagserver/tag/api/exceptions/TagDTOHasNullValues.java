package dev.kuku.youtagserver.tag.api.exceptions;

import dev.kuku.youtagserver.tag.api.dtos.TagDTO;
import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class TagDTOHasNullValues extends ResponseException {
    public TagDTOHasNullValues(TagDTO tagDTO) {
        super(HttpStatus.BAD_REQUEST, String.format("Junction has null values : %s", tagDTO.toString()));
    }
}
