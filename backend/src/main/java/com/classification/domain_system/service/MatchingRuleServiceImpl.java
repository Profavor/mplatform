package com.classification.domain_system.service;

import com.classification.domain_system.entity.MatchingRule;
import com.classification.domain_system.repository.MatchingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingRuleServiceImpl implements MatchingRuleService {

    private final MatchingRuleRepository matchingRuleRepository;

    @Override
    public List<MatchingRule> getRulesByDomainId(UUID domainId) {
        return matchingRuleRepository.findByDomainId(domainId);
    }

    @Override
    @Transactional
    public MatchingRule createRule(UUID domainId, MatchingRule rule) {
        rule.setDomainId(domainId);
        return matchingRuleRepository.save(rule);
    }

    @Override
    @Transactional
    public MatchingRule updateRule(UUID domainId, UUID ruleId, MatchingRule rule) {
        MatchingRule existing = matchingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + ruleId));
        existing.setRuleName(rule.getRuleName());
        existing.setTargetFieldKeys(rule.getTargetFieldKeys());
        existing.setMatchType(rule.getMatchType());
        existing.setActive(rule.isActive());
        return matchingRuleRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteRule(UUID domainId, UUID ruleId) {
        matchingRuleRepository.deleteById(ruleId);
    }
}
