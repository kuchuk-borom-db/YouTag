package dev.kuku.youtagserver.auth.services.api;

import com.nimbusds.jose.JOSEException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface JwtService {
    String generateJwtToken(String subject, Map<String, String> claims) throws JOSEException;
}
