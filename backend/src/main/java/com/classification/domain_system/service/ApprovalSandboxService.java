package com.classification.domain_system.service;

import com.classification.domain_system.dto.ApprovalSandboxDto;
import com.classification.domain_system.dto.RecordTimeMachineDto;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalSandboxService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final RecordRepository recordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public ApprovalSandboxDto.SandboxPreviewResponse generateSandboxPreview(UUID requestId) {
        ApprovalRequest approval = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found: " + requestId));

        UUID targetId = approval.getTargetId();
        Record record = targetId != null ? recordRepository.findById(targetId).orElse(null) : null;

        Map<String, Object> currentData = record != null ? parseData(record.getData()) : new HashMap<>();
        Map<String, Object> changesMap = parseData(approval.getChanges());

        Map<String, Object> simulatedData = new HashMap<>(currentData);

        // If changes contains nested 'data', unpack it
        if (changesMap.containsKey("data") && changesMap.get("data") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) changesMap.get("data");
            simulatedData.putAll(nested);
        } else {
            simulatedData.putAll(changesMap);
        }

        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(currentData.keySet());
        allKeys.addAll(simulatedData.keySet());

        List<RecordTimeMachineDto.FieldDiffItem> diffItems = new ArrayList<>();
        for (String key : allKeys) {
            Object val1 = currentData.get(key);
            Object val2 = simulatedData.get(key);
            String str1 = val1 != null ? String.valueOf(val1) : null;
            String str2 = val2 != null ? String.valueOf(val2) : null;

            String status;
            if (str1 == null && str2 != null) {
                status = "ADDED";
            } else if (str1 != null && str2 == null) {
                status = "REMOVED";
            } else if (!Objects.equals(str1, str2)) {
                status = "MODIFIED";
            } else {
                status = "UNCHANGED";
            }

            diffItems.add(RecordTimeMachineDto.FieldDiffItem.builder()
                    .fieldKey(key)
                    .fieldName(key)
                    .v1Value(str1 != null ? str1 : "-")
                    .v2Value(str2 != null ? str2 : "-")
                    .diffStatus(status)
                    .build());
        }

        String recordCode = targetId != null ? "REC-" + targetId.toString().substring(0, 8) : "NEW-RECORD";
        String actionType = record == null ? "CREATE" : "UPDATE";

        ApprovalSandboxDto.TargetRecordPreview preview = ApprovalSandboxDto.TargetRecordPreview.builder()
                .recordId(targetId)
                .recordCode(recordCode)
                .currentData(currentData)
                .simulatedData(simulatedData)
                .fieldDiffs(diffItems)
                .build();

        return ApprovalSandboxDto.SandboxPreviewResponse.builder()
                .approvalRequestId(requestId)
                .requesterName(approval.getRequesterName())
                .targetType(approval.getTargetType())
                .actionType(actionType)
                .targetRecords(List.of(preview))
                .summary(String.format("결재 승인 시 %d개 필드가 변경/반영됩니다.", diffItems.stream().filter(d -> !d.getDiffStatus().equals("UNCHANGED")).count()))
                .build();
    }

    private Map<String, Object> parseData(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
