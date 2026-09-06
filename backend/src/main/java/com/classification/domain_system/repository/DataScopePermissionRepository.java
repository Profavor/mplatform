package com.classification.domain_system.repository;

import com.classification.domain_system.entity.DataScopePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DataScopePermissionRepository extends JpaRepository<DataScopePermission, UUID> {

    List<DataScopePermission> findByUserId(String userId);

    List<DataScopePermission> findByUserIdAndDomainId(String userId, UUID domainId);

    Optional<DataScopePermission> findByUserIdAndDomainIdAndNodeId(String userId, UUID domainId, UUID nodeId);

    void deleteByUserIdAndDomainId(String userId, UUID domainId);
}
