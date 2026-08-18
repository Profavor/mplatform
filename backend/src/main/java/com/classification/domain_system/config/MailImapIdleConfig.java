package com.classification.domain_system.config;

import com.classification.domain_system.service.mail.MailReceiveService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mail.dsl.Mail;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * IMAP IDLE configuration for real-time email receiving.
 * Connects to the mail server via IMAP/IMAPS and listens for new emails.
 * Controlled by property `mail.imap.enabled` (default: true).
 */
@Configuration
@EnableIntegration
@ConditionalOnProperty(name = "mail.imap.enabled", havingValue = "true", matchIfMissing = true)
public class MailImapIdleConfig {

    private static final Logger log = LoggerFactory.getLogger(MailImapIdleConfig.class);

    @Value("${mail.imap.host:localhost}")
    private String imapHost;

    @Value("${mail.imap.port:993}")
    private int imapPort;

    @Value("${mail.imap.username:admin@mplatform.com}")
    private String imapUsername;

    @Value("${mail.imap.password:AdminMailPass123!}")
    private String imapPassword;

    @Bean
    public IntegrationFlow imapIdleFlow(MailReceiveService mailReceiveService) {
        String encodedUser = URLEncoder.encode(imapUsername, StandardCharsets.UTF_8);
        String encodedPass = URLEncoder.encode(imapPassword, StandardCharsets.UTF_8);
        
        boolean isSsl = (imapPort == 993 || imapPort == 465);
        String protocol = isSsl ? "imaps" : "imap";
        String imapUrl = String.format("%s://%s:%s@%s:%d/INBOX", protocol, encodedUser, encodedPass, imapHost, imapPort);

        log.info("Configuring IMAP IDLE listener ({}) for host: {}:{}", protocol, imapHost, imapPort);

        return IntegrationFlow.from(Mail.imapIdleAdapter(imapUrl)
                        .autoStartup(true)
                        .shouldMarkMessagesAsRead(true)
                        .javaMailProperties(p -> {
                            p.put("mail.imaps.ssl.enable", String.valueOf(isSsl));
                            p.put("mail.imaps.ssl.trust", "*");
                            p.put("mail.imap.ssl.trust", "*");
                            p.put("mail.imaps.ssl.checkserveridentity", "false");
                            p.put("mail.imap.ssl.checkserveridentity", "false");
                            p.put("mail.imaps.ssl.protocols", "TLSv1.2 TLSv1.3");
                            p.put("mail.imap.ssl.protocols", "TLSv1.2 TLSv1.3");
                            p.put("mail.debug", "false");
                            p.put("mail.imaps.connectiontimeout", "10000");
                            p.put("mail.imaps.timeout", "10000");
                            p.put("mail.imap.connectiontimeout", "10000");
                            p.put("mail.imap.timeout", "10000");
                        }))
                .handle(message -> {
                    try {
                        if (message.getPayload() instanceof MimeMessage mimeMessage) {
                            mailReceiveService.processIncomingMail(mimeMessage);
                        }
                    } catch (Exception e) {
                        log.error("Failed to process incoming email", e);
                    }
                })
                .get();
    }
}
