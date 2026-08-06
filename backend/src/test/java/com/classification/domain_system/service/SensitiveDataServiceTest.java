package com.classification.domain_system.service;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.SensitiveDataAccessLog;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.SensitiveDataAccessLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensitiveDataServiceTest {

    @Mock private FieldEncryptionService encryptionService;
    @Mock private FieldDefinitionService fieldDefinitionService;
    @Mock private RecordRepository recordRepository;
    @Mock private com.classification.domain_system.repository.RecordHistoryRepository recordHistoryRepository;
    @Mock private ApprovalRequestRepository approvalRepository;
    @Mock private SensitiveDataAccessLogRepository accessLogRepository;
    @Mock private com.classification.domain_system.repository.FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private com.classification.domain_system.repository.UserRepository userRepository;
    @Mock private AuthContext authContext;

    private SensitiveDataService sensitiveDataService;

    @BeforeEach
    void setUp() {
        sensitiveDataService = new SensitiveDataService(
                encryptionService,
                fieldDefinitionService,
                recordRepository,
                recordHistoryRepository,
                approvalRepository,
                accessLogRepository,
                fieldDefinitionRepository,
                userRepository,
                authContext
        );
    }

    @Test
    @DisplayName("decryptApprovalFields - 결재 요청의 암호화 필드를 복호화하고 감사 로그를 생성한다 (IPv6 ::1 ➔ 127.0.0.1 정규화)")
    void decryptApprovalFields_Success() {
        UUID approvalId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();

        ClassificationNode node = new ClassificationNode();
        node.setId(nodeId);

        ApprovalRequest request = new ApprovalRequest();
        request.setId(approvalId);
        request.setClassificationNode(node);
        request.setChanges("{\"after\":{\"rrn\":\"ENC_1234\"}}");

        FieldDefinition field = new FieldDefinition();
        field.setKey("rrn");
        field.setIsEncrypted(true);

        when(approvalRepository.findById(approvalId)).thenReturn(Optional.of(request));
        when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));
        when(encryptionService.decrypt("ENC_1234")).thenReturn("900101-1234567");
        when(authContext.getUserId()).thenReturn("user-uuid-1");

        Map<String, String> result = sensitiveDataService.decryptApprovalFields(approvalId, List.of("rrn"), "업무 목적", "::1");

        assertThat(result).containsEntry("rrn", "900101-1234567");

        ArgumentCaptor<SensitiveDataAccessLog> logCaptor = ArgumentCaptor.forClass(SensitiveDataAccessLog.class);
        verify(accessLogRepository).save(logCaptor.capture());
        SensitiveDataAccessLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getTargetId()).isEqualTo(approvalId);
        assertThat(savedLog.getTargetType()).isEqualTo("APPROVAL_REQUEST");
        assertThat(savedLog.getFieldKeys()).isEqualTo("rrn");
        assertThat(savedLog.getAccessReason()).isEqualTo("업무 목적");
        assertThat(savedLog.getIpAddress()).isEqualTo("127.0.0.1");
    }

    @Test
    @DisplayName("convertToDto - ::1 IP 정규화, username(superadmin) 표출, 식별 코드(REQ-xxx), 필드 라벨 변환 검증")
    void convertToDto_Success() {
        UUID targetId = UUID.fromString("e1030a86-3ce0-42d6-9f08-42a000000000");
        String userUuid = "6306a5b6-258a-43fb-a000-000000000000";
        SensitiveDataAccessLog logEntity = new SensitiveDataAccessLog();
        logEntity.setId(UUID.randomUUID());
        logEntity.setUserId(userUuid);
        logEntity.setTargetType("APPROVAL_REQUEST");
        logEntity.setTargetId(targetId);
        logEntity.setFieldKeys("rrn");
        logEntity.setIpAddress("::1");

        com.classification.domain_system.entity.User user = new com.classification.domain_system.entity.User();
        user.setId(userUuid);
        user.setUsername("superadmin");

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));

        FieldDefinition fd = new FieldDefinition();
        fd.setKey("rrn");
        fd.setName(Map.of("ko", "주민등록번호"));

        when(fieldDefinitionRepository.findByKey("rrn")).thenReturn(List.of(fd));

        var dto = sensitiveDataService.convertToDto(logEntity);

        assertThat(dto.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(dto.getFormattedTargetId()).isEqualTo("REQ-e1030a86");
        assertThat(dto.getUserDisplayName()).isEqualTo("superadmin");
        assertThat(dto.getFormattedFieldLabels()).isEqualTo("주민등록번호");
    }

    @Test
    @DisplayName("convertToDto - RECORD_HISTORY 도메인 및 분류 추출 검증")
    void convertToDto_RecordHistory() {
        UUID targetId = UUID.randomUUID();
        SensitiveDataAccessLog logEntity = new SensitiveDataAccessLog();
        logEntity.setId(UUID.randomUUID());
        logEntity.setTargetType("RECORD_HISTORY");
        logEntity.setTargetId(targetId);

        com.classification.domain_system.entity.RecordHistory history = new com.classification.domain_system.entity.RecordHistory();
        history.setId(targetId);
        
        com.classification.domain_system.entity.Record record = new com.classification.domain_system.entity.Record();
        ClassificationNode node = new ClassificationNode();
        node.setName(Map.of("ko", "테스트분류"));
        
        com.classification.domain_system.entity.Domain domain = new com.classification.domain_system.entity.Domain();
        domain.setName(Map.of("ko", "테스트도메인"));
        node.setDomain(domain);
        
        record.setNode(node);
        history.setRecord(record);
        history.setPreviousData("{\"testKey\":\"oldValue\"}");
        history.setNewData("{\"testKey\":\"newValue\"}");

        when(recordHistoryRepository.findById(targetId)).thenReturn(Optional.of(history));

        var dto = sensitiveDataService.convertToDto(logEntity);

        assertThat(dto.getClassificationName()).isEqualTo("테스트분류");
        assertThat(dto.getDomainName()).isEqualTo("테스트도메인");
    }

    @Test
    @DisplayName("decryptApprovalFields - 존재하지 않는 결재 요청 시 예외 발생")
    void decryptApprovalFields_NotFound() {
        UUID approvalId = UUID.randomUUID();
        when(approvalRepository.findById(approvalId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sensitiveDataService.decryptApprovalFields(approvalId, null, "사유", "127.0.0.1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
    @Test
    @DisplayName("getStatistics - 최근 7일 통계, 타입별 비율, Top 5 유저 집계 검증")
    void getStatistics_Success() {
        SensitiveDataAccessLog log1 = new SensitiveDataAccessLog();
        log1.setAccessedAt(java.time.LocalDateTime.now());
        log1.setTargetType("APPROVAL_REQUEST");
        log1.setUserId("admin");
        log1.setUsername("admin");

        SensitiveDataAccessLog log2 = new SensitiveDataAccessLog();
        log2.setAccessedAt(java.time.LocalDateTime.now().minusDays(1));
        log2.setTargetType("APPROVAL_REQUEST");
        log2.setUserId("admin");
        log2.setUsername("admin");

        SensitiveDataAccessLog log3 = new SensitiveDataAccessLog();
        log3.setAccessedAt(java.time.LocalDateTime.now().minusDays(2));
        log3.setTargetType("RECORD");
        log3.setUserId("user1");
        log3.setUsername("user1");

        when(accessLogRepository.findByAccessedAtAfter(any(java.time.LocalDateTime.class)))
                .thenReturn(List.of(log1, log2, log3));

        com.classification.domain_system.dto.SensitiveDataStatsDto stats = sensitiveDataService.getStatistics();

        assertThat(stats.getTargetTypeRatios()).containsEntry("APPROVAL_REQUEST", 2L);
        assertThat(stats.getTargetTypeRatios()).containsEntry("RECORD", 1L);

        assertThat(stats.getTopUsers()).containsEntry("admin", 2L);
        assertThat(stats.getTopUsers()).containsEntry("user1", 1L);
        
        assertThat(stats.getDailyTrends()).isNotEmpty();
    }
}
