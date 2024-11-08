package dev.kuku.youtagserver.auth.application;

import dev.kuku.youtagserver.auth.api.dto.YouTagUserDTO;
import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.auth.domain.models.AuthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public YouTagUserDTO getCurrentUser() throws NoAuthenticatedYouTagUser {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Current auth = {}", auth);
        var user = (AuthenticatedUser) auth;
        if (user == null) {
            throw new NoAuthenticatedYouTagUser();
        }
        return new YouTagUserDTO(user.email());
    }
}
