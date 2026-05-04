package edu.cit.camoro.peertayo.auth.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Login API")
class LoginControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void registerUser(String email, String password) throws Exception {
        var body = """
                {"firstName":"Test","lastName":"User","email":"%s","password":"%s"}
                """.formatted(email, password);
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /login — success returns token and user")
    void login_success() throws Exception {
        registerUser("login@test.com", "password123");

        var body = """
                {"email":"login@test.com","password":"password123"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("login@test.com"))
                .andExpect(jsonPath("$.data.user.roles[0]").value("RESPONDENT"));
    }

    @Test
    @DisplayName("POST /login — wrong password returns 401")
    void login_wrongPassword() throws Exception {
        registerUser("wrongpw@test.com", "correctpassword");

        var body = """
                {"email":"wrongpw@test.com","password":"wrongpassword"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH-001"));
    }

    @Test
    @DisplayName("POST /login — unknown email returns 404")
    void login_unknownEmail() throws Exception {
        var body = """
                {"email":"nobody@test.com","password":"password123"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("DB-001"));
    }

    @Test
    @DisplayName("POST /login — blank fields returns 400")
    void login_blankFields() throws Exception {
        var body = """
                {"email":"","password":""}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALID-001"));
    }
}
