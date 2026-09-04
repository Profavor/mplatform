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
    
    Page<Record> findBySearchableDataContainingIgnoreCase(String keyword, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(
        value = "SELECT r.* FROM record r "
              + "JOIN classification_node n ON r.node_id = n.id "
              + "JOIN domain d ON n.domain_id = d.id "
              + "WHERE r.status NOT IN ('REJECTED', 'MISMATCHED') "
              + "AND (LOWER(CAST(r.data AS text)) LIKE LOWER(CONCAT('%', :keyword, '%')) "
              + "     OR LOWER(COALESCE(CAST(r.searchable_data AS text), '')) LIKE LOWER(CONCAT('%', :keyword, '%')) "
              + "     OR CAST(r.id AS text) LIKE CONCAT('%', :keyword, '%')) "
              + "ORDER BY r.created_at DESC",
        countQuery = "SELECT count(r.id) FROM record r "
                   + "JOIN classification_node n ON r.node_id = n.id "
                   + "JOIN domain d ON n.domain_id = d.id "
                   + "WHERE r.status NOT IN ('REJECTED', 'MISMATCHED') "
                   + "AND (LOWER(CAST(r.data AS text)) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                   + "     OR LOWER(COALESCE(CAST(r.searchable_data AS text), '')) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                   + "     OR CAST(r.id AS text) LIKE CONCAT('%', :keyword, '%'))",
        nativeQuery = true
    )
    Page<Record> searchGlobalRecords(@org.springframework.data.repository.query.Param("keyword") String keyword, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM record r "
            + "WHERE r.id <> :excludedRecordId "
            + "AND CAST(r.data AS jsonb) ->> CAST(:fieldKey AS text) = :recordId "
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

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(r) FROM Record r WHERE r.node.domain.id = :domainId")
    long countByDomainId(@org.springframework.data.repository.query.Param("domainId") UUID domainId);

    List<Record> findByNode_Domain_Id(UUID domainId);

    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Record r WHERE r.node.domain.id = :domainId AND cast(r.data as String) LIKE %:searchText%")
    boolean existsByNodeDomainIdAndDataContaining(@org.springframework.data.repository.query.Param("domainId") UUID domainId, @org.springframework.data.repository.query.Param("searchText") String searchText);

    @org.springframework.data.jpa.repository.Query(value = "SELECT EXISTS (SELECT 1 FROM record r " +
            "JOIN classification_node n ON r.node_id = n.id " +
            "WHERE n.domain_id = :domainId " +
            "AND CAST(r.data AS jsonb) ->> CAST(:fieldKey AS text) = :recordId " +
            "AND r.status NOT IN ('REJECTED', 'MERGED'))", nativeQuery = true)
    boolean existsReferencingRecord(
            @org.springframework.data.repository.query.Param("domainId") UUID domainId,
            @org.springframework.data.repository.query.Param("fieldKey") String fieldKey,
            @org.springframework.data.repository.query.Param("recordId") String recordId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT r.* FROM record r " +
            "JOIN classification_node n ON r.node_id = n.id " +
            "WHERE n.domain_id = :domainId " +
            "AND (CAST(r.data AS jsonb) ->> CAST(:fieldKey AS text) = :fieldValue " +
            "     OR CAST(r.data AS jsonb) ->> LOWER(CAST(:fieldKey AS text)) = :fieldValue) " +
            "AND r.status NOT IN ('REJECTED', 'MERGED') " +
            "ORDER BY r.created_at ASC", nativeQuery = true)
    List<Record> findActiveRecordsByDomainAndFieldValue(
            @org.springframework.data.repository.query.Param("domainId") UUID domainId,
            @org.springframework.data.repository.query.Param("fieldKey") String fieldKey,
            @org.springframework.data.repository.query.Param("fieldValue") String fieldValue);
}
