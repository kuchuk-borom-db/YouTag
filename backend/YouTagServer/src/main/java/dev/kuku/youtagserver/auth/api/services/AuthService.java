package dev.kuku.youtagserver.auth.api.services;

import com.nimbusds.jose.JOSEException;
import dev.kuku.youtagserver.auth.api.dto.YouTagUserDTO;
import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.dto.GoogleUserDTO;

import java.util.Map;

public interface AuthService {
    YouTagUserDTO getCurrentUser() throws NoAuthenticatedYouTagUser;

    String getGoogleAuthorizationURL();

    GoogleUserDTO getUserFromGoogleToken(String code, String state);

    String generateJwtTokenForUser(String email, Map<String, String> claims) throws JOSEException;
}
