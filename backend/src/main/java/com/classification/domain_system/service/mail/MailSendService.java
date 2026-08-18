package com.classification.domain_system.service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for sending emails via SMTP through the mail server.
 */
@Service
public class MailSendService {

    private static final Logger log = LoggerFactory.getLogger(MailSendService.class);

    private final JavaMailSender mailSender;

    @Value("${mail.domain:mplatform.com}")
    private String mailDomain;

    public MailSendService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send an HTML email with optional CC, BCC, and attachments.
     */
    public void sendMail(String from, List<String> to, List<String> cc, List<String> bcc,
                         String subject, String htmlBody, List<Map<String, Object>> attachments) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        boolean hasAttachments = attachments != null && !attachments.isEmpty();
        MimeMessageHelper helper = new MimeMessageHelper(message, hasAttachments, "UTF-8");

        helper.setFrom(from);
        helper.setTo(to.toArray(new String[0]));
        if (cc != null && !cc.isEmpty()) {
            helper.setCc(cc.toArray(new String[0]));
        }
        if (bcc != null && !bcc.isEmpty()) {
            helper.setBcc(bcc.toArray(new String[0]));
        }
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        if (hasAttachments) {
            for (Map<String, Object> attachment : attachments) {
                String fileName = (String) attachment.get("fileName");
                byte[] content = (byte[]) attachment.get("content");
                String contentType = (String) attachment.get("contentType");
                helper.addAttachment(fileName, new ByteArrayResource(content), contentType);
            }
        }

        mailSender.send(message);
        log.info("Email sent from {} to {} recipients, subject: {}", from, to.size(), subject);
    }

    /**
     * Send a simple text email.
     */
    public void sendSimpleMail(String from, String to, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, false);
        mailSender.send(message);
        log.info("Simple email sent from {} to {}, subject: {}", from, to, subject);
    }

    /**
     * Build the full email address for an internal user.
     */
    public String buildEmailAddress(String username) {
        return username + "@" + mailDomain;
    }
}
