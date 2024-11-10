package dev.kuku.youtagserver.user.application;

import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;
import dev.kuku.youtagserver.user.api.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandHandler {
    final UserService userService;
    final AuthService authService;

    public UserDTO getUserInfo() throws NoAuthenticatedYouTagUser, UserDTOHasNullValues, EmailNotFound {
        String userId = authService.getCurrentUser().email();
        log.debug("Getting user info {}", userId);
        return userService.getUser(userId);
    }
}
