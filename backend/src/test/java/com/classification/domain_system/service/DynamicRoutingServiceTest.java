package com.classification.domain_system.service;

import com.classification.domain_system.dto.ApprovalRoutingDto;
import com.classification.domain_system.entity.ApprovalRoutingTemplate;
import com.classification.domain_system.repository.ApprovalRoutingTemplateRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DynamicRoutingServiceTest {

    @Mock private ApprovalRoutingTemplateRepository templateRepository;

    @InjectMocks
    private DynamicRoutingService dynamicRoutingService;

    private ApprovalRoutingTemplate vipTemplate;

    @BeforeEach
    void setUp() {
        vipTemplate = ApprovalRoutingTemplate.builder()
                .id(UUID.randomUUID())
                .templateName("VIP 고객 전용 2단계 결재선")
                .conditionField("grade")
                .conditionOperator("EQUALS")
                .conditionValue("VIP")
                .stepsJson("[{\"stepOrder\":1,\"requiredRole\":\"ROLE_DEPT_HEAD\",\"stepName\":\"부서장 검토\"},{\"stepOrder\":2,\"requiredRole\":\"ROLE_EXECUTIVE\",\"stepName\":\"임원 최종 승인\"}]")
                .build();
    }

    @Test
    @DisplayName("createTemplate: 동적 결재선 템플릿 생성 및 반환 검증")
    void testCreateTemplate() {
        when(templateRepository.save(any(ApprovalRoutingTemplate.class))).thenReturn(vipTemplate);

        ApprovalRoutingDto.TemplateCreateRequest request = ApprovalRoutingDto.TemplateCreateRequest.builder()
                .templateName("VIP 고객 전용 2단계 결재선")
                .conditionField("grade")
                .conditionOperator("EQUALS")
                .conditionValue("VIP")
                .build();

        ApprovalRoutingDto.TemplateResponse res = dynamicRoutingService.createTemplate(request);

        assertThat(res).isNotNull();
        assertThat(res.getTemplateName()).isEqualTo("VIP 고객 전용 2단계 결재선");
        assertThat(res.getSteps()).hasSize(2);
    }

    @Test
    @DisplayName("evaluateRoute: 레코드 데이터 조건 매칭 시 동적 다단계 결재선 자동 배정")
    void testEvaluateRouteMatched() {
        when(templateRepository.findAll()).thenReturn(List.of(vipTemplate));

        ApprovalRoutingDto.EvaluateRouteRequest req = ApprovalRoutingDto.EvaluateRouteRequest.builder()
                .recordData(Map.of("name", "홍길동", "grade", "VIP"))
                .build();

        ApprovalRoutingDto.EvaluateRouteResponse response = dynamicRoutingService.evaluateRoute(req);

        assertThat(response).isNotNull();
        assertThat(response.getMatchedTemplateName()).isEqualTo("VIP 고객 전용 2단계 결재선");
        assertThat(response.getDynamicSteps()).hasSize(2);
        assertThat(response.getDynamicSteps().get(0).getRequiredRole()).isEqualTo("ROLE_DEPT_HEAD");
        assertThat(response.getDynamicSteps().get(1).getRequiredRole()).isEqualTo("ROLE_EXECUTIVE");
    }
}
