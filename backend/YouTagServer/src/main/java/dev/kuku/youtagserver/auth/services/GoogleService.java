package dev.kuku.youtagserver.auth.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleService {
    final ClientRegistrationRepository registeredClientRepo;
    final DefaultOAuth2UserService oAuth2UserService = new DefaultOAuth2UserService();

    public OAuth2AuthorizationRequest getAuthorizationRequest() {
        ClientRegistration googleClient = registeredClientRepo.findByRegistrationId("google");
        return OAuth2AuthorizationRequest.authorizationCode()
                .clientId(googleClient.getClientId())
                .authorizationUri(googleClient.getProviderDetails().getAuthorizationUri())
                .scope(String.join(" ", googleClient.getScopes()))
                .redirectUri(googleClient.getRedirectUri())
                .state(UUID.randomUUID().toString())
                .build();
    }

    public String getAuthorizationURL() {
        log.info("Authorization URL : {}", getAuthorizationRequest().getAuthorizationRequestUri());
        return getAuthorizationRequest().getAuthorizationRequestUri();
    }

    public OAuth2AccessToken getAccessToken(String code, String state) {
        // Get the original authorization request
        OAuth2AuthorizationRequest authorizationRequest = getAuthorizationRequest();
        // Create the authorization response with the code
        OAuth2AuthorizationResponse auth2AuthorizationResponse = OAuth2AuthorizationResponse
                .success(code)
                .redirectUri(authorizationRequest.getRedirectUri())
                .state(state)
                .build();
        // Create the authorization exchange
        OAuth2AuthorizationExchange auth2AuthorizationExchange = new OAuth2AuthorizationExchange(authorizationRequest, auth2AuthorizationResponse);
        // Create the token grant request
        OAuth2AuthorizationCodeGrantRequest tokenRequest = new OAuth2AuthorizationCodeGrantRequest(registeredClientRepo.findByRegistrationId("google"), auth2AuthorizationExchange);
        // Exchange the authorization code for an access token
        OAuth2AccessTokenResponse auth2AccessTokenResponse = new DefaultAuthorizationCodeTokenResponseClient().getTokenResponse(tokenRequest);
        return auth2AccessTokenResponse.getAccessToken();
    }

    public OAuth2User getUserFromToken(OAuth2AccessToken accessToken) {
        ClientRegistration google = registeredClientRepo.findByRegistrationId("google");
        OAuth2UserRequest userRequest = new OAuth2UserRequest(google, accessToken);
        return oAuth2UserService.loadUser(userRequest);
    }
}
