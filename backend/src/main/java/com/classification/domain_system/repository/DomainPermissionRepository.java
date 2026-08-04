package com.classification.domain_system.repository;

import com.classification.domain_system.entity.DomainPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface DomainPermissionRepository extends JpaRepository<DomainPermission, UUID> {
    List<DomainPermission> findByUserId(String userId);
    List<DomainPermission> findByDomainId(UUID domainId);
    Optional<DomainPermission> findByUserIdAndDomainId(String userId, UUID domainId);
    void deleteByUserIdAndDomainId(String userId, UUID domainId);
    void deleteByUserId(String userId);
}
