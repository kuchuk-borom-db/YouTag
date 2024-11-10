package dev.kuku.youtagserver.user.infrastructure;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;
import dev.kuku.youtagserver.user.application.UserCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/authenticated/user")
@RequiredArgsConstructor

public class UserController {
    final UserCommandHandler userCommandHandler;

    @GetMapping("/")
    ResponseEntity<ResponseModel<UserDTO>> getUserInfo() throws UserDTOHasNullValues, EmailNotFound, NoAuthenticatedYouTagUser {
        return ResponseEntity.ok(ResponseModel.build(userCommandHandler.getUserInfo(), ""));
    }
}
