package edu.cit.camoro.peertayo.notification.list;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.cit.camoro.peertayo.notification.entity.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String message;
    private NotificationType type;
    @JsonProperty("isRead")
    private boolean isRead;
    private LocalDateTime createdAt;
}
