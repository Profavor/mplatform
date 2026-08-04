package com.classification.domain_system.repository;

import com.classification.domain_system.entity.DomainAccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface DomainAccessRequestRepository extends JpaRepository<DomainAccessRequest, UUID> {
    List<DomainAccessRequest> findByUserId(String userId);
    List<DomainAccessRequest> findByStatus(String status);
    Optional<DomainAccessRequest> findByUserIdAndDomainIdAndStatus(String userId, UUID domainId, String status);
    void deleteByUserId(String userId);
}
