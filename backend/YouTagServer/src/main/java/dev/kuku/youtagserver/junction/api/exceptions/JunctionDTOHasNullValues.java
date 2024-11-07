package dev.kuku.youtagserver.junction.api.exceptions;

import dev.kuku.youtagserver.junction.api.dtos.JunctionDTO;
import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class JunctionDTOHasNullValues extends ResponseException {
    public JunctionDTOHasNullValues(JunctionDTO junctionDTO) {
        super(HttpStatus.BAD_REQUEST, String.format("Junction has null values : %s", junctionDTO.toString()));
    }
}
