package com.classification.domain_system.service;

import com.classification.domain_system.entity.Notification;
import com.classification.domain_system.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SseNotificationService sseNotificationService;

    @InjectMocks
    private NotificationService notificationService;

    private String userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("testCreateNotification: Creates notification for user")
    void testCreateNotification() {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(userId);
        notification.setTitle("Test Title");
        notification.setMessage("Test Message");
        notification.setType("INFO");
        notification.setLinkUrl("/test");
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.createNotification(userId, "Test Title", "Test Message", "INFO", "/test");

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTitle()).isEqualTo("Test Title");
        assertThat(result.getMessage()).isEqualTo("Test Message");
        verify(notificationRepository).save(any(Notification.class));
        verify(sseNotificationService).sendNotification(eq(userId), any(Notification.class));
    }

    @Test
    @DisplayName("testMarkAsRead: Marks single notification as read")
    void testMarkAsRead() {
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setUserId(userId);
        notification.setIsRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.markAsRead(notificationId, userId);

        assertThat(result.getIsRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("testMarkAllAsRead: Marks all notifications for user as read")
    void testMarkAllAsRead() {
        when(notificationRepository.markAllAsRead(userId)).thenReturn(5);

        int updatedCount = notificationService.markAllAsRead(userId);

        assertThat(updatedCount).isEqualTo(5);
        verify(notificationRepository).markAllAsRead(userId);
    }

    @Test
    @DisplayName("testGetUserNotifications: Paginated user notifications ordered by createdAt desc")
    void testGetUserNotifications() {
        Pageable pageable = PageRequest.of(0, 10);
        Notification notification = new Notification();
        notification.setUserId(userId);
        Page<Notification> page = new PageImpl<>(List.of(notification));

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(page);

        Page<Notification> result = notificationService.getUserNotifications(userId, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
