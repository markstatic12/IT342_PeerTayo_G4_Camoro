package edu.cit.camoro.peertayo.notification.entity;

import edu.cit.camoro.peertayo.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Stores per-user in-app notification preferences.
 * One row per user — all types default to enabled (true).
 */
@Entity
@Table(name = "user_notification_preferences",
       uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "evaluation_assigned", nullable = false)
    @Builder.Default
    private boolean evaluationAssigned = true;

    @Column(name = "deadline_reminder", nullable = false)
    @Builder.Default
    private boolean deadlineReminder = true;

    @Column(name = "results_published", nullable = false)
    @Builder.Default
    private boolean resultsPublished = true;

    @Column(name = "form_created", nullable = false)
    @Builder.Default
    private boolean formCreated = true;

    @Column(name = "submission_received", nullable = false)
    @Builder.Default
    private boolean submissionReceived = true;

    @Column(name = "system_announcements", nullable = false)
    @Builder.Default
    private boolean systemAnnouncements = true;

    /** Returns true if the given notification type is enabled for this user. */
    public boolean isEnabled(NotificationType type) {
        return switch (type) {
            case EVALUATION_ASSIGNED  -> evaluationAssigned;
            case DEADLINE_REMINDER    -> deadlineReminder;
            case RESULTS_PUBLISHED    -> resultsPublished;
            case FORM_CREATED         -> formCreated;
            case SUBMISSION_RECEIVED  -> submissionReceived;
            case SYSTEM               -> systemAnnouncements;
        };
    }
}
