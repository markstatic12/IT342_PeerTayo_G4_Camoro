package edu.cit.camoro.peertayo.notification.controller;

import edu.cit.camoro.peertayo.notification.dto.response.NotificationResponse;
import edu.cit.camoro.peertayo.notification.service.NotificationService;
import edu.cit.camoro.peertayo.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<NotificationResponse>>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<NotificationResponse> notifications = notificationService.getNotifications(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("notifications", notifications)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Map<String, String>>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAsRead(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Notification marked as read")));
    }
}
