package dev.kuku.youtagserver.auth.controllers.internal;

import com.nimbusds.jose.JOSEException;
import dev.kuku.youtagserver.auth.services.api.GoogleOAuthService;
import dev.kuku.youtagserver.auth.services.api.JwtService;
import dev.kuku.youtagserver.auth.exceptions.internal.InvalidOAuthRedirect;
import dev.kuku.youtagserver.common.models.ResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/api/public/auth")
@RequiredArgsConstructor
class AuthController {
    final GoogleOAuthService googleOAuthService;
    final JwtService jwtService;

    @GetMapping("/login/google")
    ResponseEntity<ResponseModel<String>> getGoogleLogin() {
        return ResponseEntity.ok(new ResponseModel<>(googleOAuthService.getAuthorizationURL(), "Success"));
    }

    @GetMapping("/redirect/google")
    ResponseEntity<ResponseModel<String>> googleRedirectEndpoint(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state) throws InvalidOAuthRedirect, JOSEException {
        if (code == null || state == null) {
            throw new InvalidOAuthRedirect("Invalid Google OAuth redirect because code and/or state is null");
        }
        OAuth2AccessToken accessToken = googleOAuthService.getAccessToken(code, state);
        var user = googleOAuthService.getUserFromToken(accessToken);
        String token = jwtService.generateJwtToken(user.email(), new HashMap<>());
        return ResponseEntity.ok(new ResponseModel<>(token, ""));
    }
}
