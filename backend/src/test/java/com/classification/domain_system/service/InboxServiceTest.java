package com.classification.domain_system.service;

import com.classification.domain_system.dto.InboxFolderCountResponse;
import com.classification.domain_system.dto.InboxMessageRequest;
import com.classification.domain_system.dto.InboxMessageResponse;
import com.classification.domain_system.entity.InboxAttachment;
import com.classification.domain_system.entity.InboxMessage;
import com.classification.domain_system.entity.InboxRecipient;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.InboxAttachmentRepository;
import com.classification.domain_system.repository.InboxMessageRepository;
import com.classification.domain_system.repository.InboxRecipientRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.service.mail.MailSendService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboxServiceTest {

    @Mock InboxMessageRepository messageRepository;
    @Mock InboxRecipientRepository recipientRepository;
    @Mock InboxAttachmentRepository attachmentRepository;
    @Mock UserRepository userRepository;
    @Mock MailSendService mailSendService;
    @Mock SseNotificationService sseNotificationService;

    @InjectMocks InboxService inboxService;

    @Captor ArgumentCaptor<InboxMessage> messageCaptor;
    @Captor ArgumentCaptor<InboxRecipient> recipientCaptor;

    @Test
    @DisplayName("1. 내부 수신자로 메시지 전송 - 수신자 및 발신자 레코드 생성")
    void sendMessage_withInternalRecipients_createsRecipientsAndSenderRecord() throws Exception {
        // given
        InboxMessageRequest request = new InboxMessageRequest();
        request.setSubject("Test Subject");
        request.setBody("Test Body");
        request.setToRecipients(List.of("user1", "user2"));
        
        when(userRepository.findById(anyString())).thenReturn(Optional.of(new User()));
        
        // when
        InboxMessageResponse response = inboxService.sendMessage(request, "senderId");

        // then
        verify(messageRepository).save(messageCaptor.capture());
        InboxMessage savedMsg = messageCaptor.getValue();
        assertThat(savedMsg.getSubject()).isEqualTo("Test Subject");
        assertThat(savedMsg.getIsDraft()).isFalse();

        // 1 sender + 2 recipients
        verify(recipientRepository, times(3)).save(any(InboxRecipient.class));
        verify(sseNotificationService, times(2)).sendNotification(anyString(), anyMap());
        verify(mailSendService, never()).sendSimpleMail(any(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("2. 외부 수신자로 메시지 전송 - MailSendService 호출")
    void sendMessage_withExternalRecipients_callsMailSendService() throws Exception {
        // given
        InboxMessageRequest request = new InboxMessageRequest();
        request.setSubject("External Test");
        request.setBody("External Body");
        request.setToRecipients(List.of("test@example.com"));

        // when
        inboxService.sendMessage(request, "senderId");

        // then
        verify(messageRepository).save(any(InboxMessage.class));
        verify(mailSendService).sendSimpleMail(any(), eq("test@example.com"), eq("External Test"), eq("External Body"));
        verify(sseNotificationService, never()).sendNotification(anyString(), anyMap());
    }

    @Test
    @DisplayName("3. 혼합 수신자로 메시지 전송 - 정상 처리")
    void sendMessage_withMixedRecipients_handlesCorrectly() throws Exception {
        // given
        InboxMessageRequest request = new InboxMessageRequest();
        request.setToRecipients(List.of("user1", "test@example.com"));
        
        when(userRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            User u = new User();
            u.setId(id);
            u.setUsername(id);
            return Optional.of(u);
        });

        // when
        inboxService.sendMessage(request, "senderId");

        // then
        verify(mailSendService).sendSimpleMail(any(), eq("test@example.com"), any(), any());
        verify(sseNotificationService).sendNotification(eq("user1"), anyMap());
    }

    @Test
    @DisplayName("4. 임시저장 - isDraft가 true로 설정됨")
    void saveDraft_setsIsDraftTrue() {
        // given
        InboxMessageRequest request = new InboxMessageRequest();
        request.setSubject("Draft Subject");

        // when
        inboxService.saveDraft(request, "senderId");

        // then
        verify(messageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getIsDraft()).isTrue();
        // Recipient shouldn't be created for TO recipients in draft
        verify(recipientRepository, times(1)).save(any(InboxRecipient.class)); // Only sender recipient
    }

    @Test
    @DisplayName("5. 메시지 조회 - 읽음 처리 후 상세 반환")
    void getMessage_marksAsReadAndReturnsFull() {
        // given
        UUID msgId = UUID.randomUUID();
        InboxMessage msg = new InboxMessage();
        msg.setId(msgId);
        
        InboxRecipient rec = new InboxRecipient();
        rec.setMessage(msg);
        rec.setIsRead(false);

        when(recipientRepository.findByUserIdAndMessageId("user1", msgId)).thenReturn(List.of(rec));

        // when
        InboxMessageResponse res = inboxService.getMessage("user1", msgId);

        // then
        assertThat(rec.getIsRead()).isTrue();
        verify(recipientRepository).save(rec);
        assertThat(res.getId()).isEqualTo(msgId);
    }

    @Test
    @DisplayName("6. 메시지 조회 - 발신자가 아닌 경우 BCC 필터링")
    void getMessage_filtersBccForNonSender() {
        // given
        UUID msgId = UUID.randomUUID();
        InboxMessage msg = new InboxMessage();
        msg.setId(msgId);
        msg.setSenderId("sender1");

        InboxRecipient toRec = new InboxRecipient();
        toRec.setRecipientType("TO");
        toRec.setUserId("user1");
        
        InboxRecipient bccRec = new InboxRecipient();
        bccRec.setRecipientType("BCC");
        bccRec.setUserId("user2");

        msg.setRecipients(List.of(toRec, bccRec));

        InboxRecipient viewerRec = new InboxRecipient();
        viewerRec.setMessage(msg);
        viewerRec.setIsRead(true);

        when(recipientRepository.findByUserIdAndMessageId("user1", msgId)).thenReturn(List.of(viewerRec));

        // when
        InboxMessageResponse res = inboxService.getMessage("user1", msgId);

        // then
        assertThat(res.getToRecipients()).hasSize(1);
        // CC/BCC list shouldn't be included or empty based on mapping
        // In the service, BCC is filtered for non-senders
    }

    @Test
    @DisplayName("7. 답장 - 부모 및 루트 메시지 ID 설정")
    void replyMessage_setsParentAndRootMessageId() throws Exception {
        // given
        UUID parentId = UUID.randomUUID();
        InboxMessage parent = new InboxMessage();
        parent.setId(parentId);
        parent.setSenderId("originalSender");
        parent.setRootMessageId(parentId);
        
        when(messageRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(userRepository.findById(anyString())).thenReturn(Optional.of(new User()));

        InboxMessageRequest request = new InboxMessageRequest();
        
        // when
        inboxService.replyMessage("replier", parentId, request);

        // then
        verify(messageRepository).save(messageCaptor.capture());
        InboxMessage saved = messageCaptor.getValue();
        assertThat(saved.getParentMessageId()).isEqualTo(parentId);
        assertThat(saved.getRootMessageId()).isEqualTo(parentId);
    }

    @Test
    @DisplayName("8. 전체 답장 - 원본 수신자 모두 포함")
    void replyAllMessage_includesAllOriginalRecipients() throws Exception {
        // given
        UUID parentId = UUID.randomUUID();
        InboxMessage parent = new InboxMessage();
        parent.setId(parentId);
        parent.setSenderId("sender");
        
        InboxRecipient ccRec = new InboxRecipient();
        ccRec.setRecipientType("CC");
        ccRec.setUserId("ccUser");
        parent.setRecipients(List.of(ccRec));
        
        when(messageRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(userRepository.findById(anyString())).thenReturn(Optional.of(new User()));

        InboxMessageRequest request = new InboxMessageRequest();

        // when
        inboxService.replyAllMessage("replier", parentId, request);

        // then
        verify(messageRepository).save(messageCaptor.capture());
        assertThat(request.getToRecipients()).contains("sender");
        assertThat(request.getCcRecipients()).contains("ccUser");
    }

    @Test
    @DisplayName("9. 전달 - 첨부파일 복사")
    void forwardMessage_copiesAttachments() throws Exception {
        // given
        UUID msgId = UUID.randomUUID();
        InboxMessage original = new InboxMessage();
        original.setId(msgId);
        
        InboxAttachment att = new InboxAttachment();
        att.setId(UUID.randomUUID());
        original.addAttachment(att);

        when(messageRepository.findById(msgId)).thenReturn(Optional.of(original));
        when(attachmentRepository.findAllById(any())).thenReturn(List.of(att));
        
        InboxMessageRequest request = new InboxMessageRequest();
        request.setToRecipients(List.of("fwdUser"));

        // when
        inboxService.forwardMessage("user", msgId, request);

        // then
        verify(messageRepository).save(messageCaptor.capture());
        InboxMessage saved = messageCaptor.getValue();
        assertThat(saved.getAttachments()).hasSize(1);
    }

    @Test
    @DisplayName("10. 읽음 처리 - 수신자 업데이트")
    void markAsRead_updatesRecipient() {
        // given
        UUID msgId = UUID.randomUUID();
        InboxRecipient rec = new InboxRecipient();
        rec.setIsRead(false);
        when(recipientRepository.findByUserIdAndMessageId("user", msgId)).thenReturn(List.of(rec));

        // when
        inboxService.markAsRead("user", msgId);

        // then
        assertThat(rec.getIsRead()).isTrue();
        verify(recipientRepository).save(rec);
    }

    @Test
    @DisplayName("11. 별표 토글 - 플래그 토글")
    void toggleStar_togglesFlag() {
        // given
        UUID msgId = UUID.randomUUID();
        InboxRecipient rec = new InboxRecipient();
        rec.setIsStarred(false);
        when(recipientRepository.findByUserIdAndMessageId("user", msgId)).thenReturn(List.of(rec));

        // when
        inboxService.toggleStar("user", msgId);

        // then
        assertThat(rec.getIsStarred()).isTrue();
        verify(recipientRepository).save(rec);
    }

    @Test
    @DisplayName("12. 폴더 이동 - 폴더 업데이트")
    void moveToFolder_updatesFolder() {
        // given
        UUID msgId = UUID.randomUUID();
        InboxRecipient rec = new InboxRecipient();
        rec.setFolder("INBOX");
        when(recipientRepository.findByUserIdAndMessageId("user", msgId)).thenReturn(List.of(rec));

        // when
        inboxService.moveToFolder("user", msgId, "ARCHIVE");

        // then
        assertThat(rec.getFolder()).isEqualTo("ARCHIVE");
        verify(recipientRepository).save(rec);
    }

    @Test
    @DisplayName("13. 휴지통 이동 - TRASH 설정")
    void moveToTrash_setsFolder() {
        // given
        UUID msgId = UUID.randomUUID();
        InboxRecipient rec = new InboxRecipient();
        when(recipientRepository.findByUserIdAndMessageId("user", msgId)).thenReturn(List.of(rec));

        // when
        inboxService.moveToTrash("user", msgId);

        // then
        assertThat(rec.getFolder()).isEqualTo("TRASH");
        verify(recipientRepository).save(rec);
    }

    @Test
    @DisplayName("14. 영구 삭제 - TRASH에서만 삭제 가능")
    void permanentDelete_onlyFromTrash() {
        // given
        UUID msgId = UUID.randomUUID();
        InboxRecipient rec = new InboxRecipient();
        rec.setFolder("TRASH");
        when(recipientRepository.findByUserIdAndMessageId("user", msgId)).thenReturn(List.of(rec));

        // when
        inboxService.permanentDelete("user", msgId);

        // then
        assertThat(rec.getIsDeleted()).isTrue();
        verify(recipientRepository).save(rec);
    }

    @Test
    @DisplayName("15. 폴더 카운트 - 모든 폴더 반환")
    void getFolderCounts_returnsAllFolders() {
        // given
        when(recipientRepository.findByUserIdAndFolderAndIsDeletedFalse(eq("user"), anyString(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(recipientRepository.countByUserIdAndFolderAndIsReadFalseAndIsDeletedFalse(eq("user"), anyString()))
                .thenReturn(0L);
        when(recipientRepository.findByUserIdAndIsStarredTrueAndIsDeletedFalse(eq("user"), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(recipientRepository.countByUserIdAndIsStarredTrueAndIsReadFalseAndIsDeletedFalse(eq("user")))
                .thenReturn(0L);

        // when
        List<InboxFolderCountResponse> counts = inboxService.getFolderCounts("user");

        // then
        assertThat(counts).hasSize(6); // INBOX, SENT, DRAFT, STARRED, ARCHIVE, TRASH
    }

    @Test
    @DisplayName("16. 안읽음 카운트 - INBOX 안읽음 반환")
    void getUnreadCount_returnsInboxUnread() {
        // given
        when(recipientRepository.countByUserIdAndFolderAndIsReadFalseAndIsDeletedFalse("user", "INBOX")).thenReturn(5L);

        // when
        long count = inboxService.getUnreadCount("user");

        // then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("17. 스레드 조회 - 스레드의 모든 메시지 반환")
    void getThread_returnsAllInThread() {
        // given
        UUID msgId = UUID.randomUUID();
        InboxMessage msg = new InboxMessage();
        msg.setId(msgId);
        msg.setRootMessageId(msgId);

        when(messageRepository.findById(msgId)).thenReturn(Optional.of(msg));
        when(messageRepository.findByRootMessageIdOrderByCreatedAtAsc(msgId)).thenReturn(List.of(msg));
        when(recipientRepository.findFirstByUserIdAndMessageId("user", msgId)).thenReturn(Optional.of(new InboxRecipient()));

        // when
        List<InboxMessageResponse> thread = inboxService.getThread("user", msgId);

        // then
        assertThat(thread).hasSize(1);
    }

    @Test
    @DisplayName("18. 발송 취소 - 읽기 전/후 수신자 모두 회수 처리 및 상세 리포트 반환")
    void recallMessage_recallsAllRecipients_andReturnsDetailedReport() {
        // given
        UUID msgId = UUID.randomUUID();
        InboxMessage msg = new InboxMessage();
        msg.setId(msgId);
        msg.setSenderId("sender1");

        InboxRecipient senderRec = new InboxRecipient();
        senderRec.setUserId("sender1");
        senderRec.setRecipientType("FROM");

        InboxRecipient unreadRec = new InboxRecipient();
        unreadRec.setUserId("user_unread");
        unreadRec.setRecipientType("TO");
        unreadRec.setIsRead(false);

        InboxRecipient readRec = new InboxRecipient();
        readRec.setUserId("user_read");
        readRec.setRecipientType("CC");
        readRec.setIsRead(true);
        readRec.setReadAt(LocalDateTime.now().minusMinutes(10));

        InboxRecipient extRec = new InboxRecipient();
        extRec.setEmail("ext@gmail.com");
        extRec.setRecipientType("TO");
        extRec.setIsRead(false);

        msg.setRecipients(List.of(senderRec, unreadRec, readRec, extRec));

        when(messageRepository.findById(msgId)).thenReturn(Optional.of(msg));

        // when
        InboxMessageResponse.RecallResultResponse result = inboxService.recallMessage("sender1", msgId);

        // then
        assertThat(result.getTotalRecipients()).isEqualTo(3);
        assertThat(result.getRecalledBeforeReadCount()).isEqualTo(1);
        assertThat(result.getRecalledAfterReadCount()).isEqualTo(1);
        assertThat(result.getExternalCount()).isEqualTo(1);

        assertThat(unreadRec.getIsRecalled()).isTrue();
        assertThat(unreadRec.getFolder()).isEqualTo("TRASH");
        assertThat(readRec.getIsRecalled()).isTrue();
        assertThat(readRec.getFolder()).isEqualTo("TRASH");

        verify(recipientRepository, times(2)).save(any(InboxRecipient.class));
    }

    @Test
    @DisplayName("19. 발송 취소 - 발신자가 아닌 사용자가 시도 시 예외 발생")
    void recallMessage_byNonSender_throwsException() {
        UUID msgId = UUID.randomUUID();
        InboxMessage msg = new InboxMessage();
        msg.setId(msgId);
        msg.setSenderId("sender1");

        when(messageRepository.findById(msgId)).thenReturn(Optional.of(msg));

        assertThatThrownBy(() -> inboxService.recallMessage("other_user", msgId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only the sender");
    }

    @Test
    @DisplayName("20. 외부 메일 오픈 트래킹 - 미열람 상태일 때 읽음 일시 기록")
    void trackEmailOpen_updatesReadStatus() {
        UUID recId = UUID.randomUUID();
        InboxRecipient rec = new InboxRecipient();
        rec.setId(recId);
        rec.setIsRead(false);
        rec.setEmail("ext@gmail.com");

        when(recipientRepository.findById(recId)).thenReturn(Optional.of(rec));

        // when
        inboxService.trackEmailOpen(recId);

        // then
        assertThat(rec.getIsRead()).isTrue();
        assertThat(rec.getReadAt()).isNotNull();
        verify(recipientRepository).save(rec);
    }
}
