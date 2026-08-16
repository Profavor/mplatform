package com.classification.domain_system.repository;

import com.classification.domain_system.entity.FieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FieldDefinitionRepository extends JpaRepository<FieldDefinition, UUID> {
    @org.springframework.data.jpa.repository.Query("SELECT f FROM FieldDefinition f LEFT JOIN f.fieldGroup fg LEFT JOIN fg.sector s WHERE f.definedAtNode.id = :nodeId AND (f.isRemoved = false OR f.isRemoved IS NULL) ORDER BY COALESCE(s.sortOrder, 9999) ASC, COALESCE(fg.sortOrder, 9999) ASC, f.order ASC")
    List<FieldDefinition> findNodeFieldsWithSort(@org.springframework.data.repository.query.Param("nodeId") UUID nodeId);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM FieldDefinition f LEFT JOIN f.fieldGroup fg LEFT JOIN fg.sector s WHERE f.domain.id = :domainId AND (f.isRemoved = false OR f.isRemoved IS NULL) ORDER BY COALESCE(s.sortOrder, 9999) ASC, COALESCE(fg.sortOrder, 9999) ASC, f.order ASC")
    List<FieldDefinition> findDomainFieldsWithSort(@org.springframework.data.repository.query.Param("domainId") UUID domainId);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM FieldDefinition f WHERE f.definedAtNode.id IN :nodeIds AND (f.isRemoved = false OR f.isRemoved IS NULL)")
    List<FieldDefinition> findByDefinedAtNode_IdIn(@org.springframework.data.repository.query.Param("nodeIds") List<UUID> nodeIds);

    List<FieldDefinition> findByType(String type);

    List<FieldDefinition> findByKey(String key);

    List<FieldDefinition> findByDomain_Id(UUID domainId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT f FROM FieldDefinition f LEFT JOIN f.fieldGroup fg LEFT JOIN fg.sector s WHERE (f.definedAtNode.id = :nodeId OR f.domain.id = :domainId) AND (f.isRemoved = false OR f.isRemoved IS NULL) ORDER BY CASE WHEN f.domain IS NOT NULL THEN 0 ELSE 1 END ASC, COALESCE(s.sortOrder, 9999) ASC, COALESCE(fg.sortOrder, 9999) ASC, f.order ASC",
            countQuery = "SELECT count(f) FROM FieldDefinition f WHERE (f.definedAtNode.id = :nodeId OR f.domain.id = :domainId) AND (f.isRemoved = false OR f.isRemoved IS NULL)")
    org.springframework.data.domain.Page<FieldDefinition> findEffectiveFieldsWithPagination(@org.springframework.data.repository.query.Param("nodeId") UUID nodeId, @org.springframework.data.repository.query.Param("domainId") UUID domainId, org.springframework.data.domain.Pageable pageable);
}
