package edu.cit.camoro.peertayo.notification.shared;

import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.notification.entity.Notification;
import edu.cit.camoro.peertayo.notification.entity.NotificationType;
import edu.cit.camoro.peertayo.notification.entity.UserNotificationPreference;
import edu.cit.camoro.peertayo.notification.repository.NotificationRepository;
import edu.cit.camoro.peertayo.notification.repository.UserNotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Central service for creating notifications.
 * Checks the recipient's preferences before persisting — if the user has
 * disabled a notification type, the notification is silently skipped.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationPreferenceRepository preferenceRepository;

    /**
     * Send a notification to a single user if their preferences allow it.
     */
    @Transactional
    public void send(User recipient, String message, NotificationType type) {
        if (!isEnabled(recipient, type)) return;

        notificationRepository.save(
                Notification.builder()
                        .user(recipient)
                        .message(message)
                        .type(type)
                        .read(false)
                        .build()
        );
    }

    /**
     * Send the same notification to multiple users, respecting each user's preferences.
     */
    @Transactional
    public void sendToAll(List<User> recipients, String message, NotificationType type) {
        List<Notification> toSave = recipients.stream()
                .filter(u -> isEnabled(u, type))
                .map(u -> Notification.builder()
                        .user(u)
                        .message(message)
                        .type(type)
                        .read(false)
                        .build())
                .toList();

        if (!toSave.isEmpty()) {
            notificationRepository.saveAll(toSave);
        }
    }

    /**
     * Returns the user's preference for the given type.
     * If no preference row exists yet, defaults to enabled (true).
     */
    private boolean isEnabled(User user, NotificationType type) {
        return preferenceRepository.findByUser(user)
                .map(pref -> pref.isEnabled(type))
                .orElse(true); // default: all enabled
    }
}
