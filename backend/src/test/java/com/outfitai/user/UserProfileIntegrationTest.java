package com.outfitai.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.outfitai.auth.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JwtService jwtService;

    private String auth;

    /** Register a user through the real endpoint and keep its bearer token. */
    @BeforeEach
    void registerUser() throws Exception {
        String body = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"me@example.com","password":"password123","displayName":"Me"}"""))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = mapper.readTree(body);
        auth = "Bearer " + json.get("token").asText();
    }

    private ResultActions update(String json) throws Exception {
        return mockMvc.perform(put("/api/users/me").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void meRequiresToken() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsCurrentUser() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@example.com"))
                .andExpect(jsonPath("$.displayName").value("Me"))
                .andExpect(jsonPath("$.age").doesNotExist());
    }

    @Test
    void updateSetsAndThenClearsOptionalFields() throws Exception {
        update("""
                {"displayName":" Alex ","age":29,"gender":"OTHER"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Alex"))
                .andExpect(jsonPath("$.age").value(29))
                .andExpect(jsonPath("$.gender").value("OTHER"));
        mockMvc.perform(get("/api/users/me").header("Authorization", auth))
                .andExpect(jsonPath("$.gender").value("OTHER"));

        update("""
                {"displayName":"Alex"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.age").doesNotExist())
                .andExpect(jsonPath("$.gender").doesNotExist());
    }

    @Test
    void invalidProfileValuesAreBadRequest() throws Exception {
        update("""
                {"displayName":"A","age":5}""").andExpect(status().isBadRequest());
        update("""
                {"displayName":"A","age":200}""").andExpect(status().isBadRequest());
        update("""
                {"displayName":"A","gender":"ROBOT"}""").andExpect(status().isBadRequest());
        update("""
                {"displayName":"  "}""").andExpect(status().isBadRequest());
    }

    @Test
    void tokenOfNonexistentUserIsUnauthorized() throws Exception {
        String ghost = "Bearer " + jwtService.generate(999_999L, "ghost@example.com");

        mockMvc.perform(get("/api/users/me").header("Authorization", ghost))
                .andExpect(status().isUnauthorized());
    }
}
