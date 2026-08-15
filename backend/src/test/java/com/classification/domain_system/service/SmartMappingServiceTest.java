package com.classification.domain_system.service;

import com.classification.domain_system.dto.SmartMappingRecommendationDto;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SmartMappingServiceTest {

    @Mock private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private ClassificationNodeRepository nodeRepository;

    @InjectMocks
    private SmartMappingService smartMappingService;

    private UUID domainId;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
    }

    @Test
    @DisplayName("recommendMappings: 정확한 필드 키 및 카멜/스네이크 정규화 매핑 추천")
    void testRecommendMappingsExactAndNormalized() {
        when(nodeRepository.findByDomain_Id(domainId)).thenReturn(Collections.emptyList());

        FieldDefinition nameField = new FieldDefinition();
        nameField.setKey("userName");
        nameField.setName(Map.of("ko", "사용자명"));

        FieldDefinition phoneField = new FieldDefinition();
        phoneField.setKey("phoneNumber");
        phoneField.setName(Map.of("ko", "전화번호"));

        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(List.of(nameField, phoneField));

        String sampleJson = "{\"userName\": \"홍길동\", \"phone_number\": \"010-1234-5678\"}";

        List<SmartMappingRecommendationDto> recommendations = smartMappingService.recommendMappings(domainId, sampleJson);

        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations).hasSize(2);

        SmartMappingRecommendationDto r1 = recommendations.stream().filter(r -> r.getSourceField().equals("userName")).findFirst().orElse(null);
        assertThat(r1).isNotNull();
        assertThat(r1.getTargetFieldKey()).isEqualTo("userName");
        assertThat(r1.getConfidenceScore()).isEqualTo(100);

        SmartMappingRecommendationDto r2 = recommendations.stream().filter(r -> r.getSourceField().equals("phone_number")).findFirst().orElse(null);
        assertThat(r2).isNotNull();
        assertThat(r2.getTargetFieldKey()).isEqualTo("phoneNumber");
        assertThat(r2.getConfidenceScore()).isGreaterThanOrEqualTo(90);
    }

    @Test
    @DisplayName("recommendMappings: 빈 페이로드 입력 시 빈 결과 반환")
    void testEmptyPayload() {
        List<SmartMappingRecommendationDto> result = smartMappingService.recommendMappings(domainId, "");
        assertThat(result).isEmpty();
    }
}
