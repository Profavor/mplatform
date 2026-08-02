package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordSearchDto;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RecordSearchServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private RecordSearchService recordSearchService;

    private Record testRecord;
    private UUID domainId;
    private UUID recordId;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        recordId = UUID.randomUUID();
        testRecord = new Record();
        testRecord.setId(recordId);
        testRecord.setData("{\"productName\": \"Master Premium Laptop\"}");
    }

    @Test
    @DisplayName("키워드와 퍼지 검색 옵션으로 레코드 결과와 하이라이트를 반환한다")
    void testSearchRecordsWithHighlight() {
        // given
        given(recordRepository.findByDomainId(eq(domainId), any(PageRequest.class)))
                .willReturn(new PageImpl<>(Collections.singletonList(testRecord)));

        RecordSearchDto.SearchRequest request = new RecordSearchDto.SearchRequest();
        request.setDomainId(domainId);
        request.setKeyword("Laptop");
        request.setFuzzy(true);

        // when
        RecordSearchDto.SearchResponse response = recordSearchService.searchRecords(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRecordId()).isEqualTo(recordId);
        assertThat(response.getItems().get(0).getHighlights()).containsKey("data");
    }
}
