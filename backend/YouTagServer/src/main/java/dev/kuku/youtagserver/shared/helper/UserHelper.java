package dev.kuku.youtagserver.shared.helper;

import dev.kuku.youtagserver.shared.exceptions.AuthenticatedUserNotFound;
import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserHelper {
    final UserService userService;

    /**
     * @return currently logged-in user's email
     */
    public UserDTO getCurrentUserDTO() throws EmailNotFound, AuthenticatedUserNotFound {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AuthenticatedUserNotFound();
        return userService.getUser(auth.getName());

    }
}