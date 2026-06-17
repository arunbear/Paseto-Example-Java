package org.example;

import lombok.Builder;

import java.time.Instant;

@Builder
public record AppToken(String userId, String role, Instant expiresAt) {
}
