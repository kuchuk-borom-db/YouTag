package dev.kuku.youtagserver.auth.infrastructure;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import dev.kuku.youtagserver.auth.application.JwtService;
import dev.kuku.youtagserver.auth.domain.exceptions.JWTVerificationFailed;
import dev.kuku.youtagserver.auth.domain.exceptions.JwtTokenExpired;
import dev.kuku.youtagserver.auth.domain.models.AuthenticatedUser;
import dev.kuku.youtagserver.auth.domain.models.JwtAuthenticationToken;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.text.ParseException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {
    final JwtService jwtService;
    final UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("JWTAuthenticationProvider in action");
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String token = jwtAuthenticationToken.getCredentials();
            try {
                JWTClaimsSet claims = jwtService.extractClaims(token);
                String emailID = claims.getSubject();
                try {
                    userService.getUser(emailID);
                } catch (EmailNotFound e) {
                    log.error("Email from claims {} not found in db", emailID);
                    return null;
                }
                log.info("Authenticated user : {}", emailID);
                return new AuthenticatedUser(emailID);
            } catch (ParseException | JWTVerificationFailed | JOSEException | JwtTokenExpired e) {
                log.error(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.getTypeName().equals(JwtAuthenticationToken.class.getName());
    }
}
