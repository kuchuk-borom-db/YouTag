package dev.kuku.youtagserver.auth.application;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dev.kuku.youtagserver.auth.api.exceptions.JWTVerificationFailed;
import dev.kuku.youtagserver.auth.api.exceptions.JwtTokenExpired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {
    //TODO Secret from                n                                                                                 e
    String secret = "kuchuk borom is the king fd sf fg g gh fgh hrga f s f sdf ad";

    @Override
    public String generateJwtToken(String subject, Map<String, String> claims) throws JOSEException {
        JWSSigner signer = new MACSigner(secret.getBytes());
        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .subject(subject)
                .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7));
        for (var kv : claims.entrySet()) {
            claimsBuilder.claim(kv.getKey(), kv.getValue());
        }
        var signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsBuilder.build());
        signedJwt.sign(signer);
        return signedJwt.serialize();
    }

    @Override
    public JWTClaimsSet extractClaims(String token) throws ParseException, JWTVerificationFailed, JOSEException, JwtTokenExpired {
        log.info("Attempting to extract claims from token {}", token);
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier jwsVerifier = new MACVerifier(secret.getBytes());
        if (!signedJWT.verify(jwsVerifier)) {
            throw new JWTVerificationFailed("Invalid JWT");
        }
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        Date expirationTime = claimsSet.getExpirationTime();
        if (expirationTime == null || new Date().after(expirationTime)) {
            throw new JwtTokenExpired();
        }
        return claimsSet;
    }
}
