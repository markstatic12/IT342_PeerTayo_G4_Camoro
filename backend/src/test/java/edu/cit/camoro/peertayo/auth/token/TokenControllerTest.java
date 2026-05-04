package edu.cit.camoro.peertayo.auth.token;

import edu.cit.camoro.peertayo.TestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Token / Session API")
class TokenControllerTest {

    @Autowired MockMvc mockMvc;

    // Each test registers its own unique user to avoid token blacklist collisions
    // between tests (TokenBlacklistService is in-memory and survives @Transactional rollback)

    @Test
    @DisplayName("GET /me — returns current user")
    void getMe_success() throws Exception {
        String token = TestHelper.registerAndGetToken(mockMvc, "me1@test.com", "pass123");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.email").value("me1@test.com"));
    }

    @Test
    @DisplayName("POST /refresh — returns fresh token with current roles")
    void refresh_success() throws Exception {
        String token = TestHelper.registerAndGetToken(mockMvc, "refresh1@test.com", "pass123");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.roles[0]").value("RESPONDENT"));
    }

    @Test
    @DisplayName("POST /promote-to-facilitator — adds FACILITATOR role and returns new token")
    void promote_success() throws Exception {
        String token = TestHelper.registerAndGetToken(mockMvc, "promote1@test.com", "pass123");

        mockMvc.perform(post("/api/v1/auth/promote-to-facilitator")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.roles").isArray())
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /logout — blacklists token, subsequent /me returns 401")
    void logout_blacklistsToken() throws Exception {
        String token = TestHelper.registerAndGetToken(mockMvc, "logout1@test.com", "pass123");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /me — no token returns 401")
    void getMe_noToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
