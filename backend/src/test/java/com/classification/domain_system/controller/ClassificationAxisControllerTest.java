package com.classification.domain_system.controller;

import com.classification.domain_system.dto.ClassificationAxisRequest;
import com.classification.domain_system.dto.ClassificationAxisResponse;
import com.classification.domain_system.service.ClassificationAxisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassificationAxisControllerTest {

    @Mock
    private ClassificationAxisService axisService;

    @InjectMocks
    private ClassificationAxisController axisController;

    private UUID domainId;
    private UUID axisId;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        axisId = UUID.randomUUID();
    }

    @Test
    @DisplayName("도메인 분류축 목록을 조회한다")
    void getAxes_ReturnsList() {
        ClassificationAxisResponse res1 = ClassificationAxisResponse.builder()
                .id(axisId)
                .axisCode("DEFAULT")
                .name(Map.of("ko", "기본 축"))
                .isDefault(true)
                .build();

        when(axisService.getAxesByDomain(domainId)).thenReturn(List.of(res1));

        ResponseEntity<List<ClassificationAxisResponse>> response = axisController.getAxes(domainId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getAxisCode()).isEqualTo("DEFAULT");
    }

    @Test
    @DisplayName("신규 분류축을 생성한다")
    void createAxis_Success() {
        ClassificationAxisRequest req = new ClassificationAxisRequest();
        req.setAxisCode("DEPT");

        ClassificationAxisResponse res = ClassificationAxisResponse.builder()
                .id(axisId)
                .axisCode("DEPT")
                .build();

        when(axisService.createAxis(eq(domainId), any())).thenReturn(res);

        ResponseEntity<ClassificationAxisResponse> response = axisController.createAxis(domainId, req);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getAxisCode()).isEqualTo("DEPT");
    }

    @Test
    @DisplayName("분류축을 삭제한다")
    void deleteAxis_Success() {
        ResponseEntity<Void> response = axisController.deleteAxis(axisId);

        verify(axisService).deleteAxis(axisId);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
}
