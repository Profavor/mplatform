package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Record;
import com.classification.domain_system.service.GlobalSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalSearchControllerTest {

    @Mock
    private GlobalSearchService globalSearchService;

    @InjectMocks
    private GlobalSearchController globalSearchController;

    @Test
    @DisplayName("전역 통합 검색 성공")
    void testSearchGlobal_Success() {
        UUID recordId = UUID.randomUUID();
        Record record = new Record();
        record.setId(recordId);
        record.setStatus("ACTIVE");
        record.setData("{\"name\":\"홍길동\"}");

        Page<Record> page = new PageImpl<>(List.of(record));
        when(globalSearchService.searchGlobal(eq("keyword"), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<Record>> response = globalSearchController.searchGlobal("keyword", PageRequest.of(0, 20));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(recordId, response.getBody().getContent().get(0).getId());
        assertEquals("ACTIVE", response.getBody().getContent().get(0).getStatus());
        verify(globalSearchService).searchGlobal(eq("keyword"), any(Pageable.class));
    }
}
