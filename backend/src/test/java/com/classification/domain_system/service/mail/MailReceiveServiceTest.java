package com.classification.domain_system.service.mail;

import com.classification.domain_system.entity.InboxMessage;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.InboxMessageRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.service.storage.FileStorageService;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeBodyPart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailReceiveServiceTest {

    @Mock InboxMessageRepository inboxMessageRepository;
    @Mock UserRepository userRepository;
    @Mock FileStorageService fileStorageService;

    @InjectMocks MailReceiveService service;

    @Captor ArgumentCaptor<InboxMessage> messageCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "mailDomain", "mplatform.com");
    }

    @Test
    @DisplayName("1. 수신 이메일 처리 - 메시지 정상 저장")
    void processIncomingMail_savesMessageCorrectly() throws Exception {
        // given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mimeMessage.getMessageID()).thenReturn("msg123");
        when(mimeMessage.getSubject()).thenReturn("Test Subject");
        when(mimeMessage.getFrom()).thenReturn(new Address[]{new InternetAddress("sender@test.com")});
        when(mimeMessage.getSentDate()).thenReturn(new Date());
        when(mimeMessage.isMimeType("text/html")).thenReturn(true);
        when(mimeMessage.getContent()).thenReturn("<p>Body</p>");
        
        when(inboxMessageRepository.findByExternalMessageId("msg123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(new User()));

        // when
        service.processIncomingMail(mimeMessage);

        // then
        verify(inboxMessageRepository).save(messageCaptor.capture());
        InboxMessage saved = messageCaptor.getValue();
        assertThat(saved.getExternalMessageId()).isEqualTo("msg123");
        assertThat(saved.getSubject()).isEqualTo("Test Subject");
        assertThat(saved.getBody()).isEqualTo("<p>Body</p>");
        assertThat(saved.getSenderEmail()).isEqualTo("sender@test.com");
    }

    @Test
    @DisplayName("2. 수신 이메일 처리 - 중복 이메일 무시")
    void processIncomingMail_skipsDuplicate() throws Exception {
        // given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mimeMessage.getMessageID()).thenReturn("msg123");
        
        when(inboxMessageRepository.findByExternalMessageId("msg123")).thenReturn(Optional.of(new InboxMessage()));

        // when
        service.processIncomingMail(mimeMessage);

        // then
        verify(mimeMessage, never()).getSubject();
        verify(inboxMessageRepository, never()).save(any());
    }

    @Test
    @DisplayName("3. 수신 이메일 처리 - 내부 사용자 매핑")
    void processIncomingMail_mapsInternalUserByEmail() throws Exception {
        // given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mimeMessage.getMessageID()).thenReturn("msg123");
        when(mimeMessage.getSubject()).thenReturn("Test Subject");
        when(mimeMessage.getFrom()).thenReturn(new Address[]{new InternetAddress("sender@test.com")});
        when(mimeMessage.isMimeType("text/html")).thenReturn(false);
        when(mimeMessage.isMimeType("text/plain")).thenReturn(true);
        when(mimeMessage.getContent()).thenReturn("Plain text body");
        
        when(mimeMessage.getRecipients(Message.RecipientType.TO)).thenReturn(new Address[]{
                new InternetAddress("user1@mplatform.com")
        });

        when(inboxMessageRepository.findByExternalMessageId("msg123")).thenReturn(Optional.empty());
        
        User internalUser = new User();
        internalUser.setId("internal-user-id");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(internalUser));

        // when
        service.processIncomingMail(mimeMessage);

        // then
        verify(inboxMessageRepository).save(messageCaptor.capture());
        InboxMessage saved = messageCaptor.getValue();
        assertThat(saved.getRecipients()).hasSize(1);
        assertThat(saved.getRecipients().get(0).getUserId()).isEqualTo("internal-user-id");
    }

    @Test
    @DisplayName("4. 수신 이메일 처리 - 첨부파일 추출 및 저장")
    void processIncomingMail_extractsAttachments() throws Exception {
        // given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mimeMessage.getMessageID()).thenReturn("msg123");
        when(mimeMessage.getFrom()).thenReturn(new Address[]{new InternetAddress("sender@test.com")});
        when(mimeMessage.isMimeType("text/html")).thenReturn(false);
        when(mimeMessage.isMimeType("text/plain")).thenReturn(false);
        when(mimeMessage.isMimeType("multipart/*")).thenReturn(true);

        MimeMultipart multipart = mock(MimeMultipart.class);
        when(mimeMessage.getContent()).thenReturn(multipart);
        when(multipart.getCount()).thenReturn(1);

        MimeBodyPart attachmentPart = mock(MimeBodyPart.class);
        when(multipart.getBodyPart(0)).thenReturn(attachmentPart);
        when(attachmentPart.getDisposition()).thenReturn("attachment");
        when(attachmentPart.getFileName()).thenReturn("test.txt");
        when(attachmentPart.getContentType()).thenReturn("text/plain");
        when(attachmentPart.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("content".getBytes()));

        when(inboxMessageRepository.findByExternalMessageId("msg123")).thenReturn(Optional.empty());
        when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn("/path/to/test.txt");

        // when
        service.processIncomingMail(mimeMessage);

        // then
        verify(fileStorageService).storeFile(any(MultipartFile.class));
        verify(inboxMessageRepository).save(messageCaptor.capture());
        InboxMessage saved = messageCaptor.getValue();
        assertThat(saved.getAttachments()).hasSize(1);
        assertThat(saved.getAttachments().get(0).getFileName()).isEqualTo("test.txt");
        assertThat(saved.getAttachments().get(0).getFilePath()).isEqualTo("/path/to/test.txt");
    }

    @Test
    @DisplayName("5. 수신 이메일 처리 - 헤더에서 중요도 추출")
    void processIncomingMail_setsImportanceFromHeader() throws Exception {
        // given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mimeMessage.getMessageID()).thenReturn("msg123");
        when(mimeMessage.getFrom()).thenReturn(new Address[]{new InternetAddress("sender@test.com")});
        when(mimeMessage.getHeader("Importance")).thenReturn(new String[]{"High"});
        when(mimeMessage.isMimeType("text/html")).thenReturn(false);
        when(mimeMessage.isMimeType("text/plain")).thenReturn(true);
        when(mimeMessage.getContent()).thenReturn("Body");

        when(inboxMessageRepository.findByExternalMessageId("msg123")).thenReturn(Optional.empty());

        // when
        service.processIncomingMail(mimeMessage);

        // then
        verify(inboxMessageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getImportance()).isEqualTo("HIGH");
    }
}
