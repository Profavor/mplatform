package com.classification.domain_system.service.dq;

import com.classification.domain_system.dto.DqRuleRequest;
import com.classification.domain_system.dto.DqRuleResponse;

import java.util.List;
import java.util.UUID;

public interface DqRuleService {

    List<DqRuleResponse> getRulesByField(UUID fieldId);

    DqRuleResponse createRule(UUID fieldId, DqRuleRequest request);

    DqRuleResponse updateRule(UUID ruleId, DqRuleRequest request);

    void deleteRule(UUID ruleId);
}
