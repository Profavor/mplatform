package com.classification.domain_system.repository;

import com.classification.domain_system.entity.BusinessTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessTermRepository extends JpaRepository<BusinessTerm, UUID> {
    List<BusinessTerm> findByDomainId(UUID domainId);
    Optional<BusinessTerm> findByTermCode(String termCode);
}
