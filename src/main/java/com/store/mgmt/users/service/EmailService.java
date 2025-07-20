package com.store.mgmt.users.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${FRONTEND_URL")
    private String frontendUrl;

    @Value("${spring.mail.from}")
    private String fromEmail;

    public void sendInvitationEmail(String toEmail, String token, String organizationName, String roleName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Invitation to Join " + organizationName);
            String registrationLink = frontendUrl + "/register?token=" + token;
            String htmlContent = """
                    <h2>Invitation to Join %s</h2>
                    <p>You have been invited to join %s as a %s.</p>
                    <p>Please click the link below to register and activate your account:</p>
                    <p><a href="%s">Register Now</a></p>
                    <p>This invitation will expire in 7 days.</p>
                    <p>If you did not expect this invitation, please ignore this email.</p>
                    """.formatted(organizationName, organizationName, roleName, registrationLink);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Sent invitation email to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send invitation email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send invitation email", e);
        }
    }
}