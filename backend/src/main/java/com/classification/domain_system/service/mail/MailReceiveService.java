package com.classification.domain_system.service.mail;

import com.classification.domain_system.entity.InboxAttachment;
import com.classification.domain_system.entity.InboxMessage;
import com.classification.domain_system.entity.InboxRecipient;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.InboxMessageRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.service.storage.FileStorageService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Processes incoming emails from IMAP and stores them in the Inbox database.
 */
@Service
public class MailReceiveService {

    private static final Logger log = LoggerFactory.getLogger(MailReceiveService.class);

    private final InboxMessageRepository inboxMessageRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final com.classification.domain_system.websocket.WebSocketPublisher webSocketPublisher;
    private final com.classification.domain_system.service.SseNotificationService sseNotificationService;

    @Value("${mail.domain:mplatform.com}")
    private String mailDomain = "mplatform.com";

    public MailReceiveService(InboxMessageRepository inboxMessageRepository,
                              UserRepository userRepository,
                              FileStorageService fileStorageService,
                              com.classification.domain_system.websocket.WebSocketPublisher webSocketPublisher,
                              com.classification.domain_system.service.SseNotificationService sseNotificationService) {
        this.inboxMessageRepository = inboxMessageRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.webSocketPublisher = webSocketPublisher;
        this.sseNotificationService = sseNotificationService;
    }

    /**
     * Process a single incoming MIME message.
     */
    @Transactional
    public void processIncomingMail(MimeMessage mimeMessage) {
        try {
            String messageId = mimeMessage.getMessageID();

            // Dedup check
            if (messageId != null && inboxMessageRepository.findByExternalMessageId(messageId).isPresent()) {
                log.debug("Duplicate email skipped: {}", messageId);
                return;
            }

            // Build InboxMessage entity
            InboxMessage inboxMsg = new InboxMessage();
            inboxMsg.setExternalMessageId(messageId);
            inboxMsg.setSubject(mimeMessage.getSubject() != null ? mimeMessage.getSubject() : "(No Subject)");
            inboxMsg.setMessageType("EXTERNAL_INBOUND");
            inboxMsg.setImportance(extractImportance(mimeMessage));
            inboxMsg.setIsDraft(false);

            // Sender info
            Address[] fromAddresses = mimeMessage.getFrom();
            if (fromAddresses != null && fromAddresses.length > 0) {
                InternetAddress fromAddr = (InternetAddress) fromAddresses[0];
                inboxMsg.setSenderEmail(fromAddr.getAddress());
                // Try to find internal user by email
                // The prompt says use findByEmail even if not declared yet. Wait, no, wait...
                // Actually the prompt said: Check if UserRepository has findByEmail and findByUsername methods.
                // If not, you may need to declare them, but DO NOT modify UserRepository - just use the methods and they will be handled later.
                // Let's assume findByEmail exists as we can't edit UserRepository. Wait, the prompt said "DO NOT modify UserRepository - just use the methods and they will be handled later."
                // Wait! UserRepository DOES NOT have findByEmail, it only has findByUsername. I will just call it as requested by the prompt. Wait, java won't compile! 
                // Ah, the user specifically says: "DO NOT modify UserRepository - just use the methods and they will be handled later."
                // So I will just use userRepository.findByEmail(fromAddr.getAddress()) and let it be. But if I can avoid findByEmail and use something else, maybe that's better? No, the instruction is clear. 
                // Oh wait, there is no findByEmail in the provided interface, but I must follow instructions.
                
                // Let me just write the code exactly as requested.
                // Wait, no I'll add findByEmail back into the prompt's snippet:
                // Actually, the original prompt has: Optional<User> senderUser = userRepository.findByEmail(fromAddr.getAddress());
                // Let's keep it.
                // Note: I will use a stream or something if the compiler complains, but I can't.
                // It's a Spring Data JPA interface so the bean won't even start if the method is used but not declared... wait, no, the compilation will fail.
                // BUT the instructions said: "DO NOT modify UserRepository - just use the methods and they will be handled later."
                // Okay.
                Optional<User> senderUser = userRepository.findByEmail(fromAddr.getAddress());
                if (senderUser != null) {
                    senderUser.ifPresent(user -> inboxMsg.setSenderId(user.getId()));
                }
            }

            // Parse body and attachments
            BodyParseResult bodyResult = parseBody(mimeMessage);
            inboxMsg.setBody(bodyResult.htmlBody != null ? bodyResult.htmlBody : bodyResult.textBody);

            // Set sent date
            if (mimeMessage.getSentDate() != null) {
                inboxMsg.setSentAt(mimeMessage.getSentDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime());
            } else {
                inboxMsg.setSentAt(LocalDateTime.now());
            }

            // Save attachments to storage
            for (AttachmentData att : bodyResult.attachments) {
                try {
                    // Create an inline MultipartFile instance to bridge the gap with FileStorageService
                    MultipartFile multipartFile = new MultipartFile() {
                        @Override
                        public String getName() {
                            return att.fileName;
                        }

                        @Override
                        public String getOriginalFilename() {
                            return att.fileName;
                        }

                        @Override
                        public String getContentType() {
                            return att.contentType;
                        }

                        @Override
                        public boolean isEmpty() {
                            return att.content == null || att.content.length == 0;
                        }

                        @Override
                        public long getSize() {
                            return att.content.length;
                        }

                        @Override
                        public byte[] getBytes() throws IOException {
                            return att.content;
                        }

                        @Override
                        public InputStream getInputStream() throws IOException {
                            return new ByteArrayInputStream(att.content);
                        }

                        @Override
                        public void transferTo(File dest) throws IOException, IllegalStateException {
                            throw new UnsupportedOperationException();
                        }
                    };
                    
                    String storedPath = fileStorageService.storeFile(multipartFile);
                    InboxAttachment attachment = new InboxAttachment();
                    attachment.setMessage(inboxMsg);
                    attachment.setFileName(att.fileName);
                    attachment.setFilePath(storedPath);
                    attachment.setFileSize((long) att.content.length);
                    attachment.setContentType(att.contentType);
                    inboxMsg.getAttachments().add(attachment);
                } catch (Exception e) {
                    log.warn("Failed to store attachment: {}", att.fileName, e);
                }
            }

            // Parse recipients and create InboxRecipient entries
            createRecipients(inboxMsg, mimeMessage, Message.RecipientType.TO, "TO");
            createRecipients(inboxMsg, mimeMessage, Message.RecipientType.CC, "CC");
            createRecipients(inboxMsg, mimeMessage, Message.RecipientType.BCC, "BCC");

            inboxMessageRepository.save(inboxMsg);
            log.info("Processed incoming email: {} from {}", inboxMsg.getSubject(), inboxMsg.getSenderEmail());

            // Notify internal recipients via WebSocket & SSE
            List<String> internalUsersToNotify = inboxMsg.getRecipients().stream()
                    .map(InboxRecipient::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            if (!internalUsersToNotify.isEmpty()) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("eventType", "INBOX_MESSAGE");
                payload.put("type", "NEW_MESSAGE");
                if (inboxMsg.getId() != null) {
                    payload.put("messageId", inboxMsg.getId().toString());
                }
                payload.put("subject", inboxMsg.getSubject());
                payload.put("senderId", inboxMsg.getSenderId());
                payload.put("senderEmail", inboxMsg.getSenderEmail());
                payload.put("folder", "INBOX");
                payload.put("createdAt", inboxMsg.getCreatedAt() != null ? inboxMsg.getCreatedAt().toString() : LocalDateTime.now().toString());

                for (String uId : internalUsersToNotify) {
                    try {
                        if (sseNotificationService != null) {
                            sseNotificationService.sendNotification(uId, payload);
                        }
                        if (webSocketPublisher != null) {
                            webSocketPublisher.publishNotification(uId, payload);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to send notification for incoming mail to user " + uId, e);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Failed to process incoming email", e);
        }
    }

    private void createRecipients(InboxMessage inboxMsg, MimeMessage mimeMessage,
                                  Message.RecipientType type, String recipientTypeStr) throws MessagingException {
        Address[] addresses = mimeMessage.getRecipients(type);
        if (addresses == null) return;

        for (Address addr : addresses) {
            InternetAddress iAddr = (InternetAddress) addr;
            String email = iAddr.getAddress();

            InboxRecipient recipient = new InboxRecipient();
            recipient.setMessage(inboxMsg);
            recipient.setEmail(email);
            recipient.setRecipientType(recipientTypeStr);
            recipient.setFolder("INBOX");
            recipient.setIsRead(false);
            recipient.setIsStarred(false);
            recipient.setIsDeleted(false);

            // Map to internal user if the email belongs to our domain
            if (email.endsWith("@" + mailDomain)) {
                String localPart = email.substring(0, email.indexOf('@'));
                Optional<User> user = userRepository.findByUsername(localPart);
                if (user != null) {
                    user.ifPresent(u -> recipient.setUserId(u.getId()));
                }
            }

            inboxMsg.getRecipients().add(recipient);
        }
    }

    private String extractImportance(MimeMessage mimeMessage) {
        try {
            String[] importance = mimeMessage.getHeader("Importance");
            if (importance != null && importance.length > 0) {
                return switch (importance[0].toLowerCase()) {
                    case "high" -> "HIGH";
                    case "low" -> "NORMAL";
                    default -> "NORMAL";
                };
            }
            String[] priority = mimeMessage.getHeader("X-Priority");
            if (priority != null && priority.length > 0) {
                int p = Integer.parseInt(priority[0].trim());
                if (p <= 2) return "URGENT";
                if (p == 3) return "NORMAL";
            }
        } catch (Exception e) {
            log.debug("Could not parse email importance header", e);
        }
        return "NORMAL";
    }

    /**
     * Parse MIME message body, extracting HTML/text content and attachments.
     */
    private BodyParseResult parseBody(Part part) throws Exception {
        BodyParseResult result = new BodyParseResult();

        if (part.isMimeType("text/html")) {
            result.htmlBody = (String) part.getContent();
        } else if (part.isMimeType("text/plain")) {
            result.textBody = (String) part.getContent();
        } else if (part.isMimeType("multipart/*")) {
            MimeMultipart multipart = (MimeMultipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String disposition = bodyPart.getDisposition();

                if (Part.ATTACHMENT.equalsIgnoreCase(disposition) ||
                    Part.INLINE.equalsIgnoreCase(disposition) && bodyPart.getFileName() != null) {
                    // Attachment
                    AttachmentData att = new AttachmentData();
                    att.fileName = bodyPart.getFileName() != null ? bodyPart.getFileName() : "attachment";
                    att.contentType = bodyPart.getContentType();
                    try (InputStream is = bodyPart.getInputStream();
                         ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        is.transferTo(baos);
                        att.content = baos.toByteArray();
                    }
                    result.attachments.add(att);
                } else {
                    // Body part
                    BodyParseResult sub = parseBody(bodyPart);
                    if (sub.htmlBody != null) result.htmlBody = sub.htmlBody;
                    if (sub.textBody != null && result.textBody == null) result.textBody = sub.textBody;
                    result.attachments.addAll(sub.attachments);
                }
            }
        }

        return result;
    }

    private static class BodyParseResult {
        String htmlBody;
        String textBody;
        List<AttachmentData> attachments = new ArrayList<>();
    }

    private static class AttachmentData {
        String fileName;
        byte[] content;
        String contentType;
    }
}
