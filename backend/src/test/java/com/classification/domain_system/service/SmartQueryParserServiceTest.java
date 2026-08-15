package com.classification.domain_system.service;

import com.classification.domain_system.dto.SmartQueryDto;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SmartQueryParserServiceTest {

    @Mock private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private RecordRepository recordRepository;

    @InjectMocks
    private SmartQueryParserService smartQueryParserService;

    private UUID domainId;
    private Record rec1;
    private Record rec2;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();

        rec1 = new Record();
        rec1.setId(UUID.randomUUID());
        rec1.setData("{\"grade\":\"VIP\",\"address\":\"서울시 강남구\"}");

        rec2 = new Record();
        rec2.setId(UUID.randomUUID());
        rec2.setData("{\"grade\":\"NORMAL\",\"address\":\"부산시 해운대구\"}");
    }

    @Test
    @DisplayName("parseAndExecute: 자연어 질의어 파싱 및 조건 부합 레코드 탐색")
    void testParseAndExecute() {
        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(Collections.emptyList());
        when(recordRepository.findAllByDomainId(domainId)).thenReturn(List.of(rec1, rec2));

        SmartQueryDto.SmartQueryResponse res = smartQueryParserService.parseAndExecute(domainId, "VIP 고객 중 서울 거주자 검색");

        assertThat(res).isNotNull();
        assertThat(res.getMatchedRecordCount()).isEqualTo(1);
        assertThat(res.getRecords()).hasSize(1);
        assertThat(res.getRecords().get(0)).containsEntry("grade", "VIP");
    }
}
