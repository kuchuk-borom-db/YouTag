package dev.kuku.youtagserver.auth.application;

import com.nimbusds.jose.JOSEException;
import dev.kuku.youtagserver.auth.api.dto.GoogleUserDTO;
import dev.kuku.youtagserver.auth.api.dto.YouTagUserDTO;
import dev.kuku.youtagserver.auth.api.exceptions.NoAuthenticatedYouTagUser;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.auth.domain.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    final GoogleOAuthService googleOAuthService;
    final JwtService jwtService;

    private OAuth2AccessToken getGoogleAccessToken(String code, String state) {
        return googleOAuthService.getAccessToken(code, state);
    }

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

    public long getMaxAgeOfCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var user = (AuthenticatedUser) auth;
        return user.maxAge();
    }


    @Override
    public String getGoogleAuthorizationURL() {
        return googleOAuthService.getAuthorizationURL();
    }

    @Override
    public GoogleUserDTO getUserFromGoogleToken(String code, String state) {

        var googleUser = googleOAuthService.getUserFromToken(getGoogleAccessToken(code, state));
        return new GoogleUserDTO(googleUser.email(), googleUser.name(), googleUser.pictureUrl());
    }

    @Override
    public String generateJwtTokenForUser(String email, Map<String, String> claims) throws JOSEException {
        return jwtService.generateJwtToken(email, claims);
    }
}
