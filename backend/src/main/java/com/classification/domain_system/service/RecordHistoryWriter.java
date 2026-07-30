package com.classification.domain_system.service;

import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.repository.RecordHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RecordHistoryWriter {

    private final RecordHistoryRepository recordHistoryRepository;

    public void logHistory(Record record, String changeType, String changedBy, String prevData, String newData, UUID approvalRequestId) {
        RecordHistory history = new RecordHistory();
        history.setRecordId(record.getId());
        history.setChangeType(changeType);
        history.setChangedBy(changedBy);
        history.setPreviousData(prevData);
        history.setNewData(newData);
        history.setApprovalRequestId(approvalRequestId);
        history.setVersion(record.getVersion());
        history.setSourceSystem(record.getSourceSystem());
        recordHistoryRepository.save(history);
    }
}
