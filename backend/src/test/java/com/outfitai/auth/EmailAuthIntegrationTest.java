package com.outfitai.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import com.outfitai.user.User;
import com.outfitai.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** End-to-end through HTTP, security, service and the real (H2) database. Each test rolls back. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmailAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository users;

    private ResultActions send(String url, String json) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String register(String email, String password) {
        return """
                {"email":"%s","password":"%s","displayName":"Alex"}""".formatted(email, password);
    }

    private static String login(String email, String password) {
        return """
                {"email":"%s","password":"%s"}""".formatted(email, password);
    }

    @Test
    void registerReturnsTokenAndUserWithoutPassword() throws Exception {
        send("/api/auth/register", register("New@Example.com", "password123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("new@example.com")) // normalised
                .andExpect(jsonPath("$.user.authProvider").value("LOCAL"))
                .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void duplicateEmailIsConflict() throws Exception {
        send("/api/auth/register", register("dup@example.com", "password123")).andExpect(status().isCreated());

        send("/api/auth/register", register("DUP@example.com", "password123"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void emailOfGoogleAccountCannotRegisterWithPassword() throws Exception {
        users.saveAndFlush(User.google("g@example.com", "sub-123", "Gina"));

        send("/api/auth/register", register("g@example.com", "password123"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Google")));
    }

    @Test
    void invalidInputIsBadRequest() throws Exception {
        send("/api/auth/register", register("not-an-email", "short"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("email")))
                .andExpect(jsonPath("$.message").value(containsString("password")));
        send("/api/auth/register", "not json").andExpect(status().isBadRequest());
    }

    @Test
    void loginSucceedsWithCorrectPassword() throws Exception {
        send("/api/auth/register", register("l@example.com", "password123")).andExpect(status().isCreated());

        send("/api/auth/login", login("L@example.com", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("l@example.com"));
    }

    @Test
    void wrongPasswordUnknownEmailAndGoogleAccountAllLookIdentical() throws Exception {
        send("/api/auth/register", register("w@example.com", "password123")).andExpect(status().isCreated());
        users.saveAndFlush(User.google("g2@example.com", "sub-456", "Gina"));

        for (String body : new String[] {
                login("w@example.com", "wrong-password"),
                login("nobody@example.com", "password123"),
                login("g2@example.com", "password123")}) {
            send("/api/auth/login", body)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid email or password"));
        }
    }
}
