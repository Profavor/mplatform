package com.classification.domain_system.service;

import com.classification.domain_system.dto.VolumeAnomalyDto;
import com.classification.domain_system.entity.IntegrationLog;
import com.classification.domain_system.repository.IntegrationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VolumeAnomalyRadarService {

    private final IntegrationLogRepository integrationLogRepository;

    @Transactional(readOnly = true)
    public VolumeAnomalyDto.VolumeRadarResponse getVolumeRadarData() {
        List<VolumeAnomalyDto.VolumeDataPoint> points = new ArrayList<>();
        List<IntegrationLog> allLogs = integrationLogRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        for (int i = 5; i >= 0; i--) {
            LocalDateTime bucketStart = now.minusMinutes((i + 1) * 5L);
            LocalDateTime bucketEnd = now.minusMinutes(i * 5L);
            String label = bucketEnd.format(timeFmt);

            long logsInBucket = allLogs.stream()
                    .filter(l -> l.getCreatedAt() != null && !l.getCreatedAt().isBefore(bucketStart) && l.getCreatedAt().isBefore(bucketEnd))
                    .count();

            int creates = (int) (logsInBucket * 20);
            int updates = (int) (logsInBucket * 50);
            int deletes = (int) (logsInBucket * 2);
            int totalCalls = Math.max(100, (int) (logsInBucket * 150 + 100));

            double zScore = logsInBucket > 20 ? 3.2 : (logsInBucket > 10 ? 1.5 : 0.4);
            boolean isSpike = zScore >= 3.0;

            points.add(VolumeAnomalyDto.VolumeDataPoint.builder()
                    .timeBucket(label)
                    .createCount(creates)
                    .updateCount(updates)
                    .deleteCount(deletes)
                    .apiCallCount(totalCalls)
                    .zScore(zScore)
                    .isSpike(isSpike)
                    .build());
        }

        boolean hasSpike = points.stream().anyMatch(VolumeAnomalyDto.VolumeDataPoint::isSpike);
        int currentThroughput = points.isEmpty() ? 0 : points.get(points.size() - 1).getApiCallCount();
        int baselineThroughput = (int) points.stream().mapToInt(VolumeAnomalyDto.VolumeDataPoint::getApiCallCount).average().orElse(100.0);

        return VolumeAnomalyDto.VolumeRadarResponse.builder()
                .status(hasSpike ? "SPIKE_DETECTED" : "NORMAL")
                .currentThroughput(currentThroughput)
                .baselineThroughput(baselineThroughput)
                .history(points)
                .recommendation(hasSpike
                        ? "⚠️ 비정상 트래픽 급증(Spike)이 감지되었습니다. 연계 채널별 Rate Limiting(초당 호출 제한) 가동을 권장합니다."
                        : "✅ 모든 도메인 처리량 및 API 트래픽이 정상 기준치 범위 내에서 안정적으로 유지되고 있습니다.")
                .build();
    }
}
