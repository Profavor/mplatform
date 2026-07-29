package com.classification.domain_system.integration;

import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationNotificationTest {

    @Mock
    private IntegrationChannelRepository channelRepository;

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private DataMappingTransformer mappingTransformer;

    @Mock
    private IntegrationLogService logService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InboundIntegrationService inboundIntegrationService;

    @Test
    @DisplayName("Inbound 연계 데이터 처리 중 오류 발생 시 알림 생성 서비스가 호출된다")
    void processInboundData_Error_TriggersNotification() {
        UUID channelId = UUID.randomUUID();
        IntegrationChannel channel = new IntegrationChannel();
        channel.setId(channelId);
        channel.setActive(true);
        channel.setDirection("INBOUND");
        channel.setName("Test Inbound Channel");

        when(channelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(mappingTransformer.transformPayload(any(), any()))
                .thenThrow(new RuntimeException("Inbound Transformation Failed!"));

        assertThatThrownBy(() -> inboundIntegrationService.processInboundData(
                channelId, "{\"raw\":\"data\"}", null, null, null))
                .isInstanceOf(RuntimeException.class);

        verify(notificationService, times(1)).createNotification(
                isNull(),
                contains("Test Inbound Channel"),
                contains("Inbound Transformation Failed!"),
                eq("SYSTEM"),
                eq("/admin/integration/channels")
        );
    }
}
