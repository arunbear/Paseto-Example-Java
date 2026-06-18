package org.example;

import io.vavr.control.Try;
import lombok.extern.java.Log;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.BDDAssertions.*;

@Log
@SpringBootTest
class TestTokenService {

    @Autowired
    TokenService tokenService;

    @Test
    void a_good_token_can_be_encrypted_and_decrypted() {
        // given
        final String userId = "1234";
        final String role = "USER";
        final Instant expiresDate = Instant.now().plus(5, ChronoUnit.MINUTES);

        AppToken appToken = AppToken.builder()
            .userId(userId)
            .role(role)
            .expiresAt(expiresDate)
            .build();

        // when
        Try<String> encrypted = tokenService.encrypt(appToken);
        then(encrypted.isSuccess()).isTrue();

        // when
        String token = encrypted.get();
        then(token).isNotNull();
        log.info(token);

        // when
        Try<AppToken> decrypted = tokenService.decrypt(token);
        then(decrypted.isSuccess()).isTrue();

        // when
        AppToken decodedAppToken = decrypted.get();
        then(decodedAppToken).isNotNull();
        then(userId).isEqualTo(decodedAppToken.userId());
        then(role).isEqualTo(decodedAppToken.role());
        then(expiresDate).isEqualTo(decodedAppToken.expiresAt());
    }

    @Test
    void testBadToken() {
        String fakeToken = "v3.local.mu4W-Il_eEMmGFt5Pe5uJrB3Vq3o4XjrdMeUp0grHqf48GgjN_KevFtHwJCEdbTUdiWhL_lQ-B1Qjsl2arf9TRdqw35bwGJgiPn9OAXezvFRhifmRZOTlZB9H_1u-luEzu5Y4SZCcmWtYDKgCt8jUv5KePUBkfWoKtsMmYgoXlSjqIv0bgxEUHG0kYkDUjXwpIc.UE9DLVBBU0VUTw";
        Try<AppToken> optAppToken = tokenService.decrypt(fakeToken);
        Assertions.assertTrue(optAppToken.isEmpty());
    }

}