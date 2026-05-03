package edu.cit.camoro.peertayo.evaluation.submission;

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
@DisplayName("Evaluation Submission API")
class SubmissionControllerTest {

    @Autowired MockMvc mockMvc;

    private String facilitatorToken;
    private String evaluatorToken;
    private Long evaluatorId;
    private Long evaluateeId;
    private Long evaluationId;

    @BeforeEach
    void setUp() throws Exception {
        // Facilitator
        String raw = TestHelper.registerAndGetToken(mockMvc, "fac@sub.com", "pass123");
        facilitatorToken = TestHelper.promoteAndGetToken(mockMvc, raw);

        // Evaluator (respondent who will submit)
        var evaluatorInfo = TestHelper.register(mockMvc, "evaluator@sub.com", "pass123");
        evaluatorId    = evaluatorInfo.id();
        evaluatorToken = evaluatorInfo.token();

        // Evaluatee
        evaluateeId = TestHelper.registerAndGetUserId(mockMvc, "evaluatee@sub.com", "pass123");

        // Create evaluation
        String deadline = LocalDateTime.now().plusDays(7)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        var createBody = """
                {
                  "title": "Sub Test Eval",
                  "description": "Test",
                  "deadline": "%s",
                  "evaluateeIds": [%d],
                  "evaluatorIds": [%d]
                }
                """.formatted(deadline, evaluateeId, evaluatorId);

        var result = mockMvc.perform(post("/api/v1/evaluations")
                        .header("Authorization", "Bearer " + facilitatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andReturn();

        evaluationId = ((Number) JsonPath.read(
                result.getResponse().getContentAsString(), "$.data.evaluation.id")).longValue();
    }

    @Test
    @DisplayName("GET /evaluations/pending — evaluator sees assigned evaluations")
    void getPending_success() throws Exception {
        mockMvc.perform(get("/api/v1/evaluations/pending")
                        .header("Authorization", "Bearer " + evaluatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evaluations").isArray())
                .andExpect(jsonPath("$.data.evaluations[0].title").value("Sub Test Eval"));
    }

    @Test
    @DisplayName("GET /evaluations/pending — unauthenticated returns 401")
    void getPending_noToken() throws Exception {
        mockMvc.perform(get("/api/v1/evaluations/pending"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /evaluations/{id}/submit — evaluator submits successfully")
    void submit_success() throws Exception {
        var submitBody = """
                {
                  "responses": [
                    {"criteriaId": 1, "rating": 4},
                    {"criteriaId": 2, "rating": 3},
                    {"criteriaId": 3, "rating": 5},
                    {"criteriaId": 4, "rating": 4},
                    {"criteriaId": 5, "rating": 3},
                    {"criteriaId": 6, "rating": 4},
                    {"criteriaId": 7, "rating": 5},
                    {"criteriaId": 8, "rating": 3},
                    {"criteriaId": 9, "rating": 4},
                    {"criteriaId": 10, "rating": 4}
                  ],
                  "comment": "Great teamwork"
                }
                """;

        mockMvc.perform(post("/api/v1/evaluations/" + evaluationId + "/submit")
                        .header("Authorization", "Bearer " + evaluatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Evaluation submitted successfully"));
    }

    @Test
    @DisplayName("POST /evaluations/{id}/submit — rating out of range returns 400")
    void submit_invalidRating() throws Exception {
        var submitBody = """
                {
                  "responses": [
                    {"criteriaId": 1, "rating": 10}
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/evaluations/" + evaluationId + "/submit")
                        .header("Authorization", "Bearer " + evaluatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /evaluations/{id}/submit — double submit returns 422")
    void submit_alreadySubmitted() throws Exception {
        var submitBody = """
                {
                  "responses": [
                    {"criteriaId": 1, "rating": 4},
                    {"criteriaId": 2, "rating": 3},
                    {"criteriaId": 3, "rating": 5},
                    {"criteriaId": 4, "rating": 4},
                    {"criteriaId": 5, "rating": 3},
                    {"criteriaId": 6, "rating": 4},
                    {"criteriaId": 7, "rating": 5},
                    {"criteriaId": 8, "rating": 3},
                    {"criteriaId": 9, "rating": 4},
                    {"criteriaId": 10, "rating": 4}
                  ]
                }
                """;

        // First submit
        mockMvc.perform(post("/api/v1/evaluations/" + evaluationId + "/submit")
                .header("Authorization", "Bearer " + evaluatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(submitBody));

        // Second submit — should fail
        mockMvc.perform(post("/api/v1/evaluations/" + evaluationId + "/submit")
                        .header("Authorization", "Bearer " + evaluatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("EVAL-002"));
    }
}
