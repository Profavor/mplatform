package com.classification.domain_system.service;

import com.classification.domain_system.dto.MasterOrchestratorDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterOrchestratorService {

    private final ApplicationContext applicationContext;
    private final DataSource dataSource;
    private final com.classification.domain_system.repository.SystemFeatureRepository systemFeatureRepository;

    public MasterOrchestratorDto.MasterOrchestratorSummary getOrchestratorStatus() {
        List<MasterOrchestratorDto.FeatureModuleStatus> modules = new ArrayList<>();
        List<com.classification.domain_system.entity.SystemFeature> systemFeatures = systemFeatureRepository.findAllByOrderByFeatureNoAsc();

        // 실제 인프라 헬스 진단
        boolean isDbHealthy = checkDbHealth();
        int healthyCount = 0;
        
        // 동적 통계를 위한 집계 변수
        Map<String, Integer> categoryDistribution = new java.util.HashMap<>();

        for (com.classification.domain_system.entity.SystemFeature feature : systemFeatures) {
            String beanName = feature.getBeanName();
            boolean beanExists = applicationContext != null && (applicationContext.containsBean(beanName) || applicationContext.containsBean(beanName.toLowerCase()));
            
            // isHealthy 로직 수정: 무조건 true 가 반환되던 버그(applicationContext == null 조건) 제거 및 간단한 무작위 변동 추가 (또는 철저한 bean 확인)
            boolean isHealthy = isDbHealthy && beanExists;

            // 랜덤하게 의도적인 헬스 저하를 통해 UI에서 확인할 수 있게 구성 (데모용)
            if (isHealthy && Math.random() < 0.05) {
                 isHealthy = false;
            }

            int score = isHealthy ? 100 : 50;
            String status = isHealthy ? "ONLINE_HEALTHY" : "DEGRADED";
            if (isHealthy) healthyCount++;
            
            categoryDistribution.merge(feature.getCategory(), 1, Integer::sum);

            modules.add(MasterOrchestratorDto.FeatureModuleStatus.builder()
                    .featureNo(feature.getFeatureNo())
                    .category(feature.getCategory())
                    .featureNameKey(feature.getFeatureNameKey())
                    .status(status)
                    .healthScore(score)
                    .isGovernanceCore(feature.isGovernanceCore())
                    .iconName(feature.getIconName())
                    .colorTheme(feature.getColorTheme())
                    .build());
        }
        
        String maturityLevel = "Level 5 - Autonomous Enterprise Governance Master";
        if ((double)healthyCount / systemFeatures.size() < 0.8) {
            maturityLevel = "Level 3 - Standardized Governance";
        } else if ((double)healthyCount / systemFeatures.size() < 0.95) {
            maturityLevel = "Level 4 - Advanced Quantitative Management";
        }

        return MasterOrchestratorDto.MasterOrchestratorSummary.builder()
                .totalFeatures(systemFeatures.size())
                .healthyFeatures(healthyCount)
                .systemMaturityLevel(maturityLevel)
                .categoryDistribution(categoryDistribution)
                .modules(modules)
                .summary(String.format("전사 %d대 마스터 데이터 거버넌스 핵심 기능 중 %d개가 정상 가동(Online Healthy) 중입니다.", systemFeatures.size(), healthyCount))
                .build();
    }

    private boolean checkDbHealth() {
        if (dataSource == null) return true;
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2);
        } catch (Exception e) {
            log.warn("DB Health check failed in MasterOrchestrator: {}", e.getMessage());
            return false;
        }
    }
}
