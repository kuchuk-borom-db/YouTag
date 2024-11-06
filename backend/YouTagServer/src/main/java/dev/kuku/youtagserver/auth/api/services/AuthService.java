package dev.kuku.youtagserver.auth.api.services;

import dev.kuku.youtagserver.auth.api.dto.YouTagUserDTO;
import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;

public interface AuthService {
YouTagUserDTO getCurrentUser() throws NoAuthenticatedYouTagUser;
}
