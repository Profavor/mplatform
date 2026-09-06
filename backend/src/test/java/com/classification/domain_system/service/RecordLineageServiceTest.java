package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordLineageDto;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.entity.IntegrationLog;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.IntegrationLogRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RecordLineageServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private RecordHistoryRepository recordHistoryRepository;

    @Mock
    private IntegrationLogRepository integrationLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecordService recordService;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private IntegrationChannelRepository channelRepository;

    @InjectMocks
    private RecordLineageService recordLineageService;

    private Record testRecord;
    private UUID testId;
    private ClassificationNode testNode;
    private Domain testDomain;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testDomain = new Domain();
        testDomain.setId(UUID.randomUUID());
        testDomain.setName(java.util.Map.of("ko", "Employee Domain"));

        testNode = new ClassificationNode();
        testNode.setId(UUID.randomUUID());
        testNode.setName(java.util.Map.of("ko", "HR Dept"));
        testNode.setDomain(testDomain);

        testRecord = new Record();
        testRecord.setId(testId);
        testRecord.setNode(testNode);
        testRecord.setCreatedAt(LocalDateTime.now().minusDays(2));
        testRecord.setStatus("ACTIVE");
        testRecord.setSourceSystem("ERP_SYSTEM");
        testRecord.setData("{\"emp_name\":\"홍길동\",\"emp_no\":\"EMP001\"}");
    }

    @Test
    @DisplayName("5단계 파이프라인(SOURCE -> INBOUND -> MASTER -> OUTBOUND -> CONSUMER) 계보와 매핑 규칙, 소비 통계를 정상 추출한다")
    void testGetRecordLineage_FullPipeline() {
        // given
        // 1. Inbound Channel mock
        IntegrationChannel inboundChannel = new IntegrationChannel();
        inboundChannel.setId(UUID.randomUUID());
        inboundChannel.setChannelCode("ERP_SYSTEM");
        inboundChannel.setName("ERP Inbound Adapter");
        inboundChannel.setDirection("INBOUND");
        inboundChannel.setType("REST_API");
        inboundChannel.setMappingConfigJson("{\"name\":\"#jsonPath($, '$.emp_name')\",\"code\":\"#jsonPath($, '$.emp_no')\"}");

        // 2. Outbound Channel mock
        IntegrationChannel outboundChannel = new IntegrationChannel();
        outboundChannel.setId(UUID.randomUUID());
        outboundChannel.setChannelCode("KARTBOM_API");
        outboundChannel.setName("카트봄 동기화");
        outboundChannel.setDirection("OUTBOUND");
        outboundChannel.setType("KAFKA");

        given(recordRepository.findById(testId)).willReturn(Optional.of(testRecord));
        given(channelRepository.findAll()).willReturn(List.of(inboundChannel, outboundChannel));

        // 3. Record History mock
        RecordHistory history = new RecordHistory();
        history.setId(UUID.randomUUID());
        history.setRecord(testRecord);
        history.setVersion(1);
        history.setChangeType("CREATE");
        history.setChangedBy("admin");
        history.setChangedAt(LocalDateTime.now().minusDays(1));
        history.setNewData(testRecord.getData());
        given(recordHistoryRepository.findByRecordIdOrderByVersionAsc(testId)).willReturn(List.of(history));

        // 4. Integration Logs mock (Outbound success)
        IntegrationLog log = new IntegrationLog();
        log.setId(UUID.randomUUID());
        log.setRecordId(testId);
        log.setChannelId(outboundChannel.getId());
        log.setChannel(outboundChannel);
        log.setStatus("SUCCESS");
        log.setCreatedAt(LocalDateTime.now().minusHours(2));
        given(integrationLogRepository.findByRecordIdOrderByCreatedAtDesc(testId)).willReturn(List.of(log));

        // when
        RecordLineageDto.RecordLineageResponse response = recordLineageService.getRecordLineage(testId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecordId()).isEqualTo(testId);
        assertThat(response.getNodes()).isNotEmpty();
        assertThat(response.getEdges()).isNotEmpty();

        // 5단계 스테이지 확인
        assertThat(response.getStages()).hasSize(5);
        assertThat(response.getStages().get(0).getStage()).isEqualTo("SOURCE");
        assertThat(response.getStages().get(1).getStage()).isEqualTo("INBOUND_PIPELINE");
        assertThat(response.getStages().get(2).getStage()).isEqualTo("MASTER_RECORD");
        assertThat(response.getStages().get(3).getStage()).isEqualTo("OUTBOUND_PIPELINE");
        assertThat(response.getStages().get(4).getStage()).isEqualTo("DOWNSTREAM_CONSUMER");

        // 매핑 규칙 확인 (INBOUND_PIPELINE 노드)
        RecordLineageDto.LineageNode inboundNode = response.getNodes().stream()
                .filter(n -> "INBOUND_PIPELINE".equals(n.getStage()))
                .findFirst().orElse(null);
        assertThat(inboundNode).isNotNull();
        assertThat(inboundNode.getMappingRules()).isNotEmpty();
        assertThat(inboundNode.getMappingRules().get(0).getTargetField()).isIn("name", "code");

        // 채널 소비 통계 확인
        assertThat(response.getChannelConsumption()).isNotEmpty();
        var consumption = response.getChannelConsumption().get(0);
        assertThat(consumption.get("channelName")).isEqualTo("카트봄 동기화");
        assertThat(consumption.get("successCount")).isEqualTo(1L);
        assertThat(consumption.get("failCount")).isEqualTo(0L);
    }

    @Test
    @DisplayName("연동 실패(FAIL) 로그가 존재할 경우 파이프라인 노드가 ERROR 상태로 자율 판정되고 anomalyReason이 기록된다")
    void testGetRecordLineage_AnomalyDetection_Error() {
        // given
        IntegrationChannel outboundChannel = new IntegrationChannel();
        outboundChannel.setId(UUID.randomUUID());
        outboundChannel.setChannelCode("KRX_OUT");
        outboundChannel.setName("KRX 공시 연계");
        outboundChannel.setDirection("OUTBOUND");
        outboundChannel.setType("REST_API");

        given(recordRepository.findById(testId)).willReturn(Optional.of(testRecord));
        given(channelRepository.findAll()).willReturn(List.of(outboundChannel));
        given(recordHistoryRepository.findByRecordIdOrderByVersionAsc(testId)).willReturn(Collections.emptyList());

        // Integration Log with FAIL status
        IntegrationLog failLog = new IntegrationLog();
        failLog.setId(UUID.randomUUID());
        failLog.setRecordId(testId);
        failLog.setChannelId(outboundChannel.getId());
        failLog.setChannel(outboundChannel);
        failLog.setStatus("FAIL");
        failLog.setErrorMessage("Connection timed out to KRX Endpoint");
        failLog.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        given(integrationLogRepository.findByRecordIdOrderByCreatedAtDesc(testId)).willReturn(List.of(failLog));

        // when
        RecordLineageDto.RecordLineageResponse response = recordLineageService.getRecordLineage(testId);

        // then
        RecordLineageDto.LineageNode outboundNode = response.getNodes().stream()
                .filter(n -> "OUTBOUND_PIPELINE".equals(n.getStage()))
                .findFirst().orElse(null);
        assertThat(outboundNode).isNotNull();
        assertThat(outboundNode.getHealthStatus()).isEqualTo("ERROR");
        assertThat(outboundNode.getAnomalyReason()).contains("Connection timed out");

        // Outbound Pipeline Stage 또한 ERROR로 집계되어야 함
        RecordLineageDto.PipelineStageSummary outboundStage = response.getStages().stream()
                .filter(s -> "OUTBOUND_PIPELINE".equals(s.getStage()))
                .findFirst().orElse(null);
        assertThat(outboundStage).isNotNull();
        assertThat(outboundStage.getStatus()).isEqualTo("ERROR");
    }
}
