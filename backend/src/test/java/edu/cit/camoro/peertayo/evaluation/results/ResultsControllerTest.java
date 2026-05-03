package edu.cit.camoro.peertayo.evaluation.results;

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
@DisplayName("Evaluation Results API")
class ResultsControllerTest {

    @Autowired MockMvc mockMvc;

    private String facilitatorToken;
    private String evaluatorToken;
    private String evaluateeToken;
    private Long evaluatorId;
    private Long evaluateeId;
    private Long evaluationId;

    @BeforeEach
    void setUp() throws Exception {
        String raw = TestHelper.registerAndGetToken(mockMvc, "fac@res.com", "pass123");
        facilitatorToken = TestHelper.promoteAndGetToken(mockMvc, raw);

        var evaluatorInfo = TestHelper.register(mockMvc, "evaluator@res.com", "pass123");
        evaluatorId    = evaluatorInfo.id();
        evaluatorToken = evaluatorInfo.token();

        var evaluateeInfo = TestHelper.register(mockMvc, "evaluatee@res.com", "pass123");
        evaluateeId    = evaluateeInfo.id();
        evaluateeToken = evaluateeInfo.token();

        String deadline = LocalDateTime.now().plusDays(7)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        var createBody = """
                {
                  "title": "Results Test Eval",
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
    @DisplayName("GET /evaluations/my-results — returns empty results for new user")
    void getMyResults_empty() throws Exception {
        mockMvc.perform(get("/api/v1/evaluations/my-results")
                        .header("Authorization", "Bearer " + evaluateeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.results.overallAverage").value(0.0))
                .andExpect(jsonPath("$.data.results.totalResponses").value(0));
    }

    @Test
    @DisplayName("GET /evaluations/my-results — returns scores after submission")
    void getMyResults_afterSubmission() throws Exception {
        var submitBody = """
                {
                  "responses": [
                    {"criteriaId": 1, "rating": 5},
                    {"criteriaId": 2, "rating": 5},
                    {"criteriaId": 3, "rating": 5},
                    {"criteriaId": 4, "rating": 5},
                    {"criteriaId": 5, "rating": 5},
                    {"criteriaId": 6, "rating": 5},
                    {"criteriaId": 7, "rating": 5},
                    {"criteriaId": 8, "rating": 5},
                    {"criteriaId": 9, "rating": 5},
                    {"criteriaId": 10, "rating": 5}
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/evaluations/" + evaluationId + "/submit")
                .header("Authorization", "Bearer " + evaluatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(submitBody));

        mockMvc.perform(get("/api/v1/evaluations/my-results")
                        .header("Authorization", "Bearer " + evaluateeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.results.overallAverage").value(5.0))
                .andExpect(jsonPath("$.data.results.totalResponses").value(1));
    }

    @Test
    @DisplayName("GET /evaluations/{id}/results — facilitator sees evaluatee breakdown")
    void getEvaluationResults_asFacilitator() throws Exception {
        mockMvc.perform(get("/api/v1/evaluations/" + evaluationId + "/results")
                        .header("Authorization", "Bearer " + facilitatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evaluationId").value(evaluationId))
                .andExpect(jsonPath("$.data.evaluatees").isArray());
    }

    @Test
    @DisplayName("GET /evaluations/{id}/results — respondent gets 403")
    void getEvaluationResults_asRespondent_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/evaluations/" + evaluationId + "/results")
                        .header("Authorization", "Bearer " + evaluatorToken))
                .andExpect(status().isForbidden());
    }
}
