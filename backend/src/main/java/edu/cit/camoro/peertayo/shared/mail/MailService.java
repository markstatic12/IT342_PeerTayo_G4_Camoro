package edu.cit.camoro.peertayo.shared.mail;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending SMTP email notifications.
 */
@Service
@RequiredArgsConstructor
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private final JavaMailSender mailSender;

    /**
     * Sends a simple text email.
     * If the mail sender is not configured or fails, it logs a warning instead of crashing.
     */
    public void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@peertayo.edu.ph");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Sent SMTP email to {}", to);
        } catch (Exception e) {
            log.warn("Failed to send SMTP email to {}: {}. Ensure spring.mail.* properties are set in .env", to, e.getMessage());
        }
    }
}
