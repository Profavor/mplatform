package com.classification.domain_system.controller;

import com.classification.domain_system.dto.NotificationDto;
import com.classification.domain_system.service.NotificationService;
import com.classification.domain_system.service.SseNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private SseNotificationService sseNotificationService;

    @MockitoBean
    private com.classification.domain_system.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.classification.domain_system.service.PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @Test
    @DisplayName("GET /api/notifications/subscribe - SSE 구독 성공")
    void testSubscribe() throws Exception {
        given(sseNotificationService.subscribe(any())).willReturn(new SseEmitter());

        mockMvc.perform(get("/api/notifications/subscribe"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/notifications - 내 알림 목록 조회")
    @WithMockUser(username = "testuser")
    void testGetMyNotifications() throws Exception {
        given(notificationService.getMyNotifications("testuser")).willReturn(List.of());

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/notifications/unread-count - 읽지 않은 알림 수 조회")
    @WithMockUser(username = "testuser")
    void testGetUnreadCount() throws Exception {
        given(notificationService.getUnreadCount("testuser")).willReturn(5L);

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read - 단일 알림 읽음 처리 (UUID)")
    @WithMockUser(username = "testuser")
    void testMarkAsReadPut() throws Exception {
        UUID id = UUID.randomUUID();
        given(notificationService.markAsRead(id, "testuser")).willReturn(true);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/notifications/" + id + "/read"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read - 비UUID ID 안전 처리")
    @WithMockUser(username = "testuser")
    void testMarkAsReadNonUuid() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/notifications/1787050754192.7412/read"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/notifications/read-all - 전체 알림 읽음 처리")
    @WithMockUser(username = "testuser")
    void testMarkAllAsReadPut() throws Exception {
        given(notificationService.markAllAsRead("testuser")).willReturn(3);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/notifications/read-all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/notifications/{id} - 단일 알림 삭제")
    @WithMockUser(username = "testuser")
    void testDeleteNotification() throws Exception {
        UUID id = UUID.randomUUID();
        given(notificationService.deleteNotification(id, "testuser")).willReturn(true);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/notifications/" + id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/notifications/clear-all - 전체 알림 삭제")
    @WithMockUser(username = "testuser")
    void testClearAllNotifications() throws Exception {
        given(notificationService.clearAllNotifications("testuser")).willReturn(5);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/notifications/clear-all"))
                .andExpect(status().isOk());
    }
}
