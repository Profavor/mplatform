package com.classification.domain_system.repository;

import com.classification.domain_system.entity.RecordHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecordHistoryRepository extends JpaRepository<RecordHistory, UUID> {
    List<RecordHistory> findByRecordIdOrderByChangedAtDesc(UUID recordId);
    List<RecordHistory> findByRecordIdOrderByVersionAsc(UUID recordId);

    void deleteByRecordId(UUID recordId);

    @Query("SELECT h FROM RecordHistory h LEFT JOIN FETCH h.record r LEFT JOIN FETCH r.node n WHERE r.node.domain.id = :domainId ORDER BY h.changedAt DESC")
    List<RecordHistory> findRecentByDomainId(@Param("domainId") UUID domainId, Pageable pageable);

    @Query("SELECT h FROM RecordHistory h LEFT JOIN FETCH h.record r LEFT JOIN FETCH r.node n WHERE h.recordId = :recordId ORDER BY h.changedAt DESC")
    List<RecordHistory> findRecentByRecordId(@Param("recordId") UUID recordId, Pageable pageable);

    @Query("SELECT h FROM RecordHistory h JOIN h.record r JOIN r.node n WHERE n.domain.id = :domainId ORDER BY h.changedAt DESC")
    List<RecordHistory> findTop50ByDomainIdOrderByChangedAtDesc(@Param("domainId") UUID domainId);
}
