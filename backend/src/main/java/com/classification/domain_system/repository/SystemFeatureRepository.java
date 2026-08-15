package com.classification.domain_system.repository;

import com.classification.domain_system.entity.SystemFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemFeatureRepository extends JpaRepository<SystemFeature, Long> {
    List<SystemFeature> findByIsGovernanceCoreTrueOrderByFeatureNoAsc();
    List<SystemFeature> findAllByOrderByFeatureNoAsc();
}
