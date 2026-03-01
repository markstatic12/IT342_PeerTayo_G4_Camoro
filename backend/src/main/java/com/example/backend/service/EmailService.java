package com.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    /**
     * Sends a welcome email to a newly registered user.
     * Executed asynchronously so it never blocks the registration response.
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Welcome to PeerTayo!");
            message.setText(buildWelcomeBody(firstName));
            mailSender.send(message);
            log.info("Welcome email sent to {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Notifies a user that they have been assigned as an evaluator.
     * Executed asynchronously.
     */
    @Async
    public void sendEvaluationAssignmentEmail(String toEmail, String firstName,
                                               String evaluationTitle) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("New Peer Evaluation Assigned – " + evaluationTitle);
            message.setText(buildAssignmentBody(firstName, evaluationTitle));
            mailSender.send(message);
            log.info("Assignment email sent to {} for evaluation '{}'", toEmail, evaluationTitle);
        } catch (MailException e) {
            log.error("Failed to send assignment email to {}: {}", toEmail, e.getMessage());
        }
    }

    // ── Email body builders ──────────────────────────────────────────────────

    private String buildWelcomeBody(String firstName) {
        return "Hi " + firstName + ",\n\n"
                + "Welcome to PeerTayo – your criteria-based peer evaluation platform!\n\n"
                + "Your account has been created successfully.\n"
                + "You can now log in and start participating in peer evaluations.\n\n"
                + "If you did not create this account, please contact support immediately.\n\n"
                + "— The PeerTayo Team";
    }

    private String buildAssignmentBody(String firstName, String evaluationTitle) {
        return "Hi " + firstName + ",\n\n"
                + "You have been assigned as an evaluator for:\n\n"
                + "  \"" + evaluationTitle + "\"\n\n"
                + "Please log in to PeerTayo and complete your evaluation before the deadline.\n\n"
                + "— The PeerTayo Team";
    }
}
