package com.classification.domain_system.repository;

import com.classification.domain_system.entity.DqRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DqRuleRepository extends JpaRepository<DqRule, UUID> {
    List<DqRule> findByFieldDefinition_IdAndIsActiveTrueOrderBySortOrderAsc(UUID fieldDefinitionId);

    List<DqRule> findByFieldDefinition_IdInAndIsActiveTrueOrderBySortOrderAsc(List<UUID> fieldDefinitionIds);

    List<DqRule> findByFieldDefinition_IdOrderBySortOrderAsc(UUID fieldDefinitionId);

    List<DqRule> findByDomainIdAndIsActiveTrueOrderBySortOrderAsc(UUID domainId);

    List<DqRule> findByDomainId(UUID domainId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(r) FROM DqRule r " +
        "LEFT JOIN r.fieldDefinition f " +
        "LEFT JOIN f.domain fd " +
        "LEFT JOIN f.definedAtNode fn " +
        "LEFT JOIN fn.domain fnd " +
        "LEFT JOIN f.fieldGroup fg " +
        "LEFT JOIN fg.domain fgd " +
        "LEFT JOIN fg.sector s " +
        "LEFT JOIN s.domain sd " +
        "WHERE r.isActive = true AND (" +
        "  r.domainId = :domainId OR " +
        "  fd.id = :domainId OR " +
        "  fnd.id = :domainId OR " +
        "  fgd.id = :domainId OR " +
        "  sd.id = :domainId" +
        ")"
    )
    long countByDomainId(@org.springframework.data.repository.query.Param("domainId") UUID domainId);

    void deleteByFieldDefinition_Id(UUID fieldDefinitionId);
}
