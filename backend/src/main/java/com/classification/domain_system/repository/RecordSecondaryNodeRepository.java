package com.classification.domain_system.repository;

import com.classification.domain_system.entity.RecordSecondaryNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecordSecondaryNodeRepository extends JpaRepository<RecordSecondaryNode, UUID> {

    List<RecordSecondaryNode> findByRecordId(UUID recordId);

    Optional<RecordSecondaryNode> findByRecordIdAndAxisId(UUID recordId, UUID axisId);

    void deleteByRecordId(UUID recordId);

    void deleteByRecordIdAndAxisId(UUID recordId, UUID axisId);

    List<RecordSecondaryNode> findByNodeId(UUID nodeId);
}
