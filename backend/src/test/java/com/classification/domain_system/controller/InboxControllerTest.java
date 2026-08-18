package com.classification.domain_system.controller;

import com.classification.domain_system.config.SecurityConfig;
import com.classification.domain_system.dto.InboxMessageRequest;
import com.classification.domain_system.dto.InboxMessageResponse;
import com.classification.domain_system.security.SecurityUtils;
import com.classification.domain_system.service.InboxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InboxController.class)
@org.springframework.context.annotation.Import({SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class InboxControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean InboxService inboxService;
    @MockitoBean SecurityUtils securityUtils;
    @MockitoBean com.classification.domain_system.security.JwtUtil jwtUtil;
    @MockitoBean com.classification.domain_system.service.PermissionService permissionService;
    @MockitoBean com.classification.domain_system.context.AuthContext authContext;

    @Test
    @DisplayName("1. 메시지 목록 조회 - 정상 반환")
    void getMessages_returnsPagedResults() throws Exception {
        when(securityUtils.getCurrentUserIdOrThrow()).thenReturn("user1");
        when(inboxService.getMessages(anyString(), anyString(), any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(new InboxMessageResponse())));

        mockMvc.perform(get("/api/inbox/messages")
                        .param("folder", "INBOX")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("2. 메시지 전송 - 200 반환")
    void sendMessage_returns200() throws Exception {
        when(securityUtils.getCurrentUserIdOrThrow()).thenReturn("user1");
        
        InboxMessageRequest request = new InboxMessageRequest();
        request.setSubject("Test");
        
        InboxMessageResponse response = new InboxMessageResponse();
        response.setId(UUID.randomUUID());
        
        when(inboxService.sendMessage(any(InboxMessageRequest.class), eq("user1")))
                .thenReturn(response);

        mockMvc.perform(post("/api/inbox/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("3. 메시지 단건 조회 - 정상 반환")
    void getMessage_returnsDetail() throws Exception {
        when(securityUtils.getCurrentUserIdOrThrow()).thenReturn("user1");
        
        UUID msgId = UUID.randomUUID();
        InboxMessageResponse response = new InboxMessageResponse();
        response.setId(msgId);
        
        when(inboxService.getMessage("user1", msgId)).thenReturn(response);

        mockMvc.perform(get("/api/inbox/messages/" + msgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(msgId.toString()));
    }

    @Test
    @DisplayName("4. 메시지 삭제 - 휴지통 이동")
    void deleteMessage_returns200() throws Exception {
        when(securityUtils.getCurrentUserIdOrThrow()).thenReturn("user1");
        UUID msgId = UUID.randomUUID();

        mockMvc.perform(delete("/api/inbox/messages/" + msgId)
                        .param("permanent", "false"))
                .andExpect(status().isOk());

        verify(inboxService).moveToTrash("user1", msgId);
    }

    @Test
    @DisplayName("5. 일괄 작업 - 읽음 처리")
    void bulkAction_markRead() throws Exception {
        when(securityUtils.getCurrentUserIdOrThrow()).thenReturn("user1");
        
        UUID msgId1 = UUID.randomUUID();
        UUID msgId2 = UUID.randomUUID();
        
        Map<String, Object> req = Map.of(
                "action", "MARK_READ",
                "messageIds", List.of(msgId1.toString(), msgId2.toString())
        );

        mockMvc.perform(post("/api/inbox/messages/bulk-action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(inboxService).bulkMarkAsRead(eq("user1"), any());
    }

    @Test
    @DisplayName("6. 발송 취소 - 200 반환 및 결과 응답")
    void recallMessage_returns200() throws Exception {
        when(securityUtils.getCurrentUserIdOrThrow()).thenReturn("user1");
        UUID msgId = UUID.randomUUID();

        InboxMessageResponse.RecallResultResponse result = InboxMessageResponse.RecallResultResponse.builder()
                .messageId(msgId)
                .totalRecipients(2)
                .recalledBeforeReadCount(1)
                .recalledAfterReadCount(1)
                .externalCount(0)
                .details(List.of())
                .build();

        when(inboxService.recallMessage("user1", msgId)).thenReturn(result);

        mockMvc.perform(post("/api/inbox/messages/" + msgId + "/recall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(msgId.toString()))
                .andExpect(jsonPath("$.totalRecipients").value(2))
                .andExpect(jsonPath("$.recalledBeforeReadCount").value(1))
                .andExpect(jsonPath("$.recalledAfterReadCount").value(1));
    }

    @Test
    @DisplayName("7. 외부 메일 오픈 트래킹 - 1x1 투명 GIF 반환")
    void trackOpen_returnsImageGif() throws Exception {
        UUID recId = UUID.randomUUID();

        mockMvc.perform(get("/api/inbox/track/open/" + recId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_GIF))
                .andExpect(content().bytes(new byte[]{
                        0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 1, 0, 1, 0,
                        (byte) 0x80, 0, 0, 0, 0, 0, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                        0x21, (byte) 0xf9, 4, 1, 0, 0, 0, 0,
                        0x2c, 0, 0, 0, 0, 1, 0, 1, 0, 0, 2, 2, 0x44, 1, 0, 0x3b
                }));

        verify(inboxService).trackEmailOpen(recId);
    }
}
