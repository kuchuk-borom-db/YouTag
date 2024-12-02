package dev.kuku.youtagserver.shared.infrastructure;

import com.nimbusds.jose.JOSEException;
import dev.kuku.youtagserver.auth.api.exceptions.InvalidOAuthRedirect;
import dev.kuku.youtagserver.auth.api.services.AuthService;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/api/public/auth")
@RequiredArgsConstructor
@Slf4j
public class PublicAuthController {
    private final AuthService authService;

    @GetMapping("/login/google")
    ResponseEntity<ResponseModel<String>> getGoogleLogin(
    ) {
        return ResponseEntity.ok(new ResponseModel<>(authService.getGoogleAuthorizationURL(), "Success"));
    }

    @GetMapping("/redirect/google")
    ResponseEntity<ResponseModel<String>> googleRedirectEndpoint(
            @RequestParam String code,
            @RequestParam String state
    ) throws InvalidOAuthRedirect, JOSEException {
        log.debug("Redirect google oauth with state {} and code {}", state, code);

        if (code == null || state == null) {
            throw new InvalidOAuthRedirect("Invalid Google OAuth redirect because code and/or state is null");
        }

        var user = authService.getUserFromGoogleToken(code, state);
        String token = authService.generateJwtTokenForUser(user.email(), new HashMap<>());

        return ResponseEntity.ok(new ResponseModel<>(token, ""));
    }
}
