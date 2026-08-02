package com.classification.domain_system.repository;

import com.classification.domain_system.entity.RecordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecordHistoryRepository extends JpaRepository<RecordHistory, UUID> {
    List<RecordHistory> findByRecordIdOrderByChangedAtDesc(UUID recordId);
    List<RecordHistory> findByRecordIdOrderByVersionAsc(UUID recordId);
}
