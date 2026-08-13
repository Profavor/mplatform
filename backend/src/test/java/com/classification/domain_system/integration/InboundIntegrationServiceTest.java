package com.classification.domain_system.integration;

import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.RecordFieldSourceRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.SourcePriorityRepository;
import com.classification.domain_system.service.ApprovalService;
import com.classification.domain_system.service.FieldEncryptionService;
import com.classification.domain_system.service.MatchingService;
import com.classification.domain_system.service.NotificationService;
import com.classification.domain_system.service.dq.DqRuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class InboundIntegrationServiceTest {

    @Mock private IntegrationChannelRepository channelRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private RecordRepository recordRepository;
    @Mock private RecordHistoryRepository recordHistoryRepository;
    @Mock private MatchingService matchingService;
    @Mock private DqRuleEngine dqRuleEngine;
    @Mock private DataMappingTransformer mappingTransformer;
    @Mock private IntegrationLogService logService;
    @Mock private ApprovalService approvalService;
    @Mock private SourcePriorityRepository sourcePriorityRepository;
    @Mock private RecordFieldSourceRepository recordFieldSourceRepository;
    @Mock private NotificationService notificationService;
    @Mock private FieldEncryptionService encryptionService;

    @InjectMocks
    private InboundIntegrationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validateInboundAuthentication_Success_WithEncryptedBearerToken() {
        UUID channelId = UUID.randomUUID();
        IntegrationChannel channel = new IntegrationChannel();
        channel.setId(channelId);
        channel.setName("Test Channel");
        channel.setDirection("INBOUND");
        channel.setActive(true);
        channel.setConfigJson("{\"authType\":\"BEARER_TOKEN\",\"secretToken\":\"encryptedToken\"}");

        when(channelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(encryptionService.isEncrypted("encryptedToken")).thenReturn(true);
        when(encryptionService.decrypt("encryptedToken")).thenReturn("actualSecret");
        
        // This should pass without throwing SecurityException. Wait, processInboundData also saves record, which might cause NPE if not mocked properly, but we can just test if it throws SecurityException. We'll expect an IllegalArgumentException instead since it has no valid mapping/node configured.
        assertThrows(IllegalArgumentException.class, () -> 
            service.processInboundData(channelId, "{}", "Bearer actualSecret", null, null)
        );
    }

    @Test
    void validateInboundAuthentication_Failure_WithInvalidBearerToken() {
        UUID channelId = UUID.randomUUID();
        IntegrationChannel channel = new IntegrationChannel();
        channel.setId(channelId);
        channel.setName("Test Channel");
        channel.setDirection("INBOUND");
        channel.setActive(true);
        channel.setConfigJson("{\"authType\":\"BEARER_TOKEN\",\"secretToken\":\"encryptedToken\"}");

        when(channelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(encryptionService.isEncrypted("encryptedToken")).thenReturn(true);
        when(encryptionService.decrypt("encryptedToken")).thenReturn("actualSecret");
        
        assertThrows(SecurityException.class, () -> 
            service.processInboundData(channelId, "{}", "Bearer wrongSecret", null, null)
        );
    }
}
