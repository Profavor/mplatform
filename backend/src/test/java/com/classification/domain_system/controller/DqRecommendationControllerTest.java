package com.classification.domain_system.controller;

import com.classification.domain_system.dto.DqRecommendationResponse;
import com.classification.domain_system.service.DqRecommendationService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DqRecommendationControllerTest {

    @Mock
    private DqRecommendationService dqRecommendationService;

    @InjectMocks
    private DqRecommendationController dqRecommendationController;

    @Test
    @DisplayName("도메인 데이터 품질(DQ) 추천 규칙 목록 조회 성공")
    void testGetRecommendations_Success() {
        UUID domainId = UUID.randomUUID();
        DqRecommendationResponse responseDto = DqRecommendationResponse.builder()
                .id("REC_01")
                .fieldName("email")
                .recommendedRuleType("REGEX")
                .reason("이메일 형식 패턴 매칭 추천")
                .suggestedParameter("^[A-Za-z0-9+_.-]+@(.+)$")
                .confidenceScore(90)
                .build();

        when(dqRecommendationService.getRecommendations(domainId)).thenReturn(List.of(responseDto));

        ResponseEntity<List<DqRecommendationResponse>> response = dqRecommendationController.getRecommendations(domainId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("REC_01", response.getBody().get(0).getId());
        assertEquals("email", response.getBody().get(0).getFieldName());
        verify(dqRecommendationService).getRecommendations(domainId);
    }
}
