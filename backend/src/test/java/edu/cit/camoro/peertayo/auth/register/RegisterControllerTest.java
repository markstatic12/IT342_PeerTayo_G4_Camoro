package edu.cit.camoro.peertayo.auth.register;

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
@DisplayName("Register API")
class RegisterControllerTest {

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("POST /register — success returns 201 with RESPONDENT role")
    void register_success() throws Exception {
        var body = """
                {"firstName":"Juan","lastName":"Cruz","email":"juan@test.com","password":"pass123"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("juan@test.com"))
                .andExpect(jsonPath("$.data.user.roles[0]").value("RESPONDENT"));
    }

    @Test
    @DisplayName("POST /register — duplicate email returns 409")
    void register_duplicateEmail() throws Exception {
        var body = """
                {"firstName":"A","lastName":"B","email":"dup@test.com","password":"pass123"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DB-002"));
    }

    @Test
    @DisplayName("POST /register — short password returns 400")
    void register_shortPassword() throws Exception {
        var body = """
                {"firstName":"A","lastName":"B","email":"short@test.com","password":"abc"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALID-001"));
    }

    @Test
    @DisplayName("POST /register — missing fields returns 400")
    void register_missingFields() throws Exception {
        var body = """
                {"firstName":"","lastName":"","email":"","password":""}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
