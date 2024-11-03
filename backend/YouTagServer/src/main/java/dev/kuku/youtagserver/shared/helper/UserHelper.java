package dev.kuku.youtagserver.shared.helper;

import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.services.UserService;
import dev.kuku.youtagserver.user.domain.exception.InvalidEmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserHelper {
    final UserService userService;

    /**
     * @return currently logged-in user's email
     */
    public UserDTO getCurrentUserDTO() throws InvalidEmailException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return userService.getUser(auth.getName());
    }
}
