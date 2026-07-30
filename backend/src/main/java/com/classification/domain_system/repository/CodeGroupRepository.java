package com.classification.domain_system.repository;

import com.classification.domain_system.entity.CodeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodeGroupRepository extends JpaRepository<CodeGroup, UUID> {
    Optional<CodeGroup> findByGroupCode(String groupCode);
    List<CodeGroup> findByIsActiveTrue();
    List<CodeGroup> findByOrganizationId(UUID organizationId);
}
