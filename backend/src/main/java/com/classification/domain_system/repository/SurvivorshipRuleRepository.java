package com.classification.domain_system.repository;

import com.classification.domain_system.entity.SurvivorshipRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SurvivorshipRuleRepository extends JpaRepository<SurvivorshipRule, UUID> {
    List<SurvivorshipRule> findByDomainIdOrderByPriorityAsc(UUID domainId);
}
