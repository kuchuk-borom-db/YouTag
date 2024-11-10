package dev.kuku.youtagserver.auth.infrastructure;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import dev.kuku.youtagserver.auth.api.exceptions.JWTVerificationFailed;
import dev.kuku.youtagserver.auth.api.exceptions.JwtTokenExpired;
import dev.kuku.youtagserver.auth.application.JwtService;
import dev.kuku.youtagserver.auth.domain.models.AuthenticatedUser;
import dev.kuku.youtagserver.auth.domain.models.JwtAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.text.ParseException;

//TODO Checking if user is in repo or not

@Slf4j
record JwtAuthenticationProvider(JwtService jwtService) implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
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
