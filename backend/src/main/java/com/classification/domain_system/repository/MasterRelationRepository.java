package com.classification.domain_system.repository;

import com.classification.domain_system.entity.MasterRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MasterRelationRepository extends JpaRepository<MasterRelation, UUID> {
    List<MasterRelation> findBySourceDomainId(UUID sourceDomainId);
    List<MasterRelation> findByTargetDomainId(UUID targetDomainId);
    Optional<MasterRelation> findBySourceDomainIdAndSourceFieldKey(UUID sourceDomainId, String sourceFieldKey);
}
