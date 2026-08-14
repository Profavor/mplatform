package com.classification.domain_system.controller;

import com.classification.domain_system.dto.RecordLineageDto;
import com.classification.domain_system.service.RecordLineageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordLineageControllerTest {

    @Mock
    private RecordLineageService recordLineageService;

    @InjectMocks
    private RecordLineageController recordLineageController;

    @Test
    @DisplayName("레코드 데이터 리니지(계보) 조회 성공")
    void testGetRecordLineage_Success() {
        UUID recordId = UUID.randomUUID();
        RecordLineageDto.RecordLineageResponse mockResponse = new RecordLineageDto.RecordLineageResponse();
        when(recordLineageService.getRecordLineage(recordId)).thenReturn(mockResponse);

        ResponseEntity<RecordLineageDto.RecordLineageResponse> response = recordLineageController.getRecordLineage(recordId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(recordLineageService).getRecordLineage(recordId);
    }
}
