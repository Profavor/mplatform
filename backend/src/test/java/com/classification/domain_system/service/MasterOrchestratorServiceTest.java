package com.classification.domain_system.service;

import com.classification.domain_system.dto.MasterOrchestratorDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class MasterOrchestratorServiceTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private com.classification.domain_system.repository.SystemFeatureRepository systemFeatureRepository;

    @InjectMocks
    private MasterOrchestratorService orchestratorService;

    @BeforeEach
    void setUp() throws SQLException {
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.isValid(anyInt())).willReturn(true);
        given(applicationContext.containsBean(anyString())).willReturn(true);
        
        java.util.List<com.classification.domain_system.entity.SystemFeature> mockFeatures = new java.util.ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            String category = (i <= 10) ? "DQ_QUALITY" : "AI_INNOVATION";
            mockFeatures.add(com.classification.domain_system.entity.SystemFeature.builder()
                    .featureNo(i)
                    .category(category)
                    .featureNameKey("test_feature_" + i)
                    .beanName("testService" + i)
                    .isGovernanceCore(i <= 11)
                    .build());
        }
        given(systemFeatureRepository.findAllByOrderByFeatureNoAsc()).willReturn(mockFeatures);
    }

    @Test
    @DisplayName("getOrchestratorStatus: DB 기반 50대 시스템 거버넌스 기능 상태 반환")
    void testGetOrchestratorStatus() {
        MasterOrchestratorDto.MasterOrchestratorSummary res = orchestratorService.getOrchestratorStatus();

        assertThat(res).isNotNull();
        assertThat(res.getTotalFeatures()).isEqualTo(50);
        assertThat(res.getHealthyFeatures()).isLessThanOrEqualTo(50);
        assertThat(res.getSystemMaturityLevel()).contains("Level");
        assertThat(res.getModules()).hasSize(50);
        assertThat(res.getCategoryDistribution()).containsKeys("DQ_QUALITY", "AI_INNOVATION");
    }
}
