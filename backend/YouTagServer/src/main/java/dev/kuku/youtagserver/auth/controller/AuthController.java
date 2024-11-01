package dev.kuku.youtagserver.auth.controller;

import dev.kuku.youtagserver.auth.exceptions.InvalidOAuthRedirect;
import dev.kuku.youtagserver.auth.services.GoogleService;
import dev.kuku.youtagserver.common.models.ResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/auth")
@RequiredArgsConstructor
class AuthController {
    final GoogleService googleService;

    @GetMapping("/login/google")
    ResponseEntity<ResponseModel<String>> getGoogleLogin() {
        return ResponseEntity.ok(new ResponseModel<>(googleService.getAuthorizationURL(), "Success"));
    }

    @GetMapping("/redirect/google")
    ResponseEntity<ResponseModel<Map<String, Object>>> googleRedirectEndpoint(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state) throws InvalidOAuthRedirect {

        if (code == null || state == null) {
            throw new InvalidOAuthRedirect("Invalid Google OAuth redirect because code and/or state is null");
        }

        OAuth2AccessToken accessToken = googleService.getAccessToken(code, state);
        var user = googleService.getUserFromToken(accessToken);
        return ResponseEntity.ok(new ResponseModel<>(user.getAttributes(), "Success"));
    }
}
