package dev.kuku.youtagserver.auth.infrastructure;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import dev.kuku.youtagserver.auth.api.services.JwtService;
import dev.kuku.youtagserver.auth.domain.exceptions.JWTVerificationFailed;
import dev.kuku.youtagserver.auth.domain.exceptions.JwtTokenExpired;
import dev.kuku.youtagserver.auth.domain.models.AuthenticatedUser;
import dev.kuku.youtagserver.auth.domain.models.JwtAuthenticationToken;
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

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("JWTAuthenticationProvider in action");
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String token = jwtAuthenticationToken.getCredentials();
            try {
                JWTClaimsSet claims = jwtService.extractClaims(token);
                String emailID = claims.getSubject();
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
