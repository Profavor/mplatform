package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataProfilingReportDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataProfilingServiceTest {

    @Mock private DomainRepository domainRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private RecordRepository recordRepository;

    @InjectMocks
    private DataProfilingService profilingService;

    private UUID domainId;
    private Domain domain;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        domain = new Domain();
        domain.setId(domainId);
        domain.setName(Map.of("ko", "고객 도메인"));
    }

    @Test
    @DisplayName("getProfilingReport: 필드별 결측률 및 이상치(IQR) 정상 탐지")
    void testGetProfilingReport() {
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(nodeRepository.findByDomain_Id(domainId)).thenReturn(Collections.emptyList());

        FieldDefinition ageField = new FieldDefinition();
        ageField.setKey("age");
        ageField.setName(Map.of("ko", "나이"));
        ageField.setType("NUMBER");
        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(List.of(ageField));

        // 5개 레코드: 20, 25, 28, 30, 250 (250은 명백한 IQR 이상치)
        List<Record> records = new ArrayList<>();
        int[] ages = {20, 25, 28, 30, 250};
        for (int a : ages) {
            Record r = new Record();
            r.setId(UUID.randomUUID());
            r.setData("{\"age\":" + a + "}");
            records.add(r);
        }
        when(recordRepository.findAllByDomainId(domainId)).thenReturn(records);

        DataProfilingReportDto report = profilingService.getProfilingReport(domainId);

        assertThat(report).isNotNull();
        assertThat(report.getTotalRecords()).isEqualTo(5);
        assertThat(report.getFieldProfiles()).hasSize(1);

        DataProfilingReportDto.FieldProfile fp = report.getFieldProfiles().get(0);
        assertThat(fp.getFieldKey()).isEqualTo("age");
        assertThat(fp.getNullCount()).isEqualTo(0);
        assertThat(fp.getNullRate()).isEqualTo(0.0);
        assertThat(fp.getOutlierCount()).isGreaterThanOrEqualTo(1);

        assertThat(report.getOutliers()).isNotEmpty();
        assertThat(report.getOutliers().get(0).getFieldKey()).isEqualTo("age");
        assertThat(report.getOutliers().get(0).getValue()).isEqualTo(250.0);
    }

    @Test
    @DisplayName("getProfilingReport: 존재하지 않는 도메인이면 ResourceNotFoundException 발생")
    void testGetReportNotFound() {
        when(domainRepository.findById(domainId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profilingService.getProfilingReport(domainId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Domain not found");
    }
}
