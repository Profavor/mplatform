package com.classification.domain_system.repository;

import com.classification.domain_system.entity.CodeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodeGroupRepository extends JpaRepository<CodeGroup, UUID> {
    Optional<CodeGroup> findByGroupCode(String groupCode);
    List<CodeGroup> findByIsActiveTrue();
    List<CodeGroup> findByOrganizationId(UUID organizationId);

    @Query("SELECT c FROM CodeGroup c WHERE LOWER(c.groupCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(CAST(c.name AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    org.springframework.data.domain.Page<CodeGroup> searchByKeyword(@Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);
}
