package dev.kuku.youtagserver.shared.infrastructure;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;
import dev.kuku.youtagserver.user.api.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/authenticated/auth")
public class AuthenticateAuthController {

    private final UserService userService;

    private final AuthService authService;


    @GetMapping("/user")
    ResponseEntity<ResponseModel<UserDTO>> getUserInfo() throws UserDTOHasNullValues, EmailNotFound, NoAuthenticatedYouTagUser {
        String userId = authService.getCurrentUser().email();
        log.debug("Getting user info {}", userId);
        //Define cache control to allow client to cache the response until max age
        long userAge = authService.getMaxAgeOfCurrentUser();
        return ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=" + userAge)
                .body(ResponseModel.build(userService.getUser(userId), ""));

    }
}
