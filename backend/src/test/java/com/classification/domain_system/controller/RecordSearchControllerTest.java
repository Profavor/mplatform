package com.classification.domain_system.controller;

import com.classification.domain_system.dto.RecordSearchDto;
import com.classification.domain_system.service.RecordSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordSearchControllerTest {

    @Mock
    private RecordSearchService recordSearchService;

    @InjectMocks
    private RecordSearchController recordSearchController;

    @Test
    @DisplayName("레코드 복합 검색 성공")
    void testSearchRecords_Success() {
        RecordSearchDto.SearchRequest request = new RecordSearchDto.SearchRequest();
        request.setDomainId(UUID.randomUUID());
        request.setKeyword("홍길동");

        RecordSearchDto.SearchItem item = new RecordSearchDto.SearchItem();
        item.setRecordId(UUID.randomUUID());
        item.setRecordCode("REC_100");

        RecordSearchDto.SearchResponse searchResponse = new RecordSearchDto.SearchResponse(
                List.of(item), 1L, 0, 20
        );

        when(recordSearchService.searchRecords(any(RecordSearchDto.SearchRequest.class)))
                .thenReturn(searchResponse);

        ResponseEntity<RecordSearchDto.SearchResponse> response = recordSearchController.searchRecords(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("REC_100", response.getBody().getItems().get(0).getRecordCode());
        verify(recordSearchService).searchRecords(any(RecordSearchDto.SearchRequest.class));
    }
}
