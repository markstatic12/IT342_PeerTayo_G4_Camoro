package edu.cit.camoro.peertayo.notification.list;

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
public class ListNotificationController {

    private final ListNotificationService listNotificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<NotificationResponse>>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<NotificationResponse> notifications =
                listNotificationService.getNotifications(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("notifications", notifications)));
    }
}
