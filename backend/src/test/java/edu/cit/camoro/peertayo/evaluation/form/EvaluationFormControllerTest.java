package edu.cit.camoro.peertayo.evaluation.form;

import com.jayway.jsonpath.JsonPath;
import edu.cit.camoro.peertayo.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Evaluation Form API")
class EvaluationFormControllerTest {

    @Autowired MockMvc mockMvc;

    private String facilitatorToken;
    private String respondentToken;
    private Long evaluateeId;

    @BeforeEach
    void setUp() throws Exception {
        // Register a respondent (evaluatee)
        evaluateeId = TestHelper.registerAndGetUserId(mockMvc, "evaluatee@test.com", "pass123");

        // Register a facilitator
        String raw = TestHelper.registerAndGetToken(mockMvc, "facilitator@test.com", "pass123");
        facilitatorToken = TestHelper.promoteAndGetToken(mockMvc, raw);

        // Register a plain respondent
        respondentToken = TestHelper.registerAndGetToken(mockMvc, "respondent@test.com", "pass123");
    }

    private String futureDeadline() {
        return LocalDateTime.now().plusDays(7)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    private String createBody(Long evaluateeId, Long evaluatorId) {
        return """
                {
                  "title": "Q1 Review",
                  "description": "Quarterly peer review",
                  "deadline": "%s",
                  "evaluateeIds": [%d],
                  "evaluatorIds": [%d]
                }
                """.formatted(futureDeadline(), evaluateeId, evaluatorId);
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /evaluations — facilitator can create evaluation")
    void create_asFacilitator_success() throws Exception {
        // evaluateeId (from @BeforeEach) and facilitatorToken owner are different users
        // Use evaluateeId as evaluatee and a separate evaluator
        Long evaluatorId2 = TestHelper.registerAndGetUserId(mockMvc, "evaluator2@test.com", "pass123");

        mockMvc.perform(post("/api/v1/evaluations")
                        .header("Authorization", "Bearer " + facilitatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(evaluateeId, evaluatorId2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.evaluation.title").value("Q1 Review"))
                .andExpect(jsonPath("$.data.evaluation.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /evaluations — respondent gets 403")
    void create_asRespondent_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations")
                        .header("Authorization", "Bearer " + respondentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(evaluateeId, evaluateeId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH-003"));
    }

    @Test
    @DisplayName("POST /evaluations — unauthenticated gets 401")
    void create_noToken_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(evaluateeId, evaluateeId)))
                .andExpect(status().isUnauthorized());
    }

    // ── List ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /evaluations/created — facilitator sees their forms")
    void getCreated_asFacilitator() throws Exception {
        Long evaluatorId2 = TestHelper.registerAndGetUserId(mockMvc, "list_eval@test.com", "pass123");
        mockMvc.perform(post("/api/v1/evaluations")
                .header("Authorization", "Bearer " + facilitatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody(evaluateeId, evaluatorId2)));

        mockMvc.perform(get("/api/v1/evaluations/created")
                        .header("Authorization", "Bearer " + facilitatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evaluations").isArray());
    }

    @Test
    @DisplayName("GET /evaluations/created — respondent gets 403")
    void getCreated_asRespondent_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/evaluations/created")
                        .header("Authorization", "Bearer " + respondentToken))
                .andExpect(status().isForbidden());
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /evaluations/{id} — facilitator can update their form")
    void update_success() throws Exception {
        Long evaluatorId2 = TestHelper.registerAndGetUserId(mockMvc, "upd_eval@test.com", "pass123");
        var createResult = mockMvc.perform(post("/api/v1/evaluations")
                        .header("Authorization", "Bearer " + facilitatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(evaluateeId, evaluatorId2)))
                .andReturn();

        Long evalId = ((Number) JsonPath.read(
                createResult.getResponse().getContentAsString(), "$.data.evaluation.id")).longValue();

        var updateBody = """
                {
                  "title": "Updated Title",
                  "description": "Updated desc",
                  "deadline": "%s"
                }
                """.formatted(futureDeadline());

        mockMvc.perform(put("/api/v1/evaluations/" + evalId)
                        .header("Authorization", "Bearer " + facilitatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evaluation.title").value("Updated Title"));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /evaluations/{id} — facilitator can delete their form")
    void delete_success() throws Exception {
        Long evaluatorId2 = TestHelper.registerAndGetUserId(mockMvc, "del_eval@test.com", "pass123");
        var createResult = mockMvc.perform(post("/api/v1/evaluations")
                        .header("Authorization", "Bearer " + facilitatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(evaluateeId, evaluatorId2)))
                .andReturn();

        Long evalId = ((Number) JsonPath.read(
                createResult.getResponse().getContentAsString(), "$.data.evaluation.id")).longValue();

        mockMvc.perform(delete("/api/v1/evaluations/" + evalId)
                        .header("Authorization", "Bearer " + facilitatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Evaluation deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /evaluations/{id} — respondent gets 403")
    void delete_asRespondent_forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/evaluations/999")
                        .header("Authorization", "Bearer " + respondentToken))
                .andExpect(status().isForbidden());
    }
}
