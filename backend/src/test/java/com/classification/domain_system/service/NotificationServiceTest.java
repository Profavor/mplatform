package com.classification.domain_system.service;

import com.classification.domain_system.dto.NotificationDto;
import com.classification.domain_system.entity.Notification;
import com.classification.domain_system.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private String userId;
    private UUID notificationId;
    private Notification notification;

    @BeforeEach
    void setUp() {
        userId = "admin@example.com";
        notificationId = UUID.randomUUID();
        notification = Notification.builder()
                .id(notificationId)
                .userId(userId)
                .title("결재 승인 요청")
                .message("신규 레코드 등록 요청이 도착했습니다.")
                .type("APPROVAL")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getMyNotifications & getUnreadCount: 사용자 알림 조회 및 미확인 카운트 확인")
    void testGetNotificationsAndUnreadCount() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(notification));
        when(notificationRepository.countByUserIdAndIsReadFalse(userId)).thenReturn(1L);

        List<NotificationDto.NotificationResponse> list = notificationService.getMyNotifications(userId);
        long unreadCount = notificationService.getUnreadCount(userId);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getTitle()).isEqualTo("결재 승인 요청");
        assertThat(unreadCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("markAsRead & markAllAsRead: 단건 및 전체 읽음 처리 검증")
    void testMarkAsRead() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.markAllAsReadByUserId(userId)).thenReturn(1);

        boolean marked = notificationService.markAsRead(notificationId, userId);
        int allMarked = notificationService.markAllAsRead(userId);

        assertThat(marked).isTrue();
        assertThat(notification.isRead()).isTrue();
        assertThat(allMarked).isEqualTo(1);
        verify(notificationRepository, times(1)).save(notification);
    }
}
