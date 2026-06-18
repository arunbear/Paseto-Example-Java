package org.example;

import lombok.Builder;

import java.time.Instant;

@Builder
public record AppToken(String userId, String role, Instant expiresAt) {

    public AppToken {
        if (expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Cannot create an expired token");
        }
    }
}
