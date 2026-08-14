package com.classification.domain_system.controller;

import com.classification.domain_system.dto.DataProfilingResponse;
import com.classification.domain_system.service.DataProfilingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataProfilingControllerTest {

    @Mock
    private DataProfilingService dataProfilingService;

    @InjectMocks
    private DataProfilingController dataProfilingController;

    @Test
    @DisplayName("도메인 데이터 프로파일링 결과 조회 성공")
    void testGetDomainProfiling_Success() {
        UUID domainId = UUID.randomUUID();
        DataProfilingResponse responseDto = DataProfilingResponse.builder()
                .fieldName("email")
                .totalCount(100L)
                .nullCount(5L)
                .nullRatio(0.05)
                .cardinality(95L)
                .topValues(Map.of("test@example.com", 1L))
                .build();

        when(dataProfilingService.profileDomainData(domainId)).thenReturn(List.of(responseDto));

        ResponseEntity<List<DataProfilingResponse>> response = dataProfilingController.getDomainProfiling(domainId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("email", response.getBody().get(0).getFieldName());
        assertEquals(100L, response.getBody().get(0).getTotalCount());
        verify(dataProfilingService).profileDomainData(domainId);
    }
}
