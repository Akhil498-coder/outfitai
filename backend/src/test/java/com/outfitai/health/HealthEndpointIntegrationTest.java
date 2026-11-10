package com.outfitai.health;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

/** Full app on a random port with the real (H2) database. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthEndpointIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void healthReportsDatabaseUp() {
        HealthResponse body = rest.getForObject("/api/health", HealthResponse.class);

        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo("UP");
        assertThat(body.db()).isEqualTo("UP");
    }
}
