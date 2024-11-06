package dev.kuku.youtagserver.auth.application;

import dev.kuku.youtagserver.auth.api.dto.YouTagUserDTO;
import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.auth.domain.models.AuthenticatedUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public YouTagUserDTO getCurrentUser() throws NoAuthenticatedYouTagUser {
        var user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        if(user == null) {
            throw new NoAuthenticatedYouTagUser();
        }
        return new YouTagUserDTO(user.email());
    }
}
