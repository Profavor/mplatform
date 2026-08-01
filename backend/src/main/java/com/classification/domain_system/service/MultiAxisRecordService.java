package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordSecondaryNodeResponse;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordSecondaryNode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.RecordSecondaryNodeRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 레코드 다축(Multi-axis) 서브 분류 노드 매핑 서비스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MultiAxisRecordService {

    private final RecordRepository recordRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final RecordSecondaryNodeRepository secondaryNodeRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<RecordSecondaryNodeResponse> getSecondaryNodes(UUID recordId) {
        if (!recordRepository.existsById(recordId)) {
            throw new ResourceNotFoundException("Record not found with id: " + recordId);
        }
        return secondaryNodeRepository.findByRecordId(recordId).stream()
                .map(RecordSecondaryNodeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<RecordSecondaryNodeResponse> setSecondaryNodes(UUID recordId, List<UUID> nodeIds) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + recordId));

        // 기존 매핑 삭제 후 즉시 flush → Unique Constraint(record_id, axis_id) 위반 방지
        secondaryNodeRepository.deleteByRecordId(recordId);
        entityManager.flush();

        if (nodeIds == null || nodeIds.isEmpty()) {
            return List.of();
        }

        List<ClassificationNode> nodes = nodeRepository.findAllById(nodeIds);
        List<RecordSecondaryNode> newMappings = new ArrayList<>();

        for (ClassificationNode node : nodes) {
            RecordSecondaryNode mapping = new RecordSecondaryNode();
            mapping.setRecordId(recordId);
            mapping.setNode(node);
            mapping.setAxisId(node.getAxis() != null ? node.getAxis().getId() : UUID.randomUUID());
            newMappings.add(mapping);
        }

        List<RecordSecondaryNode> saved = secondaryNodeRepository.saveAll(newMappings);
        log.info("[Multi-Axis] Updated secondary nodes for recordId={}: {} nodes mapped", recordId, saved.size());

        return saved.stream()
                .map(RecordSecondaryNodeResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
