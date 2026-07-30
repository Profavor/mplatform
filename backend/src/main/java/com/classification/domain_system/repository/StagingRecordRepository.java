package com.classification.domain_system.repository;

import com.classification.domain_system.entity.StagingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StagingRecordRepository extends JpaRepository<StagingRecord, UUID> {
    List<StagingRecord> findByBatchId(UUID batchId);
}
