package edu.cit.camoro.peertayo.notification.markread;

import edu.cit.camoro.peertayo.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class MarkReadController {

    private final MarkReadService markReadService;

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Map<String, String>>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        markReadService.markAsRead(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Notification marked as read")));
    }
}
