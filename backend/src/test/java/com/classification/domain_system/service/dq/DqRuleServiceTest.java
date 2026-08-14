package com.classification.domain_system.service.dq;

import com.classification.domain_system.dto.DqRuleRequest;
import com.classification.domain_system.dto.DqRuleResponse;
import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.DqRuleType;
import com.classification.domain_system.entity.DqSeverity;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.repository.DqRuleRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DqRuleServiceTest {

    @Mock
    private DqRuleRepository dqRuleRepository;

    @Mock
    private FieldDefinitionRepository fieldDefinitionRepository;

    @InjectMocks
    private DqRuleServiceImpl dqRuleService;

    private UUID fieldId;
    private UUID ruleId;
    private FieldDefinition field;
    private DqRule rule;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
        ruleId = UUID.randomUUID();

        field = new FieldDefinition();
        field.setId(fieldId);
        field.setKey("email");

        rule = new DqRule();
        rule.setId(ruleId);
        rule.setFieldDefinition(field);
        rule.setRuleType(DqRuleType.REGEX);
        rule.setSeverity(DqSeverity.ERROR);
        rule.setIsActive(true);
    }

    @Test
    @DisplayName("필드별 DQ 룰 목록 조회 성공")
    void getRulesByField_success() {
        given(dqRuleRepository.findByFieldDefinition_IdOrderBySortOrderAsc(fieldId)).willReturn(List.of(rule));

        List<DqRuleResponse> result = dqRuleService.getRulesByField(fieldId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRuleType()).isEqualTo("REGEX");
    }

    @Test
    @DisplayName("DQ 룰 생성 성공")
    void createRule_success() {
        DqRuleRequest request = new DqRuleRequest();
        request.setRuleType("REGEX");
        request.setSeverity("ERROR");
        request.setIsActive(true);

        given(fieldDefinitionRepository.findById(fieldId)).willReturn(Optional.of(field));
        given(dqRuleRepository.save(any(DqRule.class))).willReturn(rule);

        DqRuleResponse created = dqRuleService.createRule(fieldId, request);

        assertThat(created).isNotNull();
        verify(dqRuleRepository).save(any(DqRule.class));
    }

    @Test
    @DisplayName("DQ 룰 삭제 성공")
    void deleteRule_success() {
        given(dqRuleRepository.existsById(ruleId)).willReturn(true);

        dqRuleService.deleteRule(ruleId);

        verify(dqRuleRepository).deleteById(ruleId);
    }
}
