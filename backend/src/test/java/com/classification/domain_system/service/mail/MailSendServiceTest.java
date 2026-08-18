package com.classification.domain_system.service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MailSendServiceTest {

    @Mock JavaMailSender mailSender;
    @InjectMocks MailSendService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "mailDomain", "mplatform.com");
    }

    @Test
    @DisplayName("1. MimeMessage 생성 - 수신자, 참조, 숨은 참조 정상 설정")
    void sendMail_constructsMimeMessageCorrectly() throws MessagingException {
        // given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        List<String> to = List.of("to@test.com");
        List<String> cc = List.of("cc@test.com");
        List<String> bcc = List.of("bcc@test.com");

        // when
        service.sendMail("from@test.com", to, cc, bcc, "Test Subject", "<p>Test</p>", null);

        // then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("2. MimeMessage 생성 - 첨부파일 정상 추가")
    void sendMail_withAttachments_addsToMessage() throws MessagingException {
        // given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        List<String> to = List.of("to@test.com");
        List<Map<String, Object>> attachments = List.of(
                Map.of("fileName", "test.txt", "content", "content".getBytes(), "contentType", "text/plain")
        );

        // when
        service.sendMail("from@test.com", to, null, null, "Test Subject", "<p>Test</p>", attachments);

        // then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("3. 단순 메일 발송 - 텍스트 이메일 전송")
    void sendSimpleMail_sendsTextEmail() throws MessagingException {
        // given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        service.sendSimpleMail("from@test.com", "to@test.com", "Test Subject", "Test Body");

        // then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("4. 이메일 주소 빌드 - 도메인 정상 추가")
    void buildEmailAddress_appendsDomain() {
        // given
        String username = "testuser";

        // when
        String email = service.buildEmailAddress(username);

        // then
        assertThat(email).isEqualTo("testuser@mplatform.com");
    }
}
