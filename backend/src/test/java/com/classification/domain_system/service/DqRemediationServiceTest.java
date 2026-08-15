package com.classification.domain_system.service;

import com.classification.domain_system.dto.DqRemediationDto;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DqRemediationServiceTest {

    @Mock private RecordRepository recordRepository;
    @Mock private RecordHistoryRepository recordHistoryRepository;
    @Mock private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private ClassificationNodeRepository nodeRepository;

    @InjectMocks
    private DqRemediationService remediationService;

    private UUID domainId;
    private UUID recordId;
    private Record record;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        recordId = UUID.randomUUID();
        record = new Record();
        record.setId(recordId);
        record.setVersion(1);
        record.setData("{\"name\": \"  홍길동  \", \"phone\": \"01012345678\", \"bizNo\": \"1234567890\", \"email\": \"USER@TEST.COM\"}");
    }

    @Test
    @DisplayName("scanAndPropose: 공백, 전화번호, 사업자번호, 이메일 보정 제안 정상 탐지")
    void testScanAndPropose() {
        when(nodeRepository.findByDomain_Id(domainId)).thenReturn(Collections.emptyList());
        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(Collections.emptyList());
        when(recordRepository.findAllByDomainId(domainId)).thenReturn(List.of(record));

        List<DqRemediationDto.RemediationProposal> proposals = remediationService.scanAndPropose(domainId);

        assertThat(proposals).hasSize(4);

        assertThat(proposals.stream().anyMatch(p -> p.getFieldKey().equals("name") && p.getProposedValue().equals("홍길동"))).isTrue();
        assertThat(proposals.stream().anyMatch(p -> p.getFieldKey().equals("phone") && p.getProposedValue().equals("010-1234-5678"))).isTrue();
        assertThat(proposals.stream().anyMatch(p -> p.getFieldKey().equals("bizNo") && p.getProposedValue().equals("123-45-67890"))).isTrue();
        assertThat(proposals.stream().anyMatch(p -> p.getFieldKey().equals("email") && p.getProposedValue().equals("user@test.com"))).isTrue();
    }

    @Test
    @DisplayName("applyRemediations: 제안된 보정값 레코드 반영 및 변경 이력 기록")
    void testApplyRemediations() {
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        DqRemediationDto.RemediationApplyRequest request = DqRemediationDto.RemediationApplyRequest.builder()
                .items(List.of(
                        new DqRemediationDto.ProposalItem(recordId, "phone", "010-1234-5678")
                ))
                .build();

        DqRemediationDto.RemediationApplyResult result = remediationService.applyRemediations(domainId, request);

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailedCount()).isEqualTo(0);
        assertThat(record.getData()).contains("010-1234-5678");
        assertThat(record.getVersion()).isEqualTo(2);

        verify(recordRepository, times(1)).save(record);
        verify(recordHistoryRepository, times(1)).save(any(RecordHistory.class));
    }
}
