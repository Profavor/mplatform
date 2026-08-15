package com.classification.domain_system.service;

import com.classification.domain_system.dto.SchemaCompatibilityDto;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SchemaCompatibilityServiceTest {

    @Mock private FieldDefinitionRepository fieldDefinitionRepository;

    @InjectMocks
    private SchemaCompatibilityService compatibilityService;

    private UUID domainId;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
    }

    @Test
    @DisplayName("analyzeCompatibility: 필수값 전환 및 필드 삭제 시 브레이킹 체인지 감지")
    void testAnalyzeCompatibilityBreaking() {
        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(Collections.emptyList());

        SchemaCompatibilityDto.SchemaCompatibilityReport res = compatibilityService.analyzeCompatibility(domainId, "필드 삭제 및 REQUIRED 전환");

        assertThat(res).isNotNull();
        assertThat(res.getOverallCompatibility()).isEqualTo("BREAKING_CHANGE");
        assertThat(res.getRiskScore()).isGreaterThan(50);
        assertThat(res.getRisks()).isNotEmpty();
    }

    @Test
    @DisplayName("analyzeCompatibility: 하위 호환 가능한 스키마 변경")
    void testAnalyzeCompatibilityCompatible() {
        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(Collections.emptyList());

        SchemaCompatibilityDto.SchemaCompatibilityReport res = compatibilityService.analyzeCompatibility(domainId, "신규 OPTIONAL 필드 추가");

        assertThat(res).isNotNull();
        assertThat(res.getOverallCompatibility()).isEqualTo("COMPATIBLE");
        assertThat(res.getRiskScore()).isEqualTo(0);
    }
}
