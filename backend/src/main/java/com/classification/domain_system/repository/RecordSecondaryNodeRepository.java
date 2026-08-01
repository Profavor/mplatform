package com.classification.domain_system.repository;

import com.classification.domain_system.entity.RecordSecondaryNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecordSecondaryNodeRepository extends JpaRepository<RecordSecondaryNode, UUID> {

    List<RecordSecondaryNode> findByRecordId(UUID recordId);

    Optional<RecordSecondaryNode> findByRecordIdAndAxisId(UUID recordId, UUID axisId);

    @Modifying
    @Query("DELETE FROM RecordSecondaryNode r WHERE r.recordId = :recordId")
    void deleteByRecordId(@Param("recordId") UUID recordId);

    @Modifying
    @Query("DELETE FROM RecordSecondaryNode r WHERE r.recordId = :recordId AND r.axisId = :axisId")
    void deleteByRecordIdAndAxisId(@Param("recordId") UUID recordId, @Param("axisId") UUID axisId);

    List<RecordSecondaryNode> findByNodeId(UUID nodeId);
}
