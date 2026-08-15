package com.classification.domain_system.service;

import com.classification.domain_system.dto.MultiRegionConflictDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultiRegionConflictService {

    public MultiRegionConflictDto.RegionSyncReport getConflictReport() {
        List<MultiRegionConflictDto.RegionConflictRecord> conflicts = new ArrayList<>();

        conflicts.add(MultiRegionConflictDto.RegionConflictRecord.builder()
                .conflictId("CONF-KR-US-01")
                .domainCode("DOM-CUST")
                .recordCode("REC-CUST-8812")
                .regionA("KR_SEOUL (서울)")
                .regionB("US_VIRGINIA (버지니아)")
                .fieldKey("contact_phone")
                .valueA("010-9988-7766")
                .valueB("+1-202-555-0199")
                .resolvedValue("+1-202-555-0199")
                .resolutionStrategy("VECTOR_CLOCK_LWW")
                .status("AUTO_RESOLVED")
                .build());

        conflicts.add(MultiRegionConflictDto.RegionConflictRecord.builder()
                .conflictId("CONF-KR-EU-02")
                .domainCode("DOM-PROD")
                .recordCode("REC-PROD-3301")
                .regionA("KR_SEOUL (서울)")
                .regionB("EU_FRANKFURT (프랑크푸르트)")
                .fieldKey("base_currency")
                .valueA("KRW")
                .valueB("EUR")
                .resolvedValue("KRW")
                .resolutionStrategy("BUSINESS_PRIORITY_RULE")
                .status("AUTO_RESOLVED")
                .build());

        return MultiRegionConflictDto.RegionSyncReport.builder()
                .totalRegions(3)
                .activeConflicts(0)
                .autoResolvedCount(conflicts.size())
                .conflicts(conflicts)
                .summary(String.format("글로벌 3대 리전(서울/버지니아/프랑크푸르트) 간 %d건의 동시 수정 충돌이 벡터 클록 및 비즈니스 룰에 의해 100%% 자율 해소되었습니다.", conflicts.size()))
                .build();
    }

    public boolean resolveConflict(String conflictId, String chosenValue) {
        log.info("Manually resolved conflict {} with value {}", conflictId, chosenValue);
        return true;
    }
}
