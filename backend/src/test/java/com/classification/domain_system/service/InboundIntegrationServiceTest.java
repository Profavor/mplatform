package com.classification.domain_system.service;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.integration.DataMappingTransformer;
import com.classification.domain_system.integration.InboundIntegrationService;
import com.classification.domain_system.integration.IntegrationLogService;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.RecordFieldSourceRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.SourcePriorityRepository;
import com.classification.domain_system.service.dq.DqRuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InboundIntegrationServiceTest {

    @Mock
    private IntegrationChannelRepository channelRepository;

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private RecordHistoryRepository recordHistoryRepository;

    @Mock
    private MatchingService matchingService;

    @Mock
    private DqRuleEngine dqRuleEngine;

    @Mock
    private DataMappingTransformer mappingTransformer;

    @Mock
    private IntegrationLogService logService;

    @Mock
    private com.classification.domain_system.service.ApprovalService approvalService;

    @Mock
    private SourcePriorityRepository sourcePriorityRepository;

    @Mock
    private RecordFieldSourceRepository recordFieldSourceRepository;

    @InjectMocks
    private InboundIntegrationService inboundIntegrationService;

    private UUID channelId;
    private UUID nodeId;
    private IntegrationChannel inboundChannel;
    private ClassificationNode targetNode;

    @BeforeEach
    void setUp() {
        channelId = UUID.randomUUID();
        nodeId = UUID.randomUUID();

        inboundChannel = new IntegrationChannel();
        inboundChannel.setId(channelId);
        inboundChannel.setName("Test Inbound Webhook");
        inboundChannel.setType("WEB_SERVICE");
        inboundChannel.setDirection("INBOUND");
        inboundChannel.setActive(true);
        inboundChannel.setNodeId(nodeId);
        inboundChannel.setConfigJson("{\"authType\":\"BEARER_TOKEN\",\"secretToken\":\"valid-token-123\"}");
        inboundChannel.setMappingConfigJson("{\"mappings\":[{\"targetField\":\"internalName\",\"sourceExpression\":\"payload['externalName']\"}]}");

        targetNode = new ClassificationNode();
        targetNode.setId(nodeId);
        targetNode.setDomain(new Domain());
        targetNode.getDomain().setId(UUID.randomUUID());

        lenient().when(channelRepository.findById(channelId)).thenReturn(Optional.of(inboundChannel));
        lenient().when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(targetNode));

        MatchingService.DuplicateResult duplicateResult = new MatchingService.DuplicateResult();
        duplicateResult.hasDuplicates = false;
        duplicateResult.duplicateRecordIds = java.util.Collections.emptyList();
        lenient().when(matchingService.checkDuplicates(eq(nodeId), any())).thenReturn(duplicateResult);

        Record savedRecord = new Record();
        savedRecord.setId(UUID.randomUUID());
        savedRecord.setNode(targetNode);
        savedRecord.setStatus("ACTIVE");
        savedRecord.setData("{\"internalName\":\"Sample Data\"}");
        lenient().when(recordRepository.save(any(Record.class))).thenReturn(savedRecord);
    }

    @Test
    @DisplayName("유효한 Bearer 토큰으로 Inbound 데이터 수신 성공 테스트")
    void processInboundData_ValidBearerToken_Success() {
        // given
        String rawPayload = "{\"externalName\":\"Sample Data\"}";
        String transformedPayload = "{\"internalName\":\"Sample Data\"}";
        String authHeader = "Bearer valid-token-123";

        given(channelRepository.findById(channelId)).willReturn(Optional.of(inboundChannel));
        given(mappingTransformer.transformPayload(rawPayload, inboundChannel.getMappingConfigJson()))
                .willReturn(transformedPayload);

        // when
        String result = inboundIntegrationService.processInboundData(channelId, rawPayload, authHeader, null, null);

        // then
        assertThat(result).isEqualTo(transformedPayload);
        verify(logService).logSuccess(eq(channelId), any(), eq("INBOUND_RECEIVE"), eq(rawPayload), eq(transformedPayload));
    }

    @Test
    @DisplayName("잘못된 Bearer 토큰 전달 시 SecurityException 예외 발생 검증")
    void processInboundData_InvalidBearerToken_ThrowsException() {
        // given
        String rawPayload = "{\"externalName\":\"Sample Data\"}";
        String invalidAuthHeader = "Bearer invalid-token-xyz";

        given(channelRepository.findById(channelId)).willReturn(Optional.of(inboundChannel));

        // when & then
        assertThatThrownBy(() -> inboundIntegrationService.processInboundData(channelId, rawPayload, invalidAuthHeader, null, null))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("유효하지 않은 인증 토큰");

        verify(logService).logError(eq(channelId), any(), eq("INBOUND_RECEIVE"), eq(rawPayload), any(), any(), any(), eq(1));
    }

    @Test
    @DisplayName("API Key 헤더 인증 성공 테스트")
    void processInboundData_ValidApiKey_Success() {
        // given
        inboundChannel.setConfigJson("{\"authType\":\"API_KEY\",\"secretToken\":\"my-secret-key\"}");
        String rawPayload = "{\"externalName\":\"Sample Data\"}";
        String transformedPayload = "{\"internalName\":\"Sample Data\"}";

        given(channelRepository.findById(channelId)).willReturn(Optional.of(inboundChannel));
        given(mappingTransformer.transformPayload(rawPayload, inboundChannel.getMappingConfigJson()))
                .willReturn(transformedPayload);

        // when
        String result = inboundIntegrationService.processInboundData(channelId, rawPayload, null, "my-secret-key", null);

        // then
        assertThat(result).isEqualTo(transformedPayload);
    }

    @Test
    @DisplayName("비활성화된 Inbound 채널 요청 시 예외 발생 검증")
    void processInboundData_InactiveChannel_ThrowsException() {
        // given
        inboundChannel.setActive(false);
        given(channelRepository.findById(channelId)).willReturn(Optional.of(inboundChannel));

        // when & then
        assertThatThrownBy(() -> inboundIntegrationService.processInboundData(channelId, "{}", "Bearer valid-token-123", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    @DisplayName("Outbound 방향 채널에 Inbound 요청 시 예외 발생 검증")
    void processInboundData_OutboundChannel_ThrowsException() {
        // given
        inboundChannel.setDirection("OUTBOUND");
        given(channelRepository.findById(channelId)).willReturn(Optional.of(inboundChannel));

        // when & then
        assertThatThrownBy(() -> inboundIntegrationService.processInboundData(channelId, "{}", "Bearer valid-token-123", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not an inbound");
    }
}
