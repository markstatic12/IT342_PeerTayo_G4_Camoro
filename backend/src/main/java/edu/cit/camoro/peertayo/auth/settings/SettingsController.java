package edu.cit.camoro.peertayo.auth.settings;

import edu.cit.camoro.peertayo.auth.shared.UserResponse;
import edu.cit.camoro.peertayo.auth.token.AuthResponse;
import edu.cit.camoro.peertayo.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    /**
     * GET /api/v1/settings/profile
     * Returns the current user's profile information.
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, UserResponse>>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse profile = settingsService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("user", profile)));
    }

    /**
     * PUT /api/v1/settings/profile
     * Updates first name, last name, and email.
     * Returns a fresh AuthResponse (user + new token) so the frontend can update its session.
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<AuthResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        AuthResponse response = settingsService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * PUT /api/v1/settings/password
     * Changes the user's password after verifying the current one.
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Map<String, String>>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        settingsService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Password updated successfully")));
    }
}
