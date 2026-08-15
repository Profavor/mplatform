package com.classification.domain_system.service;

import com.classification.domain_system.dto.AnomalyAccessDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyAccessDetectionService {

    private final Set<String> blockedUserIds = ConcurrentHashMap.newKeySet();

    public AnomalyAccessDto.AnomalyDetectionSummaryResponse detectSecurityEvents() {
        List<AnomalyAccessDto.AnomalySecurityEvent> events = new ArrayList<>();

        events.add(AnomalyAccessDto.AnomalySecurityEvent.builder()
                .eventId("SEC-001")
                .userId("guest_user_99")
                .username("외주 협력사 계정")
                .sourceIp("198.51.100.45 (해외 미인가 IP)")
                .actionType("대량 PII 데이터 연속 조회")
                .threatLevel("CRITICAL")
                .details("1분 내 500건의 주민등록번호 복호화 조회 시도 감지")
                .timestamp(LocalDateTime.now().minusMinutes(5))
                .blocked(blockedUserIds.contains("guest_user_99"))
                .build());

        events.add(AnomalyAccessDto.AnomalySecurityEvent.builder()
                .eventId("SEC-002")
                .userId("temp_crawler")
                .username("비정상 자동화 봇")
                .sourceIp("203.0.113.12")
                .actionType("비정상 API 대량 호출")
                .threatLevel("HIGH")
                .details("초당 50회 이상의 레코드 검색 요청 발생")
                .timestamp(LocalDateTime.now().minusMinutes(12))
                .blocked(blockedUserIds.contains("temp_crawler"))
                .build());

        long activeCount = events.stream().filter(e -> !e.isBlocked()).count();

        return AnomalyAccessDto.AnomalyDetectionSummaryResponse.builder()
                .threatLevelScore(activeCount > 0 ? 85 : 10)
                .activeThreatCount((int) activeCount)
                .events(events)
                .summary(String.format("제로트러스트 보안 엔진이 %d건의 이상 접근 위협을 실시간 감지하였습니다.", activeCount))
                .build();
    }

    public boolean blockSuspiciousActor(String userId) {
        if (userId != null && !userId.isBlank()) {
            blockedUserIds.add(userId);
            log.warn("Blocked suspicious actor: {}", userId);
            return true;
        }
        return false;
    }
}
