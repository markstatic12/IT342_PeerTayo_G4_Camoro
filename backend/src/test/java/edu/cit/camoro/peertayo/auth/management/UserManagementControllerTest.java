package edu.cit.camoro.peertayo.auth.management;

import edu.cit.camoro.peertayo.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("User Management API")
class UserManagementControllerTest {

    @Autowired MockMvc mockMvc;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        TestHelper.registerAndGetUserId(mockMvc, "alice@test.com", "pass123");
        TestHelper.registerAndGetUserId(mockMvc, "bob@test.com", "pass123");
        token = TestHelper.registerAndGetToken(mockMvc, "searcher@test.com", "pass123");
    }

    @Test
    @DisplayName("GET /users — returns up to 20 users")
    void listUsers_success() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.users").isArray());
    }

    @Test
    @DisplayName("GET /users?q=alice — returns matching users")
    void searchUsers_byName() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .param("q", "alice")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.users[0].email").value("alice@test.com"));
    }

    @Test
    @DisplayName("GET /users?q=xyz — returns empty list for no match")
    void searchUsers_noMatch() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .param("q", "xyz_nobody")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.users").isEmpty());
    }

    @Test
    @DisplayName("GET /users — unauthenticated returns 401")
    void listUsers_noToken() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }
}
