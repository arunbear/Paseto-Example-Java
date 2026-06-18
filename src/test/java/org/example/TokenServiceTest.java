package org.example;

import io.vavr.control.Try;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Log
@SpringBootTest
class TestTokenService {

    @Autowired
    TokenService tokenService;

    @Test
    void testGoodToken() {
        final String userId = "1234";
        final String role = "USER";
        final Instant expiresDate = Instant.now().plus(5, ChronoUnit.MINUTES);

        AppToken appToken = AppToken.builder().userId(userId).role(role).expiresAt(expiresDate).build();

        Try<String> encrypted = tokenService.encrypt(appToken);
        Assertions.assertTrue(encrypted.isSuccess());
        String token = encrypted.get();
        Assertions.assertNotNull(token);
        log.info(token);

        Optional<AppToken> optAppToken = tokenService.decrypt(token);
        Assertions.assertTrue(optAppToken.isPresent());
        AppToken decodedAppToken = optAppToken.get();

        Assertions.assertNotNull(decodedAppToken);
        Assertions.assertEquals(userId, decodedAppToken.userId());
        Assertions.assertEquals(role, decodedAppToken.role());
        Assertions.assertEquals(expiresDate, decodedAppToken.expiresAt());
    }

    @Test
    void testBadToken() {
        String fakeToken = "v3.local.mu4W-Il_eEMmGFt5Pe5uJrB3Vq3o4XjrdMeUp0grHqf48GgjN_KevFtHwJCEdbTUdiWhL_lQ-B1Qjsl2arf9TRdqw35bwGJgiPn9OAXezvFRhifmRZOTlZB9H_1u-luEzu5Y4SZCcmWtYDKgCt8jUv5KePUBkfWoKtsMmYgoXlSjqIv0bgxEUHG0kYkDUjXwpIc.UE9DLVBBU0VUTw";
        Optional<AppToken> optAppToken = tokenService.decrypt(fakeToken);
        Assertions.assertTrue(optAppToken.isEmpty());
    }

}