package dev.kuku.youtagserver.auth.application;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dev.kuku.youtagserver.auth.api.services.JwtService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

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
}
