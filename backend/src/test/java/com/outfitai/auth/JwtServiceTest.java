package com.outfitai.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-1234";

    private final JwtService service = new JwtService(SECRET, Duration.ofHours(1));

    @Test
    void roundTripReturnsUserId() {
        String token = service.generate(42L, "a@example.com");

        assertThat(service.parseUserId(token)).contains(42L);
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService shortLived = new JwtService(SECRET, Duration.ofSeconds(-5));

        assertThat(shortLived.parseUserId(shortLived.generate(1L, "a@example.com"))).isEmpty();
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = service.generate(1L, "a@example.com");
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

        assertThat(service.parseUserId(tampered)).isEmpty();
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtService other = new JwtService("another-secret-another-secret-123456", Duration.ofHours(1));

        assertThat(service.parseUserId(other.generate(1L, "a@example.com"))).isEmpty();
    }

    @Test
    void garbageIsRejected() {
        assertThat(service.parseUserId("not-a-jwt")).isEmpty();
    }

    @Test
    void shortSecretFailsFast() {
        assertThatThrownBy(() -> new JwtService("too-short", Duration.ofHours(1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
