package com.classification.domain_system.service;

import com.classification.domain_system.dto.DomainSnapshotDto;
import com.classification.domain_system.entity.DomainSnapshot;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.DomainSnapshotRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DomainSnapshotService {

    private final DomainSnapshotRepository domainSnapshotRepository;
    private final RecordRepository recordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public DomainSnapshotDto.SnapshotResponse createSnapshot(UUID domainId, DomainSnapshotDto.SnapshotCreateRequest req, String username) {
        List<Record> records = recordRepository.findAllByDomainId(domainId);

        List<Map<String, Object>> recordsSnapshot = new ArrayList<>();
        for (Record r : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId().toString());
            item.put("nodeId", r.getNode() != null ? r.getNode().getId().toString() : null);
            item.put("data", r.getData());
            item.put("status", r.getStatus());
            recordsSnapshot.add(item);
        }

        String dataJson;
        try {
            dataJson = objectMapper.writeValueAsString(recordsSnapshot);
        } catch (Exception e) {
            dataJson = "[]";
        }

        DomainSnapshot snapshot = DomainSnapshot.builder()
                .domainId(domainId)
                .snapshotName(req.getSnapshotName() != null ? req.getSnapshotName() : "정기 스냅샷")
                .versionTag(req.getVersionTag() != null ? req.getVersionTag() : "v1.0")
                .recordCount(records.size())
                .snapshotData(dataJson)
                .createdBy(username != null ? username : "SYSTEM")
                .build();

        DomainSnapshot saved = domainSnapshotRepository.save(snapshot);

        return DomainSnapshotDto.SnapshotResponse.builder()
                .snapshotId(saved.getId())
                .domainId(saved.getDomainId())
                .snapshotName(saved.getSnapshotName())
                .versionTag(saved.getVersionTag())
                .recordCount(saved.getRecordCount())
                .createdBy(saved.getCreatedBy())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<DomainSnapshotDto.SnapshotResponse> getSnapshots(UUID domainId) {
        return domainSnapshotRepository.findByDomainIdOrderByCreatedAtDesc(domainId).stream()
                .map(s -> DomainSnapshotDto.SnapshotResponse.builder()
                        .snapshotId(s.getId())
                        .domainId(s.getDomainId())
                        .snapshotName(s.getSnapshotName())
                        .versionTag(s.getVersionTag())
                        .recordCount(s.getRecordCount())
                        .createdBy(s.getCreatedBy())
                        .createdAt(s.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public DomainSnapshotDto.SnapshotRestoreResponse restoreSnapshot(UUID snapshotId) {
        DomainSnapshot snapshot = domainSnapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new ResourceNotFoundException("Domain snapshot not found: " + snapshotId));

        int restored = 0;
        try {
            List<Map<String, Object>> snapshotList = objectMapper.readValue(
                    snapshot.getSnapshotData(), new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> item : snapshotList) {
                if (item.containsKey("id") && item.get("id") != null) {
                    UUID recId = UUID.fromString(String.valueOf(item.get("id")));
                    Record record = recordRepository.findById(recId).orElse(null);
                    if (record != null) {
                        record.setData(String.valueOf(item.get("data")));
                        if (item.containsKey("status") && item.get("status") != null) {
                            record.setStatus(String.valueOf(item.get("status")));
                        }
                        recordRepository.save(record);
                        restored++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to restore snapshot {}", snapshotId, e);
            throw new RuntimeException("Snapshot restore failed: " + e.getMessage());
        }

        return DomainSnapshotDto.SnapshotRestoreResponse.builder()
                .snapshotId(snapshotId)
                .restoredRecords(restored)
                .status("SUCCESS")
                .message(String.format("총 %d건의 레코드가 스냅샷 시점(%s)으로 안전하게 복원되었습니다.", restored, snapshot.getVersionTag()))
                .build();
    }
}
