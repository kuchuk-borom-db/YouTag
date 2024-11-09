package dev.kuku.youtagserver.auth.infrastructure;

import com.nimbusds.jose.JOSEException;
import dev.kuku.youtagserver.auth.api.exceptions.InvalidOAuthRedirect;
import dev.kuku.youtagserver.auth.application.GoogleOAuthService;
import dev.kuku.youtagserver.auth.application.JwtService;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Slf4j
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
        log.debug("Redirect google oauth with state {} and code {}", state, code);
        if (code == null || state == null) {
            throw new InvalidOAuthRedirect("Invalid Google OAuth redirect because code and/or state is null");
        }
        OAuth2AccessToken accessToken = googleOAuthService.getAccessToken(code, state);
        var user = googleOAuthService.getUserFromToken(accessToken);
        String token = jwtService.generateJwtToken(user.email(), new HashMap<>());
        return ResponseEntity.ok(new ResponseModel<>(token, ""));
    }
}
