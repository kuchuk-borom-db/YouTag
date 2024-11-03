package dev.kuku.youtagserver.auth.api.services;

import dev.kuku.youtagserver.auth.domain.models.GoogleUser;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Service;

@Service
public interface GoogleOAuthService {

    OAuth2AuthorizationRequest getAuthorizationRequest();

    String getAuthorizationURL();

    OAuth2AccessToken getAccessToken(String code, String state);

    GoogleUser getUserFromToken(OAuth2AccessToken accessToken);
}
