package edu.cit.camoro.peertayo.notification.shared;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@peertayo.edu}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Sends a basic text email asynchronously so it doesn't slow down the main process.
     */
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    /**
     * Sends a rich HTML email with a "Call to Action" button.
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String title, String content, String buttonText, String buttonPath) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
            String htmlContent = String.format(
                "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>" +
                "<body style='margin:0; padding:0; background-color: #f4f7fa; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif;'>" +
                "<table width='100%%' border='0' cellspacing='0' cellpadding='0' style='background-color: #f4f7fa; padding: 48px 20px;'>" +
                "  <tr><td align='center'>" +
                "    <table width='100%%' border='0' cellspacing='0' cellpadding='0' style='max-width: 600px; background-color: #111827; border-radius: 20px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.1); border: 1px solid rgba(0,0,0,0.05);'>" +
                "      <!-- Top Accent Stripe -->" +
                "      <tr><td height='4' style='background-color: #3b82f6; line-height: 4px; font-size: 4px;'>&nbsp;</td></tr>" +
                "      <!-- Header -->" +
                "      <tr><td style='padding: 40px 40px 20px 40px;'>" +
                "        <table width='100%%' border='0' cellspacing='0' cellpadding='0'><tr>" +
                "          <td><h1 style='margin:0; font-size: 24px; font-weight: 800; color: #ffffff; letter-spacing: -0.5px;'>PeerTayo</h1>" +
                "          <div style='margin-top: 4px; height: 1px; width: 40px; background-color: #3b82f6;'></div>" +
                "          </td>" +
                "        </tr></table>" +
                "      </td></tr>" +
                "      <!-- Main Body -->" +
                "      <tr><td style='padding: 0 40px 40px 40px;'>" +
                "        <h2 style='margin: 0 0 16px 0; font-size: 22px; font-weight: 700; color: #ffffff; line-height: 1.2;'>%s</h2>" +
                "        <div style='font-size: 16px; line-height: 1.7; color: #94a3b8; margin-bottom: 32px; white-space: pre-line;'>%s</div>" +
                "        <!-- Button -->" +
                "        <table border='0' cellspacing='0' cellpadding='0'><tr><td align='left' style='border-radius: 12px; background-color: #3b82f6;'>" +
                "          <a href='%s%s' style='display: inline-block; padding: 16px 32px; font-size: 16px; font-weight: 700; color: #ffffff; text-decoration: none; border-radius: 12px; transition: background-color 0.2s;'>%s</a>" +
                "        </td></tr></table>" +
                "      </td></tr>" +
                "      <!-- Secondary Info -->" +
                "      <tr><td style='padding: 0 40px 40px 40px;'>" +
                "        <div style='padding-top: 32px; border-top: 1px solid rgba(255,255,255,0.08);'>" +
                "          <p style='margin:0; font-size: 14px; color: #64748b;'>Need to access the dashboard directly? <a href='%s' style='color: #60a5fa; text-decoration: underline;'>Click here to visit PeerTayo</a></p>" +
                "        </div>" +
                "      </td></tr>" +
                "      <!-- Footer -->" +
                "      <tr><td style='background-color: #0f172a; padding: 32px 40px; text-align: center;'>" +
                "        <p style='margin:0; font-size: 12px; color: #475569; line-height: 1.5;'>" +
                "          This is an automated performance notification from PeerTayo.<br/>" +
                "          To manage your email preferences, visit your account settings.<br/><br/>" +
                "          &copy; 2026 PeerTayo Team &bull; Criteria-Based Evaluation" +
                "        </p>" +
                "      </td></tr>" +
                "    </table>" +
                "  </td></tr>" +
                "</table>" +
                "</body></html>",
                title, content, frontendUrl, buttonPath, buttonText, frontendUrl
            );

            helper.setText(htmlContent, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(fromEmail);
            
            mailSender.send(mimeMessage);
            log.info("Premium HTML Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send premium HTML email to {}", to, e);
        }
    }
}
