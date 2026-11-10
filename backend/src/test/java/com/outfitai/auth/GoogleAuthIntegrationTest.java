package com.outfitai.auth;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.outfitai.user.User;
import com.outfitai.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GoogleAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository users;

    /** Replaces the real verifier, so no network call to Google happens. */
    @MockitoBean
    private GoogleTokenVerifier verifier;

    private ResultActions googleLogin() throws Exception {
        return mockMvc.perform(post("/api/auth/google")
                .contentType(MediaType.APPLICATION_JSON).content("{\"idToken\":\"fake-token\"}"));
    }

    @Test
    void firstSignInCreatesGoogleUserAndSecondSignInReusesIt() throws Exception {
        when(verifier.verify(anyString())).thenReturn(
                Optional.of(new GoogleIdentity("sub-1", "Gina@Example.com", true, "Gina G")));

        googleLogin().andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("gina@example.com"))
                .andExpect(jsonPath("$.user.displayName").value("Gina G"))
                .andExpect(jsonPath("$.user.authProvider").value("GOOGLE"));
        googleLogin().andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(users.count()).isEqualTo(1);
    }

    @Test
    void invalidTokenIsUnauthorized() throws Exception {
        when(verifier.verify(anyString())).thenReturn(Optional.empty());

        googleLogin().andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid Google token"));
    }

    @Test
    void unverifiedGoogleEmailIsRejected() throws Exception {
        when(verifier.verify(anyString())).thenReturn(
                Optional.of(new GoogleIdentity("sub-2", "x@example.com", false, "X")));

        googleLogin().andExpect(status().isUnauthorized());
    }

    @Test
    void emailAlreadyUsedByPasswordAccountIsConflict() throws Exception {
        users.saveAndFlush(User.local("taken@example.com", "hash", "Local"));
        when(verifier.verify(anyString())).thenReturn(
                Optional.of(new GoogleIdentity("sub-3", "taken@example.com", true, "Taken")));

        googleLogin().andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("password")));
    }

    @Test
    void missingTokenIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/google").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
