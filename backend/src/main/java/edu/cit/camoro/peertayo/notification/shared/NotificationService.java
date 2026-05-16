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
    private final EmailService emailService;

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

        // Trigger Email Notification for high-priority events
        triggerEmail(recipient, message, type);
    }

    private void triggerEmail(User recipient, String message, NotificationType type) {
        String subject;
        String title;
        String buttonText = "Launch PeerTayo";
        String buttonPath = "/pending-evaluations";
        
        String welcomeNote = "Visit the PeerTayo portal to manage your evaluations and stay updated with your team's performance.";

        switch (type) {
            case EVALUATION_ASSIGNED -> {
                subject = "[PeerTayo] New Evaluation Task Assigned";
                title = "New Assignment Received";
            }
            case DEADLINE_REMINDER -> {
                subject = "[Action Required] Your PeerTayo Deadline is Approaching";
                title = "Priority Deadline Reminder";
            }
            case DEADLINE_EXTENDED -> {
                subject = "[Update] More Time Granted for Your Evaluation";
                title = "Deadline Extended";
            }
            case ZERO_SUBMISSIONS -> {
                subject = "[Urgent] Evaluation Closed with No Responses";
                title = "Incomplete Evaluation Form";
                buttonPath = "/forms-created";
            }
            case RESULTS_PUBLISHED -> {
                subject = "[PeerTayo] Your Performance Results are Now Live";
                title = "Results Published";
                buttonPath = "/my-results";
            }
            case WELCOME -> {
                subject = "Welcome to PeerTayo!";
                title = "Account Created Successfully";
                buttonText = "Explore Dashboard";
                buttonPath = "/";
            }
            default -> {
                return;
            }
        }

        String fullContent = message + "\n\n" + welcomeNote;

        emailService.sendHtmlEmail(
            recipient.getEmail(),
            subject,
            title,
            fullContent,
            buttonText,
            buttonPath
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
            // Trigger Emails for each recipient
            toSave.forEach(notif -> triggerEmail(notif.getUser(), notif.getMessage(), notif.getType()));
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
