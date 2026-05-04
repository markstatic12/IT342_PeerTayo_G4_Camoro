package edu.cit.camoro.peertayo.notification;

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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Notification API")
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;

    private String evaluatorToken;
    private Long evaluatorId;

    @BeforeEach
    void setUp() throws Exception {
        // Register evaluator — creating an evaluation sends them a notification
        var evaluatorInfo = TestHelper.register(mockMvc, "notif@test.com", "pass123");
        evaluatorId    = evaluatorInfo.id();
        evaluatorToken = evaluatorInfo.token();

        // Register facilitator and create an evaluation assigning evaluator
        Long evaluateeId = TestHelper.registerAndGetUserId(mockMvc, "notif_ee@test.com", "pass123");
        String raw = TestHelper.registerAndGetToken(mockMvc, "notif_fac@test.com", "pass123");
        String facToken = TestHelper.promoteAndGetToken(mockMvc, raw);

        String deadline = LocalDateTime.now().plusDays(7)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        mockMvc.perform(post("/api/v1/evaluations")
                .header("Authorization", "Bearer " + facToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "Notif Test",
                          "description": "Test",
                          "deadline": "%s",
                          "evaluateeIds": [%d],
                          "evaluatorIds": [%d]
                        }
                        """.formatted(deadline, evaluateeId, evaluatorId)));
    }

    @Test
    @DisplayName("GET /notifications — returns notifications for authenticated user")
    void getNotifications_success() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + evaluatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications").isArray())
                .andExpect(jsonPath("$.data.notifications[0].message")
                        .value("You have a new evaluation assigned."));
    }

    @Test
    @DisplayName("GET /notifications — unauthenticated returns 401")
    void getNotifications_noToken() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /notifications/{id}/read — marks notification as read")
    void markAsRead_success() throws Exception {
        // Get the notification id
        var result = mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + evaluatorToken))
                .andReturn();

        List<Integer> ids = JsonPath.read(result.getResponse().getContentAsString(),
                "$.data.notifications[*].id");
        Long notifId = ids.get(0).longValue();

        mockMvc.perform(put("/api/v1/notifications/" + notifId + "/read")
                        .header("Authorization", "Bearer " + evaluatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Notification marked as read"));
    }

    @Test
    @DisplayName("PUT /notifications/{id}/read — wrong user gets 404")
    void markAsRead_wrongUser() throws Exception {
        // Get notification id
        var result = mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + evaluatorToken))
                .andReturn();
        List<Integer> ids = JsonPath.read(result.getResponse().getContentAsString(),
                "$.data.notifications[*].id");
        Long notifId = ids.get(0).longValue();

        // Different user tries to mark it
        String otherToken = TestHelper.registerAndGetToken(mockMvc, "other@test.com", "pass123");
        mockMvc.perform(put("/api/v1/notifications/" + notifId + "/read")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }
}
