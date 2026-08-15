package com.classification.domain_system.service;

import com.classification.domain_system.dto.DqRemediationDto;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DqRemediationService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^01[016789](\\d{3,4})(\\d{4})$");
    private static final Pattern BIZ_NO_PATTERN = Pattern.compile("^(\\d{3})(\\d{2})(\\d{5})$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private final RecordRepository recordRepository;
    private final RecordHistoryRepository recordHistoryRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public List<DqRemediationDto.RemediationProposal> scanAndPropose(UUID domainId) {
        List<Record> records = recordRepository.findAllByDomainId(domainId);
        List<ClassificationNode> nodes = nodeRepository.findByDomain_Id(domainId);
        List<FieldDefinition> domainFields = fieldDefinitionRepository.findDomainFieldsWithSort(domainId);
        List<UUID> nodeIds = nodes.stream().map(ClassificationNode::getId).collect(Collectors.toList());
        List<FieldDefinition> nodeFields = !nodeIds.isEmpty() ? fieldDefinitionRepository.findByDefinedAtNode_IdIn(nodeIds) : Collections.emptyList();

        Map<String, FieldDefinition> fieldMap = new LinkedHashMap<>();
        domainFields.forEach(f -> fieldMap.put(f.getKey(), f));
        nodeFields.forEach(f -> fieldMap.putIfAbsent(f.getKey(), f));

        List<DqRemediationDto.RemediationProposal> proposals = new ArrayList<>();

        for (Record r : records) {
            String recordCode = "REC-" + r.getId().toString().substring(0, 8);
            Map<String, Object> data = parseData(r.getData());

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                Object valObj = entry.getValue();
                if (valObj == null) continue;

                String val = String.valueOf(valObj);
                FieldDefinition fd = fieldMap.get(key);
                String fieldName = (fd != null && fd.getName() != null)
                        ? fd.getName().getOrDefault("ko", fd.getName().getOrDefault("en", key))
                        : key;

                // 1. Whitespace Trim
                if (val.startsWith(" ") || val.endsWith(" ")) {
                    String trimmed = val.trim();
                    proposals.add(DqRemediationDto.RemediationProposal.builder()
                            .recordId(r.getId())
                            .recordCode(recordCode)
                            .fieldKey(key)
                            .fieldName(fieldName)
                            .currentValue(val)
                            .proposedValue(trimmed)
                            .remediationType("TRIM_WHITESPACE")
                            .reason("문자열 앞/뒤 불필요 공백 제거")
                            .build());
                    continue;
                }

                // 2. Phone Number Formatting (01012345678 -> 010-1234-5678)
                var phoneMatcher = PHONE_PATTERN.matcher(val);
                if (phoneMatcher.matches()) {
                    String formatted = val.replaceAll("^(\\d{3})(\\d{3,4})(\\d{4})$", "$1-$2-$3");
                    proposals.add(DqRemediationDto.RemediationProposal.builder()
                            .recordId(r.getId())
                            .recordCode(recordCode)
                            .fieldKey(key)
                            .fieldName(fieldName)
                            .currentValue(val)
                            .proposedValue(formatted)
                            .remediationType("PHONE_FORMAT")
                            .reason("휴대전화번호 하이픈 표준 포맷팅")
                            .build());
                    continue;
                }

                // 3. Business Registration Number Formatting (1234567890 -> 123-45-67890)
                var bizMatcher = BIZ_NO_PATTERN.matcher(val);
                if (bizMatcher.matches()) {
                    String formatted = val.replaceAll("^(\\d{3})(\\d{2})(\\d{5})$", "$1-$2-$3");
                    proposals.add(DqRemediationDto.RemediationProposal.builder()
                            .recordId(r.getId())
                            .recordCode(recordCode)
                            .fieldKey(key)
                            .fieldName(fieldName)
                            .currentValue(val)
                            .proposedValue(formatted)
                            .remediationType("BIZ_NO_FORMAT")
                            .reason("사업자등록번호 하이픈 표준 포맷팅")
                            .build());
                    continue;
                }

                // 4. Email Lowercase Normalization
                if (EMAIL_PATTERN.matcher(val).matches() && !val.equals(val.toLowerCase())) {
                    proposals.add(DqRemediationDto.RemediationProposal.builder()
                            .recordId(r.getId())
                            .recordCode(recordCode)
                            .fieldKey(key)
                            .fieldName(fieldName)
                            .currentValue(val)
                            .proposedValue(val.toLowerCase())
                            .remediationType("EMAIL_LOWERCASE")
                            .reason("이메일 대소문자 소문자 정규화")
                            .build());
                }
            }
        }

        return proposals;
    }

    @Transactional
    public DqRemediationDto.RemediationApplyResult applyRemediations(UUID domainId, DqRemediationDto.RemediationApplyRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return DqRemediationDto.RemediationApplyResult.builder()
                    .successCount(0)
                    .failedCount(0)
                    .message("No items to apply.")
                    .build();
        }

        Map<UUID, List<DqRemediationDto.ProposalItem>> grouped = request.getItems().stream()
                .collect(Collectors.groupingBy(DqRemediationDto.ProposalItem::getRecordId));

        int success = 0;
        int failed = 0;

        for (Map.Entry<UUID, List<DqRemediationDto.ProposalItem>> entry : grouped.entrySet()) {
            UUID recordId = entry.getKey();
            try {
                Record record = recordRepository.findById(recordId).orElse(null);
                if (record == null) {
                    failed += entry.getValue().size();
                    continue;
                }

                String previousData = record.getData();
                Map<String, Object> data = parseData(previousData);

                for (DqRemediationDto.ProposalItem item : entry.getValue()) {
                    data.put(item.getFieldKey(), item.getNewValue());
                }

                String newData = objectMapper.writeValueAsString(data);
                record.setData(newData);
                record.setVersion((record.getVersion() != null ? record.getVersion() : 1) + 1);
                record.setUpdatedAt(LocalDateTime.now());
                recordRepository.save(record);

                // History
                RecordHistory history = new RecordHistory();
                history.setRecordId(record.getId());
                history.setChangeType("REMEDIATION");
                history.setChangedBy("DQ_AUTO_REMEDIATOR");
                history.setPreviousData(previousData);
                history.setNewData(newData);
                history.setVersion(record.getVersion());
                history.setSourceSystem("DQ_ENGINE");
                recordHistoryRepository.save(history);

                success += entry.getValue().size();
            } catch (Exception e) {
                log.error("Failed to apply remediation to record {}: {}", recordId, e.getMessage());
                failed += entry.getValue().size();
            }
        }

        return DqRemediationDto.RemediationApplyResult.builder()
                .successCount(success)
                .failedCount(failed)
                .message(String.format("총 %d개 필드 보정 완료 (실패 %d개)", success, failed))
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
