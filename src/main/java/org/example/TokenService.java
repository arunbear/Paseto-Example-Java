package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;
import org.paseto4j.commons.SecretKey;
import org.paseto4j.commons.Version;
import org.paseto4j.version4.Paseto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Log4j2
@Service
public class TokenService {

    @Value("${app.token.secret:#{null}}")
    String secret = "";

    @Value("${app.token.footer:#{null}}")
    String footer = "";

    public Try<String> encrypt(AppToken token) {
        return Try.of(() -> {
            String payload = mapper().writeValueAsString(token);
            return Paseto.encrypt(key(), payload, footer);
        });
    }

    public Try<AppToken> decrypt(String token) {
        return Try.of(() -> {
            String payload = Paseto.decrypt(key(), token, footer);
            return mapper().readValue(payload, AppToken.class);
        });
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