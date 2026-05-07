package edu.cit.camoro.peertayo.notification.preferences;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationPreferencesResponse {
    private boolean evaluationAssigned;
    private boolean deadlineReminder;
    private boolean resultsPublished;
    private boolean formCreated;
    private boolean submissionReceived;
    private boolean systemAnnouncements;
}
