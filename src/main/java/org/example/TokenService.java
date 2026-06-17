package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.log4j.Log4j2;
import org.paseto4j.commons.PasetoException;
import org.paseto4j.commons.SecretKey;
import org.paseto4j.commons.Version;
import org.paseto4j.version4.Paseto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

@Log4j2
@Service
public class TokenService {

    @Value("${app.token.secret:#{null}}")
    String secret = "";

    @Value("${app.token.footer:#{null}}")
    String footer = "";

    public Optional<String> encrypt(AppToken token) {
        String payload;
        try {
            payload = mapper().writeValueAsString(token);
            return Optional.of(Paseto.encrypt(key(), payload, footer));
        }
        catch (PasetoException | JsonProcessingException e) {
            log.error("Failed to encode token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<AppToken> decrypt(String token) {
        try {
            String payload = Paseto.decrypt(key(), token, footer);
            AppToken appToken = mapper().readValue(payload, AppToken.class);
            if (Instant.now().isAfter(appToken.expiresAt())) {
                return Optional.empty();
            }
            return Optional.of(appToken);
        }
        catch (PasetoException | JsonProcessingException e) {
            log.error("Failed to decode token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private SecretKey key() {
        return new SecretKey(this.secret.getBytes(StandardCharsets.UTF_8), Version.V4);
    }

    private ObjectMapper mapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

}