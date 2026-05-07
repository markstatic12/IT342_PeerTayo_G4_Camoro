package edu.cit.camoro.peertayo.notification.preferences;

import edu.cit.camoro.peertayo.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications/preferences")
@RequiredArgsConstructor
public class NotificationPreferencesController {

    private final NotificationPreferencesService preferencesService;

    /**
     * GET /api/v1/notifications/preferences
     * Returns the current user's notification preferences.
     * If no preferences have been saved yet, returns all-enabled defaults.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> getPreferences(
            @AuthenticationPrincipal UserDetails userDetails) {
        NotificationPreferencesResponse prefs =
                preferencesService.getPreferences(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(prefs));
    }

    /**
     * PUT /api/v1/notifications/preferences
     * Saves the user's notification preferences.
     */
    @PutMapping
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> updatePreferences(
            @RequestBody UpdateNotificationPreferencesRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        NotificationPreferencesResponse prefs =
                preferencesService.updatePreferences(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok(prefs));
    }
}
