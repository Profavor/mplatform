package com.classification.domain_system.service;

import com.classification.domain_system.dto.MultilingualSyncDto;
import com.classification.domain_system.entity.BusinessTerm;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.repository.BusinessTermRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultilingualSyncService {

    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final BusinessTermRepository businessTermRepository;

    @Transactional(readOnly = true)
    public MultilingualSyncDto.SyncPlanResponse scanMissingLocales(UUID domainId) {
        List<FieldDefinition> fields = fieldDefinitionRepository.findDomainFieldsWithSort(domainId);
        List<BusinessTerm> terms = businessTermRepository.findAll();

        Map<String, BusinessTerm> termCodeMap = terms.stream()
                .filter(t -> t.getTermCode() != null)
                .collect(Collectors.toMap(t -> t.getTermCode().toLowerCase(), t -> t, (a, b) -> a));

        List<MultilingualSyncDto.MissingLocaleItem> missingItems = new ArrayList<>();

        for (FieldDefinition f : fields) {
            Map<String, String> names = f.getName() != null ? f.getName() : Collections.emptyMap();
            List<String> missing = new ArrayList<>();

            if (!names.containsKey("ko") || names.get("ko") == null || names.get("ko").isBlank()) {
                missing.add("ko");
            }
            if (!names.containsKey("en") || names.get("en") == null || names.get("en").isBlank()) {
                missing.add("en");
            }

            if (!missing.isEmpty()) {
                String keyLower = f.getKey().toLowerCase();
                BusinessTerm matchedTerm = termCodeMap.get(keyLower);
                String suggested = matchedTerm != null && matchedTerm.getTermName() != null
                        ? matchedTerm.getTermName().getOrDefault("en", matchedTerm.getTermName().getOrDefault("ko", f.getKey()))
                        : f.getKey();

                missingItems.add(MultilingualSyncDto.MissingLocaleItem.builder()
                        .fieldId(f.getId())
                        .fieldKey(f.getKey())
                        .currentNameMap(names)
                        .missingLanguages(missing)
                        .suggestedTermName(suggested)
                        .build());
            }
        }

        return MultilingualSyncDto.SyncPlanResponse.builder()
                .domainId(domainId)
                .totalFields(fields.size())
                .missingCount(missingItems.size())
                .missingItems(missingItems)
                .build();
    }

    @Transactional
    public MultilingualSyncDto.ApplySyncResult syncFromGlossary(UUID domainId) {
        List<FieldDefinition> fields = fieldDefinitionRepository.findDomainFieldsWithSort(domainId);
        List<BusinessTerm> terms = businessTermRepository.findAll();

        Map<String, BusinessTerm> termCodeMap = terms.stream()
                .filter(t -> t.getTermCode() != null)
                .collect(Collectors.toMap(t -> t.getTermCode().toLowerCase(), t -> t, (a, b) -> a));

        int synced = 0;
        for (FieldDefinition f : fields) {
            Map<String, String> names = f.getName() != null ? new HashMap<>(f.getName()) : new HashMap<>();
            boolean updated = false;

            String keyLower = f.getKey().toLowerCase();
            BusinessTerm term = termCodeMap.get(keyLower);

            if (!names.containsKey("ko") || names.get("ko") == null || names.get("ko").isBlank()) {
                String koVal = term != null && term.getTermName() != null && term.getTermName().containsKey("ko")
                        ? term.getTermName().get("ko")
                        : f.getKey();
                names.put("ko", koVal);
                updated = true;
            }

            if (!names.containsKey("en") || names.get("en") == null || names.get("en").isBlank()) {
                String enVal = term != null && term.getTermName() != null && term.getTermName().containsKey("en")
                        ? term.getTermName().get("en")
                        : f.getKey();
                names.put("en", enVal);
                updated = true;
            }

            if (updated) {
                f.setName(names);
                fieldDefinitionRepository.save(f);
                synced++;
            }
        }

        return MultilingualSyncDto.ApplySyncResult.builder()
                .domainId(domainId)
                .syncedCount(synced)
                .message(String.format("총 %d개 필드의 누락 다국어 메타데이터가 표준 용어사전과 동기화되었습니다.", synced))
                .build();
    }
}
