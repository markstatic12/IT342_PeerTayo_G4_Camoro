package edu.cit.camoro.peertayo.evaluation.shared;

import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.evaluation.entity.EvaluationAssignment;
import edu.cit.camoro.peertayo.evaluation.entity.EvaluationForm;
import edu.cit.camoro.peertayo.evaluation.repository.EvaluationAssignmentRepository;
import edu.cit.camoro.peertayo.evaluation.repository.EvaluationFormRepository;
import edu.cit.camoro.peertayo.notification.entity.NotificationType;
import edu.cit.camoro.peertayo.notification.repository.NotificationRepository;
import edu.cit.camoro.peertayo.notification.shared.NotificationService;
import edu.cit.camoro.peertayo.shared.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeadlineManagementService {

    private static final Logger log = LoggerFactory.getLogger(DeadlineManagementService.class);

    private final EvaluationFormRepository evaluationFormRepository;
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final MailService mailService;

    /**
     * Runs every 5 minutes to:
     * 1. Auto-close evaluations past their deadline
     * 2. Flag evaluations as "Needs Attention" when 1 day away with incomplete submissions
     * 3. Send reminder notifications to pending evaluators
     */
    @Scheduled(fixedDelay = 60_000) // 1 minute
    @Transactional
    public void processDeadlineManagement() {
        LocalDateTime now = LocalDateTime.now();
        
        // Get all active evaluations
        List<EvaluationForm> activeEvaluations = evaluationFormRepository.findAll()
                .stream()
                .filter(e -> e.getStatus().equals("ACTIVE") || e.getStatus().equals("NEEDS_ATTENTION"))
                .collect(Collectors.toList());

        for (EvaluationForm evaluation : activeEvaluations) {
            if (evaluation.getDeadline().isBefore(now)) {
                // Auto-close evaluations past deadline
                closeEvaluation(evaluation);
            } else if (shouldFlagAsNeedsAttention(evaluation, now)) {
                // Flag and send reminders if 1 day away with incomplete submissions
                flagAsNeedsAttentionAndNotify(evaluation);
            }
        }
    }

    private void closeEvaluation(EvaluationForm evaluation) {
        // BR-004: Check for zero submissions
        long submissionCount = evaluationAssignmentRepository.countByEvaluationAndSubmittedTrue(evaluation);
        
        evaluation.setStatus("CLOSED");
        evaluationFormRepository.save(evaluation);
        log.info("Auto-closed evaluation {} - deadline passed (submissions: {})", evaluation.getId(), submissionCount);

        if (submissionCount == 0) {
            String message = String.format(
                "Evaluation '%s' has expired with zero submissions. Please choose to extend the deadline or close it permanently.",
                evaluation.getTitle()
            );
            notificationService.send(evaluation.getCreatedBy(), message, NotificationType.ZERO_SUBMISSIONS);
            log.info("BR-004: Sent zero-submission alert to facilitator for evaluation {}", evaluation.getId());
            
            // Email alert placeholder (as per requirement)
            sendEmailAlert(evaluation.getCreatedBy(), "Zero Submissions for Evaluation: " + evaluation.getTitle(), message);
        }
    }

    private void sendEmailAlert(User user, String subject, String body) {
        mailService.send(user.getEmail(), subject, body);
        log.info("[EMAIL ALERT] To: {} | Subject: {} | Body: {}", user.getEmail(), subject, body);
    }

    private boolean shouldFlagAsNeedsAttention(EvaluationForm evaluation, LocalDateTime now) {
        // If already flagged, don't flag again
        if (evaluation.getStatus().equals("NEEDS_ATTENTION")) {
            return false;
        }

        LocalDateTime createdAt = evaluation.getCreatedAt();
        LocalDateTime deadline = evaluation.getDeadline();
        
        java.time.Duration totalDuration = java.time.Duration.between(createdAt, deadline);
        long totalHours = totalDuration.toHours();
        
        // Senior Engineering Logic: Proportional Urgency
        long warningThresholdHours;
        if (totalHours > 72) { // > 3 days
            warningThresholdHours = 24; 
        } else if (totalHours > 24) { // 1-3 days
            warningThresholdHours = Math.max(6, totalHours / 5); // 20% of time, min 6h
        } else { // < 24 hours
            warningThresholdHours = Math.max(1, totalHours / 2); // 50% of time, min 1h
        }

        LocalDateTime warningMark = deadline.minusHours(warningThresholdHours);
        if (now.isBefore(warningMark)) {
            return false;
        }

        // Check if there are any unsubmitted assignments
        long unsubmittedCount = evaluationAssignmentRepository
                .findAllByEvaluation(evaluation)
                .stream()
                .filter(a -> !a.isSubmitted())
                .count();

        return unsubmittedCount > 0;
    }

    private void flagAsNeedsAttentionAndNotify(EvaluationForm evaluation) {
        evaluation.setStatus("NEEDS_ATTENTION");
        evaluationFormRepository.save(evaluation);
        log.info("Flagged evaluation {} as Needs Attention - deadline approaching with incomplete submissions", evaluation.getId());

        // Notify Creator (Facilitator)
        String creatorMsg = String.format(
            "Evaluation '%s' is approaching its deadline with incomplete submissions. Reminders have been sent to pending evaluators.",
            evaluation.getTitle()
        );
        notificationService.send(evaluation.getCreatedBy(), creatorMsg, NotificationType.DEADLINE_REMINDER);

        // Get all unsubmitted evaluators and send reminder notifications
        List<EvaluationAssignment> unsubmitted = evaluationAssignmentRepository
                .findAllByEvaluation(evaluation)
                .stream()
                .filter(a -> !a.isSubmitted())
                .collect(Collectors.toList());

        Set<Long> notifiedEvaluators = new HashSet<>();
        for (EvaluationAssignment assignment : unsubmitted) {
            Long evaluatorId = assignment.getEvaluator().getId();
            
            // Only send one notification per evaluator per evaluation
            if (!notifiedEvaluators.contains(evaluatorId)) {
                try {
                    String message = String.format(
                            "Reminder: Evaluation '%s' is due soon (deadline: %s). Please submit your evaluation for %s.",
                            evaluation.getTitle(),
                            evaluation.getDeadline().format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")),
                            assignment.getEvaluatee().getFirstName() + " " + assignment.getEvaluatee().getLastName()
                    );
                    
                    notificationService.send(
                            assignment.getEvaluator(),
                            message,
                            NotificationType.DEADLINE_REMINDER
                    );
                    
                    notifiedEvaluators.add(evaluatorId);
                    log.debug("Sent reminder notification to evaluator {} for evaluation {}", evaluatorId, evaluation.getId());
                } catch (Exception e) {
                    log.warn("Failed to send reminder notification to evaluator {} for evaluation {}: {}",
                            evaluatorId, evaluation.getId(), e.getMessage());
                }
            }
        }
    }
}
