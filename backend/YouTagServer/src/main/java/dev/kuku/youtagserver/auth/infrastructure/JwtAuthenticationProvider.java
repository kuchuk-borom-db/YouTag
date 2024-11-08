package dev.kuku.youtagserver.auth.infrastructure;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import dev.kuku.youtagserver.auth.api.exceptions.JWTVerificationFailed;
import dev.kuku.youtagserver.auth.api.exceptions.JwtTokenExpired;
import dev.kuku.youtagserver.auth.application.JwtService;
import dev.kuku.youtagserver.auth.domain.models.AuthenticatedUser;
import dev.kuku.youtagserver.auth.domain.models.JwtAuthenticationToken;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;
import dev.kuku.youtagserver.user.api.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.text.ParseException;

//TODO Checking if user is in repo or not

@Slf4j
record JwtAuthenticationProvider(JwtService jwtService, UserService userService) implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String token = jwtAuthenticationToken.getCredentials();
            try {
                JWTClaimsSet claims = jwtService.extractClaims(token);
                String emailID = claims.getSubject();

                //Check if the user exists in repo
                userService.getUser(emailID);
                log.info("Authenticated user : {}", emailID);
                var authenticatedUser = new AuthenticatedUser(emailID);
                SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
                return authenticatedUser;
            } catch (ParseException | JWTVerificationFailed | JOSEException | JwtTokenExpired | EmailNotFound |
                     UserDTOHasNullValues e) {
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
