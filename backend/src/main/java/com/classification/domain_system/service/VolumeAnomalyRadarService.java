package com.classification.domain_system.service;

import com.classification.domain_system.dto.VolumeAnomalyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VolumeAnomalyRadarService {

    public VolumeAnomalyDto.VolumeRadarResponse getVolumeRadarData() {
        List<VolumeAnomalyDto.VolumeDataPoint> points = new ArrayList<>();

        points.add(VolumeAnomalyDto.VolumeDataPoint.builder().timeBucket("13:50").createCount(120).updateCount(340).deleteCount(5).apiCallCount(1200).zScore(0.4).isSpike(false).build());
        points.add(VolumeAnomalyDto.VolumeDataPoint.builder().timeBucket("13:55").createCount(110).updateCount(310).deleteCount(2).apiCallCount(1150).zScore(0.2).isSpike(false).build());
        points.add(VolumeAnomalyDto.VolumeDataPoint.builder().timeBucket("14:00").createCount(130).updateCount(360).deleteCount(8).apiCallCount(1280).zScore(0.5).isSpike(false).build());
        points.add(VolumeAnomalyDto.VolumeDataPoint.builder().timeBucket("14:05").createCount(125).updateCount(330).deleteCount(4).apiCallCount(1210).zScore(0.3).isSpike(false).build());
        points.add(VolumeAnomalyDto.VolumeDataPoint.builder().timeBucket("14:10").createCount(140).updateCount(390).deleteCount(6).apiCallCount(1350).zScore(0.7).isSpike(false).build());
        points.add(VolumeAnomalyDto.VolumeDataPoint.builder().timeBucket("14:15").createCount(850).updateCount(1800).deleteCount(42).apiCallCount(4850).zScore(3.4).isSpike(true).build());

        boolean hasSpike = points.stream().anyMatch(VolumeAnomalyDto.VolumeDataPoint::isSpike);

        return VolumeAnomalyDto.VolumeRadarResponse.builder()
                .status(hasSpike ? "SPIKE_DETECTED" : "NORMAL")
                .currentThroughput(4850)
                .baselineThroughput(1250)
                .history(points)
                .recommendation(hasSpike
                        ? "⚠️ 14:15 시점에 기준치 대비 3.8배의 비정상 트래픽 급증(Spike)이 감지되었습니다. 연계 채널별 Rate Limiting(초당 호출 제한) 가동을 권장합니다."
                        : "✅ 모든 도메인 처리량 및 API 트래픽이 정상 기준치 범위 내에서 안정적으로 유지되고 있습니다.")
                .build();
    }
}
