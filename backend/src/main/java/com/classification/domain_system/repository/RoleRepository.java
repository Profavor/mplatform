package com.classification.domain_system.repository;

import com.classification.domain_system.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    List<Role> findByOrganizationId(UUID organizationId);
    List<Role> findByOrganizationIdOrOrganizationIdIsNull(UUID organizationId);
    Optional<Role> findByOrganizationIdAndName(UUID organizationId, String name);
    boolean existsByOrganizationIdAndName(UUID organizationId, String name);
}
