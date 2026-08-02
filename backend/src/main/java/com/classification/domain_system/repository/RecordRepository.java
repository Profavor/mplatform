package com.classification.domain_system.repository;

import com.classification.domain_system.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface RecordRepository extends JpaRepository<Record, UUID>, CustomRecordRepository {
    Page<Record> findByNodeId(UUID nodeId, Pageable pageable);
    
    @org.springframework.data.jpa.repository.Query(value = "SELECT r FROM Record r JOIN FETCH r.node WHERE r.node.id IN :nodeIds AND r.status NOT IN ('REJECTED', 'MISMATCHED')", countQuery = "SELECT count(r) FROM Record r WHERE r.node.id IN :nodeIds AND r.status NOT IN ('REJECTED', 'MISMATCHED')")
    Page<Record> findByNodeIdIn(@org.springframework.data.repository.query.Param("nodeIds") java.util.List<UUID> nodeIds, Pageable pageable);

    Page<Record> findByNodeIdAndStatus(UUID nodeId, String status, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM record r "
            + "WHERE r.id <> :excludedRecordId "
            + "AND r.data ->> CAST(:fieldKey AS text) = :recordId "
            + "AND r.status NOT IN ('REJECTED', 'MERGED')", nativeQuery = true)
    List<Record> findReferencingRecords(
            @org.springframework.data.repository.query.Param("fieldKey") String fieldKey,
            @org.springframework.data.repository.query.Param("recordId") String recordId,
            @org.springframework.data.repository.query.Param("excludedRecordId") UUID excludedRecordId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT r FROM Record r JOIN FETCH r.node WHERE r.node.domain.id = :domainId AND r.status NOT IN ('REJECTED', 'MISMATCHED')", countQuery = "SELECT count(r) FROM Record r WHERE r.node.domain.id = :domainId AND r.status NOT IN ('REJECTED', 'MISMATCHED')")
    Page<Record> findByDomainId(@org.springframework.data.repository.query.Param("domainId") UUID domainId, Pageable pageable);
    
    long countByStatus(String status);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(r) FROM Record r WHERE r.node.domain.id = :domainId AND r.status = :status")
    long countByNodeDomainIdAndStatus(@org.springframework.data.repository.query.Param("domainId") UUID domainId, @org.springframework.data.repository.query.Param("status") String status);

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Record r JOIN FETCH r.node WHERE r.node.domain.id = :domainId AND r.status NOT IN ('REJECTED', 'MISMATCHED') ORDER BY r.createdAt DESC")
    List<Record> findAllByDomainId(@org.springframework.data.repository.query.Param("domainId") UUID domainId);
}
