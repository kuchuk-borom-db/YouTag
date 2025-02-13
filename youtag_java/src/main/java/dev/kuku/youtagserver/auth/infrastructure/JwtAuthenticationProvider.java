package dev.kuku.youtagserver.auth.infrastructure;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import dev.kuku.youtagserver.auth.api.exceptions.JWTVerificationFailed;
import dev.kuku.youtagserver.auth.api.exceptions.JwtTokenExpired;
import dev.kuku.youtagserver.auth.application.JwtService;
import dev.kuku.youtagserver.auth.domain.AuthenticatedUser;
import dev.kuku.youtagserver.auth.domain.JwtAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.text.ParseException;

//TODO Checking if user is in repo or not

@Slf4j
@RequiredArgsConstructor
@Component
class JwtAuthenticationProvider implements AuthenticationProvider {
    final JwtService jwtService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            throw new AuthenticationServiceException("Authentication type not supported");
        }

        String token = jwtAuthenticationToken.getCredentials();
        JWTClaimsSet claims;

        try {
            claims = jwtService.extractClaims(token);
        } catch (JWTVerificationFailed | ParseException | JOSEException | JwtTokenExpired e) {
            throw new JWTVerificationFailed(e.getMessage());
        }

        String emailID = claims.getSubject();
        log.info("Authenticated user: {}", emailID);
        return new AuthenticatedUser(emailID, claims.getExpirationTime().toInstant().getEpochSecond());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.getTypeName().equals(JwtAuthenticationToken.class.getName());
    }
}
