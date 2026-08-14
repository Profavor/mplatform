package com.classification.domain_system.service;

import com.classification.domain_system.entity.MatchingRule;

import java.util.List;
import java.util.UUID;

public interface MatchingRuleService {

    List<MatchingRule> getRulesByDomainId(UUID domainId);

    MatchingRule createRule(UUID domainId, MatchingRule rule);

    MatchingRule updateRule(UUID domainId, UUID ruleId, MatchingRule rule);

    void deleteRule(UUID domainId, UUID ruleId);
}
