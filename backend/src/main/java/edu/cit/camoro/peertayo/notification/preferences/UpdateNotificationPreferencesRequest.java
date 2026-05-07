package edu.cit.camoro.peertayo.notification.preferences;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateNotificationPreferencesRequest {
    private boolean evaluationAssigned;
    private boolean deadlineReminder;
    private boolean resultsPublished;
    private boolean formCreated;
    private boolean submissionReceived;
    private boolean systemAnnouncements;
}
