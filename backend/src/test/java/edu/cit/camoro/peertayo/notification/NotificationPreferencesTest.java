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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Notification Preferences API")
class NotificationPreferencesTest {

    @Autowired MockMvc mockMvc;

    private String userToken;
    private Long userId;
    private String facilitatorToken;
    private Long evaluateeId;

    @BeforeEach
    void setUp() throws Exception {
        var info = TestHelper.register(mockMvc, "notifpref@test.com", "pass123");
        userToken = info.token();
        userId = info.id();

        String raw = TestHelper.registerAndGetToken(mockMvc, "fac_pref@test.com", "pass123");
        facilitatorToken = TestHelper.promoteAndGetToken(mockMvc, raw);
        evaluateeId = TestHelper.registerAndGetUserId(mockMvc, "ee_pref@test.com", "pass123");
    }

    // ── GET defaults ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /preferences — returns all-enabled defaults for new user")
    void getPreferences_defaults() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/preferences")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evaluationAssigned").value(true))
                .andExpect(jsonPath("$.data.deadlineReminder").value(true))
                .andExpect(jsonPath("$.data.resultsPublished").value(true))
                .andExpect(jsonPath("$.data.formCreated").value(true))
                .andExpect(jsonPath("$.data.submissionReceived").value(true))
                .andExpect(jsonPath("$.data.systemAnnouncements").value(true));
    }

    // ── PUT preferences ───────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /preferences — saves and returns updated preferences")
    void updatePreferences_success() throws Exception {
        var body = """
                {
                  "evaluationAssigned": false,
                  "deadlineReminder": true,
                  "resultsPublished": false,
                  "formCreated": true,
                  "submissionReceived": false,
                  "systemAnnouncements": true
                }
                """;

        mockMvc.perform(put("/api/v1/notifications/preferences")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evaluationAssigned").value(false))
                .andExpect(jsonPath("$.data.resultsPublished").value(false))
                .andExpect(jsonPath("$.data.submissionReceived").value(false))
                .andExpect(jsonPath("$.data.deadlineReminder").value(true))
                .andExpect(jsonPath("$.data.systemAnnouncements").value(true));
    }

    @Test
    @DisplayName("PUT then GET — persisted preferences are returned on subsequent GET")
    void updateThenGet_persisted() throws Exception {
        // Disable evaluationAssigned
        mockMvc.perform(put("/api/v1/notifications/preferences")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"evaluationAssigned":false,"deadlineReminder":true,
                         "resultsPublished":true,"formCreated":true,
                         "submissionReceived":true,"systemAnnouncements":true}
                        """));

        // GET should reflect the saved value
        mockMvc.perform(get("/api/v1/notifications/preferences")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evaluationAssigned").value(false));
    }

    // ── Preference enforcement ────────────────────────────────────────────

    @Test
    @DisplayName("Notification NOT created when evaluationAssigned preference is OFF")
    void evaluationAssigned_disabled_noNotification() throws Exception {
        // Turn off evaluationAssigned for the user
        mockMvc.perform(put("/api/v1/notifications/preferences")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"evaluationAssigned":false,"deadlineReminder":true,
                         "resultsPublished":true,"formCreated":true,
                         "submissionReceived":true,"systemAnnouncements":true}
                        """));

        // Facilitator creates evaluation assigning this user as evaluator
        String deadline = LocalDateTime.now().plusDays(7)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        mockMvc.perform(post("/api/v1/evaluations")
                .header("Authorization", "Bearer " + facilitatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Pref Test","description":"Test","deadline":"%s",
                         "evaluateeIds":[%d],"evaluatorIds":[%d]}
                        """.formatted(deadline, evaluateeId, userId)));

        // User should have NO notifications
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications").isEmpty());
    }

    @Test
    @DisplayName("Notification IS created when evaluationAssigned preference is ON")
    void evaluationAssigned_enabled_notificationCreated() throws Exception {
        // Ensure preference is ON (default)
        mockMvc.perform(put("/api/v1/notifications/preferences")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"evaluationAssigned":true,"deadlineReminder":true,
                         "resultsPublished":true,"formCreated":true,
                         "submissionReceived":true,"systemAnnouncements":true}
                        """));

        // Facilitator creates evaluation assigning this user as evaluator
        String deadline = LocalDateTime.now().plusDays(7)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        mockMvc.perform(post("/api/v1/evaluations")
                .header("Authorization", "Bearer " + facilitatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Pref Test ON","description":"Test","deadline":"%s",
                         "evaluateeIds":[%d],"evaluatorIds":[%d]}
                        """.formatted(deadline, evaluateeId, userId)));

        // User SHOULD have a notification
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications").isArray())
                .andExpect(jsonPath("$.data.notifications[0].type").value("EVALUATION_ASSIGNED"))
                .andExpect(jsonPath("$.data.notifications[0].isRead").value(false));
    }

    @Test
    @DisplayName("GET /preferences — unauthenticated returns 401")
    void getPreferences_noToken() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/preferences"))
                .andExpect(status().isUnauthorized());
    }
}
