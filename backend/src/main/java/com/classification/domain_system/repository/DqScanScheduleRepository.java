package com.classification.domain_system.repository;

import com.classification.domain_system.entity.DqScanSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DqScanScheduleRepository extends JpaRepository<DqScanSchedule, UUID> {
    Optional<DqScanSchedule> findByDomainId(UUID domainId);
    List<DqScanSchedule> findByIsEnabledTrue();
}
