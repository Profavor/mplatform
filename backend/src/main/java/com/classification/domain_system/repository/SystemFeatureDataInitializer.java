package com.classification.domain_system.repository;

import com.classification.domain_system.entity.SystemFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SystemFeatureDataInitializer implements CommandLineRunner {

    private final SystemFeatureRepository systemFeatureRepository;

    @Override
    public void run(String... args) throws Exception {
        if (systemFeatureRepository.count() > 0) {
            return;
        }

        List<SystemFeature> features = new ArrayList<>();
        features.add(SystemFeature.builder().featureNo(1).category("SCHEMA_LIFECYCLE").featureNameKey("record_rollback").beanName("recordRollbackService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(2).category("DQ_QUALITY").featureNameKey("korean_dq_rule").beanName("koreanDqRuleService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(3).category("WORKFLOW_APPROVAL").featureNameKey("approval_delegation").beanName("approvalDelegationService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(4).category("SCHEMA_LIFECYCLE").featureNameKey("classification_node").beanName("classificationNodeService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(5).category("SCHEMA_LIFECYCLE").featureNameKey("domain_schema").beanName("domainSchemaService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(6).category("INTEGRATION_PIPELINE").featureNameKey("channel_metrics").beanName("channelMetricsService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(7).category("INTEGRATION_PIPELINE").featureNameKey("async_batch").beanName("asyncBatchService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(8).category("WORKFLOW_APPROVAL").featureNameKey("approval_escalation").beanName("approvalEscalationService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(9).category("DQ_QUALITY").featureNameKey("data_profiling").beanName("dataProfilingService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(10).category("INTEGRATION_PIPELINE").featureNameKey("smart_mapping").beanName("smartMappingService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(11).category("SECURITY_COMPLIANCE").featureNameKey("compliance_audit").beanName("complianceAuditService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(12).category("SCHEMA_LIFECYCLE").featureNameKey("data_lineage").beanName("dataLineageService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(13).category("DQ_QUALITY").featureNameKey("autonomous_cleansing").beanName("autonomousCleansingService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(14).category("SCHEMA_LIFECYCLE").featureNameKey("schema_impact_simulator").beanName("schemaImpactSimulatorService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(15).category("SCHEMA_LIFECYCLE").featureNameKey("excel_export").beanName("excelExportService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(16).category("INTEGRATION_PIPELINE").featureNameKey("notification").beanName("notificationService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(17).category("SCHEMA_LIFECYCLE").featureNameKey("record_version_diff").beanName("recordVersionDiffService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(18).category("AI_INNOVATION").featureNameKey("business_term").beanName("businessTermService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(19).category("WORKFLOW_APPROVAL").featureNameKey("approval_sandbox").beanName("approvalSandboxService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(20).category("DQ_QUALITY").featureNameKey("reference_integrity").beanName("referenceIntegrityService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(21).category("INTEGRATION_PIPELINE").featureNameKey("integration_dlq").beanName("integrationDlqService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(22).category("SECURITY_COMPLIANCE").featureNameKey("data_masking").beanName("dataMaskingService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(23).category("SCHEMA_LIFECYCLE").featureNameKey("domain_snapshot").beanName("domainSnapshotService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(24).category("WORKFLOW_APPROVAL").featureNameKey("dynamic_routing").beanName("dynamicRoutingService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(25).category("INTEGRATION_PIPELINE").featureNameKey("webhook_subscription").beanName("webhookSubscriptionService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(26).category("AI_INNOVATION").featureNameKey("multilingual").beanName("multilingualService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(27).category("DQ_QUALITY").featureNameKey("golden_record").beanName("goldenRecordService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(28).category("SECURITY_COMPLIANCE").featureNameKey("hash_chain_audit").beanName("hashChainAuditService").isGovernanceCore(false).iconName("").colorTheme("").build());
        
        // 11 Core Features
        features.add(SystemFeature.builder().featureNo(29).category("AI_INNOVATION").featureNameKey("governance_copilot").beanName("governanceCopilotService").isGovernanceCore(true).iconName("smart_toy").colorTheme("primary").build());
        features.add(SystemFeature.builder().featureNo(50).category("AI_INNOVATION").featureNameKey("master_orchestrator").beanName("masterOrchestratorService").isGovernanceCore(true).iconName("military_tech").colorTheme("warning").build());
        features.add(SystemFeature.builder().featureNo(1001).category("AI_INNOVATION").featureNameKey("pipeline_self_healing").beanName("pipelineSelfHealingService").isGovernanceCore(true).iconName("healing").colorTheme("danger-outline").build());
        features.add(SystemFeature.builder().featureNo(1002).category("INTEGRATION_PIPELINE").featureNameKey("freshness_heatmap").beanName("freshnessHeatmapService").isGovernanceCore(true).iconName("thermostat").colorTheme("success-outline").build());
        features.add(SystemFeature.builder().featureNo(1003).category("INTEGRATION_PIPELINE").featureNameKey("multi_region_conflict").beanName("multiRegionConflictService").isGovernanceCore(true).iconName("public").colorTheme("info-outline").build());
        
        features.add(SystemFeature.builder().featureNo(48).category("SCHEMA_LIFECYCLE").featureNameKey("multi_tenant").beanName("multiTenantRouterService").isGovernanceCore(true).iconName("domain").colorTheme("primary-outline").build());
        features.add(SystemFeature.builder().featureNo(49).category("INTEGRATION_PIPELINE").featureNameKey("data_sla").beanName("dataSlaContractService").isGovernanceCore(true).iconName("handshake").colorTheme("info-outline").build());
        features.add(SystemFeature.builder().featureNo(46).category("DQ_QUALITY").featureNameKey("governance_maturity").beanName("governanceMaturityService").isGovernanceCore(true).iconName("emoji_events").colorTheme("success-outline").build());
        features.add(SystemFeature.builder().featureNo(45).category("INTEGRATION_PIPELINE").featureNameKey("volume_radar").beanName("volumeAnomalyRadarService").isGovernanceCore(true).iconName("radar").colorTheme("danger-outline").build());
        features.add(SystemFeature.builder().featureNo(44).category("SECURITY_COMPLIANCE").featureNameKey("regulatory_compliance").beanName("regulatoryComplianceService").isGovernanceCore(true).iconName("policy").colorTheme("secondary-outline").build());
        features.add(SystemFeature.builder().featureNo(41).category("SCHEMA_LIFECYCLE").featureNameKey("cold_storage").beanName("coldStorageArchiveService").isGovernanceCore(true).iconName("ac_unit").colorTheme("info-outline").build());
        
        features.add(SystemFeature.builder().featureNo(30).category("AI_INNOVATION").featureNameKey("data_asset_valuation").beanName("dataAssetValuationService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(31).category("WORKFLOW_APPROVAL").featureNameKey("rejection_analytics").beanName("rejectionAnalyticsService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(32).category("SECURITY_COMPLIANCE").featureNameKey("data_retention").beanName("dataRetentionService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(33).category("SECURITY_COMPLIANCE").featureNameKey("anomaly_access").beanName("anomalyAccessService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(34).category("INTEGRATION_PIPELINE").featureNameKey("cross_domain_pipeline").beanName("crossDomainPipelineService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(35).category("SECURITY_COMPLIANCE").featureNameKey("api_key_management").beanName("apiKeyManagementService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(36).category("INTEGRATION_PIPELINE").featureNameKey("system_diagnostics").beanName("systemDiagnosticsService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(37).category("SCHEMA_LIFECYCLE").featureNameKey("schema_compatibility").beanName("schemaCompatibilityService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(38).category("DQ_QUALITY").featureNameKey("business_rule").beanName("businessRuleService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(39).category("INTEGRATION_PIPELINE").featureNameKey("cdc_stream").beanName("cdcStreamService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(40).category("WORKFLOW_APPROVAL").featureNameKey("workspace_widget").beanName("workspaceWidgetService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(42).category("AI_INNOVATION").featureNameKey("ai_unstructured_extractor").beanName("aiUnstructuredExtractorService").isGovernanceCore(false).iconName("").colorTheme("").build());
        features.add(SystemFeature.builder().featureNo(43).category("AI_INNOVATION").featureNameKey("semantic_ontology").beanName("semanticOntologyService").isGovernanceCore(false).iconName("").colorTheme("").build());

        systemFeatureRepository.saveAll(features);
    }
}
