package com.classification.domain_system.service;

import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.repository.WorkflowConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkflowConfigServiceTest {

    @Mock
    private WorkflowConfigRepository repository;

    @InjectMocks
    private WorkflowConfigService workflowConfigService;

    @Test
    @DisplayName("평문 description이 입력되었을 때 다국어 JSON으로 안전하게 자동 래핑되는지 검증 (#89)")
    void testSanitizeJsonFields_PlainTextDescription() {
        WorkflowConfig config = new WorkflowConfig();
        config.setName("일반 등록 워크플로우");
        config.setDescription("이것은 일반 평문 설명입니다.");

        workflowConfigService.sanitizeJsonFields(config);

        assertNotNull(config.getDescription());
        assertTrue(config.getDescription().contains("\"ko\":\"이것은 일반 평문 설명입니다.\""));
        assertTrue(config.getDescription().contains("\"en\":\"이것은 일반 평문 설명입니다.\""));

        assertNotNull(config.getName());
        assertTrue(config.getName().contains("\"ko\":\"일반 등록 워크플로우\""));
    }

    @Test
    @DisplayName("이미 JSON 형식인 description과 name은 그대로 유지되는지 검증")
    void testSanitizeJsonFields_AlreadyJson() {
        WorkflowConfig config = new WorkflowConfig();
        config.setName("{\"ko\":\"등록 서식\",\"en\":\"Create Form\"}");
        config.setDescription("{\"ko\":\"설명\",\"en\":\"Description\"}");

        workflowConfigService.sanitizeJsonFields(config);

        assertEquals("{\"ko\":\"등록 서식\",\"en\":\"Create Form\"}", config.getName());
        assertEquals("{\"ko\":\"설명\",\"en\":\"Description\"}", config.getDescription());
    }

    @Test
    @DisplayName("빈 문자열 description은 null로 변환되는지 검증")
    void testSanitizeJsonFields_BlankDescription() {
        WorkflowConfig config = new WorkflowConfig();
        config.setDescription("   ");

        workflowConfigService.sanitizeJsonFields(config);

        assertNull(config.getDescription());
    }
}
