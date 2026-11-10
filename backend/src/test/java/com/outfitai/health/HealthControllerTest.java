package com.outfitai.health;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.outfitai.auth.JwtService;
import com.outfitai.config.SecurityConfig;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Web-layer slice test: only the controller is loaded, the service is mocked. */
@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class) // the slice skips @Configuration classes, so bring our security rules in
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthService healthService;

    @MockitoBean
    private JwtService jwtService; // needed by SecurityConfig's filter

    @Test
    void returnsHealthJson() throws Exception {
        when(healthService.check()).thenReturn(new HealthResponse("UP", "UP", Instant.parse("2026-01-01T00:00:00Z")));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.db").value("UP"))
                .andExpect(jsonPath("$.time").exists());
    }
}
