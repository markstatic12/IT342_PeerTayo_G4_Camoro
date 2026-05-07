package edu.cit.camoro.peertayo.notification.preferences;

import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.auth.repository.UserRepository;
import edu.cit.camoro.peertayo.notification.entity.UserNotificationPreference;
import edu.cit.camoro.peertayo.notification.repository.UserNotificationPreferenceRepository;
import edu.cit.camoro.peertayo.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationPreferencesService {

    private final UserNotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public NotificationPreferencesResponse getPreferences(String email) {
        User user = getUser(email);
        UserNotificationPreference pref = preferenceRepository.findByUser(user)
                .orElseGet(() -> defaultPreferences(user));
        return toResponse(pref);
    }

    @Transactional
    public NotificationPreferencesResponse updatePreferences(String email,
                                                              UpdateNotificationPreferencesRequest request) {
        User user = getUser(email);
        UserNotificationPreference pref = preferenceRepository.findByUser(user)
                .orElseGet(() -> defaultPreferences(user));

        pref.setEvaluationAssigned(request.isEvaluationAssigned());
        pref.setDeadlineReminder(request.isDeadlineReminder());
        pref.setResultsPublished(request.isResultsPublished());
        pref.setFormCreated(request.isFormCreated());
        pref.setSubmissionReceived(request.isSubmissionReceived());
        pref.setSystemAnnouncements(request.isSystemAnnouncements());

        preferenceRepository.save(pref);
        return toResponse(pref);
    }

    private UserNotificationPreference defaultPreferences(User user) {
        return UserNotificationPreference.builder().user(user).build();
    }

    private NotificationPreferencesResponse toResponse(UserNotificationPreference pref) {
        return NotificationPreferencesResponse.builder()
                .evaluationAssigned(pref.isEvaluationAssigned())
                .deadlineReminder(pref.isDeadlineReminder())
                .resultsPublished(pref.isResultsPublished())
                .formCreated(pref.isFormCreated())
                .submissionReceived(pref.isSubmissionReceived())
                .systemAnnouncements(pref.isSystemAnnouncements())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
