package com.classification.domain_system.controller;

import com.classification.domain_system.dto.SchemaImpactAnalysisDto;
import com.classification.domain_system.service.SchemaImpactAnalysisService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchemaImpactAnalysisControllerTest {

    @Mock
    private SchemaImpactAnalysisService schemaImpactAnalysisService;

    @InjectMocks
    private SchemaImpactAnalysisController schemaImpactAnalysisController;

    @Test
    @DisplayName("스키마 변경 영향도 분석(Impact Analysis) 요청 성공")
    void testAnalyzeImpact_Success() {
        UUID domainId = UUID.randomUUID();
        SchemaImpactAnalysisDto.ImpactAnalysisRequest request = new SchemaImpactAnalysisDto.ImpactAnalysisRequest();
        request.setChangeType("DELETE_FIELD");
        request.setFieldKey("email");

        SchemaImpactAnalysisDto.ImpactAnalysisResponse mockResponse = new SchemaImpactAnalysisDto.ImpactAnalysisResponse();
        mockResponse.setDomainId(domainId);
        mockResponse.setRiskLevel("HIGH");
        mockResponse.setTotalAffectedRecords(150L);

        when(schemaImpactAnalysisService.analyzeImpact(eq(domainId), any(SchemaImpactAnalysisDto.ImpactAnalysisRequest.class)))
                .thenReturn(mockResponse);

        ResponseEntity<SchemaImpactAnalysisDto.ImpactAnalysisResponse> response = schemaImpactAnalysisController.analyzeImpact(domainId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("HIGH", response.getBody().getRiskLevel());
        assertEquals(150L, response.getBody().getTotalAffectedRecords());
        verify(schemaImpactAnalysisService).analyzeImpact(eq(domainId), any(SchemaImpactAnalysisDto.ImpactAnalysisRequest.class));
    }
}
