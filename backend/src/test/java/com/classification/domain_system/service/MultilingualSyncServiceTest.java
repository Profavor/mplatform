package com.classification.domain_system.service;

import com.classification.domain_system.dto.MultilingualSyncDto;
import com.classification.domain_system.entity.BusinessTerm;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.repository.BusinessTermRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MultilingualSyncServiceTest {

    @Mock private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private BusinessTermRepository businessTermRepository;

    @InjectMocks
    private MultilingualSyncService multilingualSyncService;

    private UUID domainId;
    private FieldDefinition incompleteField;
    private BusinessTerm term;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();

        incompleteField = new FieldDefinition();
        incompleteField.setId(UUID.randomUUID());
        incompleteField.setKey("biz_no");
        incompleteField.setName(Map.of("ko", "사업자등록번호")); // en is missing

        term = BusinessTerm.builder()
                .termCode("biz_no")
                .termName(Map.of("ko", "사업자등록번호", "en", "Business Registration No"))
                .build();
    }

    @Test
    @DisplayName("scanMissingLocales: 영문(en) 다국어 누락 필드 탐지 검증")
    void testScanMissingLocales() {
        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(List.of(incompleteField));
        when(businessTermRepository.findAll()).thenReturn(List.of(term));

        MultilingualSyncDto.SyncPlanResponse res = multilingualSyncService.scanMissingLocales(domainId);

        assertThat(res).isNotNull();
        assertThat(res.getMissingCount()).isEqualTo(1);
        assertThat(res.getMissingItems().get(0).getMissingLanguages()).contains("en");
        assertThat(res.getMissingItems().get(0).getSuggestedTermName()).isEqualTo("Business Registration No");
    }

    @Test
    @DisplayName("syncFromGlossary: 용어사전 기반 누락 영문 메타데이터 자동 동기화")
    void testSyncFromGlossary() {
        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(List.of(incompleteField));
        when(businessTermRepository.findAll()).thenReturn(List.of(term));

        MultilingualSyncDto.ApplySyncResult res = multilingualSyncService.syncFromGlossary(domainId);

        assertThat(res).isNotNull();
        assertThat(res.getSyncedCount()).isEqualTo(1);
        assertThat(incompleteField.getName()).containsKey("en");
        assertThat(incompleteField.getName().get("en")).isEqualTo("Business Registration No");
        verify(fieldDefinitionRepository, times(1)).save(incompleteField);
    }
}
