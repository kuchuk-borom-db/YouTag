package dev.kuku.youtagserver.auth.application;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import dev.kuku.youtagserver.auth.api.exceptions.JWTVerificationFailed;
import dev.kuku.youtagserver.auth.api.exceptions.JwtTokenExpired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Map;

@Service
public interface JwtService {
    String generateJwtToken(String subject, Map<String, String> claims) throws JOSEException;

    JWTClaimsSet extractClaims(String token) throws ParseException, JWTVerificationFailed, JOSEException, JwtTokenExpired;
}
