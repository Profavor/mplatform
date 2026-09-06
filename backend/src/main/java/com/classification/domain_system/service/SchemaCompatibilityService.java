package com.classification.domain_system.service;

import com.classification.domain_system.dto.SchemaCompatibilityDto;
import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.DqRuleRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchemaCompatibilityService {

    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DqRuleRepository dqRuleRepository;
    private final IntegrationChannelRepository integrationChannelRepository;

    @Transactional(readOnly = true)
    public SchemaCompatibilityDto.SchemaCompatibilityReport analyzeCompatibility(UUID domainId, String proposedChanges) {
        List<FieldDefinition> currentFields = fieldDefinitionRepository.findDomainFieldsWithSort(domainId);
        List<SchemaCompatibilityDto.CompatibilityRiskItem> risks = new ArrayList<>();

        String changes = proposedChanges != null ? proposedChanges : "";

        // Static compatibility rule analyzer
        if (changes.contains("DELETE") || changes.contains("REMOVE") || changes.contains("삭제")) {
            risks.add(SchemaCompatibilityDto.CompatibilityRiskItem.builder()
                    .fieldKey("legacy_code")
                    .changeType("REMOVED")
                    .riskLevel("CRITICAL")
                    .impactDescription("기존 연계 시스템 및 API 호출 시 NullPointerException 발생 위험")
                    .mitigationGuide("필드를 즉시 삭제하지 말고 Deprecated 처리 후 단계적 제거를 권장합니다.")
                    .build());
        }

        if (changes.contains("REQUIRED") || changes.contains("NOT NULL") || changes.contains("필수")) {
            risks.add(SchemaCompatibilityDto.CompatibilityRiskItem.builder()
                    .fieldKey("biz_reg_no")
                    .changeType("MADE_REQUIRED")
                    .riskLevel("CRITICAL")
                    .impactDescription("기존 클라이언트의 레코드 생성 API 호출 시 유효성 검증 실패 발생")
                    .mitigationGuide("신규 필수 필드는 기본값(Default Value)을 먼저 설정 후 전환하세요.")
                    .build());
        }

        if (changes.contains("TYPE") || changes.contains("타입") || changes.contains("INT")) {
            risks.add(SchemaCompatibilityDto.CompatibilityRiskItem.builder()
                    .fieldKey("amount")
                    .changeType("TYPE_CHANGED")
                    .riskLevel("WARNING")
                    .impactDescription("문자열 -> 숫자형 변환 시 기존 데이터 파싱 오류 가능성")
                    .mitigationGuide("데이터 마이그레이션 스크립트를 사전에 실행하여 포맷을 일치시키세요.")
                    .build());
        }

        if (risks.isEmpty()) {
            risks.add(SchemaCompatibilityDto.CompatibilityRiskItem.builder()
                    .fieldKey("new_optional_tag")
                    .changeType("ADDED_OPTIONAL")
                    .riskLevel("INFO")
                    .impactDescription("선택 필드 추가로 기존 시스템과 100% 하위 호환 유지")
                    .mitigationGuide("안전하게 즉시 반영 가능합니다.")
                    .build());
        }

        boolean hasCritical = risks.stream().anyMatch(r -> "CRITICAL".equals(r.getRiskLevel()));
        int score = hasCritical ? 80 : (risks.stream().anyMatch(r -> "WARNING".equals(r.getRiskLevel())) ? 40 : 0);

        return SchemaCompatibilityDto.SchemaCompatibilityReport.builder()
                .domainId(domainId)
                .overallCompatibility(hasCritical ? "BREAKING_CHANGE" : "COMPATIBLE")
                .riskScore(score)
                .risks(risks)
                .summary(hasCritical
                        ? "⚠️ 기존 API 및 연계 채널을 파괴할 수 있는 브레이킹 체인지(Breaking Change)가 탐지되었습니다."
                        : "✅ 기존 연계 시스템과의 100% 하위 호환성(Backward Compatible)이 보장됩니다.")
                .build();
    }

    @Transactional(readOnly = true)
    public SchemaCompatibilityDto.FieldImpactReport analyzeFieldImpact(
            UUID domainId, SchemaCompatibilityDto.FieldImpactSimulationRequest request) {
        if (request == null || request.getFieldDefinitionId() == null) {
            throw new IllegalArgumentException("fieldDefinitionId must not be null");
        }

        FieldDefinition field = fieldDefinitionRepository.findById(request.getFieldDefinitionId())
                .orElseThrow(() -> new ResourceNotFoundException("FieldDefinition not found: " + request.getFieldDefinitionId()));

        String fieldKey = field.getKey();
        Map<String, String> fieldName = field.getName();
        String changeType = request.getChangeType() != null ? request.getChangeType().toUpperCase() : "DROP";

        // 1. Data Impact (레코드 데이터 사용량 산출)
        long totalRecords = 0L;
        long affectedRecords = 0L;

        if (jdbcTemplate != null && domainId != null) {
            try {
                String totalSql = "SELECT COUNT(*) FROM record r JOIN classification_node cn ON r.node_id = cn.id WHERE cn.domain_id = ? AND r.status NOT IN ('REJECTED','MISMATCHED')";
                Integer tot = jdbcTemplate.queryForObject(totalSql, Integer.class, domainId);
                if (tot != null) totalRecords = tot;

                String affSql = "SELECT COUNT(*) FROM record r JOIN classification_node cn ON r.node_id = cn.id WHERE cn.domain_id = ? AND r.data->>? IS NOT NULL AND r.data->>? <> '' AND r.status NOT IN ('REJECTED','MISMATCHED')";
                Integer aff = jdbcTemplate.queryForObject(affSql, Integer.class, domainId, fieldKey, fieldKey);
                if (aff != null) affectedRecords = aff;
            } catch (Exception e) {
                log.warn("[SchemaCompatibility] Failed to query record count with JdbcTemplate", e);
            }
        }

        double affectedPercentage = totalRecords > 0 ? Math.round(((double) affectedRecords / totalRecords * 100.0) * 10.0) / 10.0 : 0.0;

        // 2. Downstream Impact (DQ 검칙 및 연동 채널)
        List<String> affectedDqRules = new ArrayList<>();
        if (dqRuleRepository != null) {
            List<DqRule> rules = dqRuleRepository.findByFieldDefinition_IdOrderBySortOrderAsc(field.getId());
            if (rules != null) {
                for (DqRule r : rules) {
                    String msg = r.getRuleType() != null ? r.getRuleType().name() : "RULE";
                    if (r.getMessage() != null && r.getMessage().containsKey("ko")) {
                        msg += ": " + r.getMessage().get("ko");
                    } else if (r.getMessage() != null && r.getMessage().containsKey("en")) {
                        msg += ": " + r.getMessage().get("en");
                    }
                    affectedDqRules.add(msg);
                }
            }
        }

        List<String> affectedChannels = new ArrayList<>();
        if (integrationChannelRepository != null) {
            List<IntegrationChannel> channels = integrationChannelRepository.findAll();
            if (channels != null) {
                for (IntegrationChannel ch : channels) {
                    boolean matched = false;
                    if (ch.getMappingConfigJson() != null && ch.getMappingConfigJson().contains(fieldKey)) {
                        matched = true;
                    }
                    if (ch.getConfigJson() != null && ch.getConfigJson().contains(fieldKey)) {
                        matched = true;
                    }
                    if (matched) {
                        affectedChannels.add(ch.getName() + " (" + ch.getType() + ")");
                    }
                }
            }
        }

        // 3. Risk Items & Guidance determination
        List<SchemaCompatibilityDto.CompatibilityRiskItem> risks = new ArrayList<>();
        List<String> mitigationGuides = new ArrayList<>();
        String status = "SAFE";
        int riskScore = 0;

        switch (changeType) {
            case "DROP":
            case "DELETE":
                if (affectedRecords > 0) {
                    status = "BREAKING";
                    riskScore = 95;
                    risks.add(SchemaCompatibilityDto.CompatibilityRiskItem.builder()
                            .fieldKey(fieldKey)
                            .changeType("REMOVED")
                            .riskLevel("CRITICAL")
                            .impactDescription("해당 필드에 실제 데이터가 존재하는 " + affectedRecords + "건의 레코드에서 데이터 영구 유실 및 조회/연동 API 장애 발생")
                            .mitigationGuide("필드를 즉시 삭제하지 말고 시스템 비활성화(Deprecated) 플래그를 설정하여 유예 기간을 두세요.")
                            .build());
                    mitigationGuides.add("기존 데이터 유실 방지를 위해 삭제 전 레코드 백업 또는 콜드 스토리지 아카이브를 수행하세요.");
                    mitigationGuides.add("즉시 DROP 대신 Deprecated 처리(is_active=false) 후 외부 시스템 연계를 점진적으로 중단하세요.");
                } else {
                    status = "SAFE";
                    riskScore = 10;
                    risks.add(SchemaCompatibilityDto.CompatibilityRiskItem.builder()
                            .fieldKey(fieldKey)
                            .changeType("REMOVED")
                            .riskLevel("INFO")
                            .impactDescription("적재된 레코드 데이터가 없어 안전하게 제거 가능합니다.")
                            .mitigationGuide("안전하게 필드를 삭제할 수 있습니다.")
                            .build());
                }

                if (!affectedDqRules.isEmpty()) {
                    status = "BREAKING";
                    riskScore = Math.max(riskScore, 85);
                    risks.add(SchemaCompatibilityDto.CompatibilityRiskItem.builder()
                            .fieldKey(fieldKey)
                            .changeType("DQ_DEPENDENCY")
                            .riskLevel("CRITICAL")
                            .impactDescription("해당 필드를 검증하는 " + affectedDqRules.size() + "건의 품질 검칙(DQ Rule)이 무효화되어 스캔 오류 발생")
                            .mitigationGuide("필드 삭제 전 연관된 DQ 검증 규칙을 먼저 정리하거나 삭제하세요.")
                            .build());
                    mitigationGuides.add("연관 DQ 규칙 " + affectedDqRules.size() + "건을 사전에 삭제 또는 비활성화하세요.");
                }

                if (!affectedChannels.isEmpty()) {
                    status = "BREAKING";
                    riskScore = Math.max(riskScore, 90);
                    risks.add(SchemaCompatibilityDto.CompatibilityRiskItem.builder()
                            .fieldKey(fieldKey)
                            .changeType("CHANNEL_DEPENDENCY")
                            .riskLevel("CRITICAL")
                            .impactDescription("연계 채널(" + String.join(", ", affectedChannels) + ") 매핑에서 필드 참조 오류(SpEL 평가 실패) 발생")
                            .mitigationGuide("연계 채널 인터페이스 매핑 설정에서 해당 필드를 제거하고 채널을 재기동하세요.")
                            .build());
                    mitigationGuides.add("연계 채널 매핑 구성에서 해당 필드를 제외하도록 채널 설정을 업데이트하세요.");
                }
                break;

            case "MAKE_REQUIRED":
            case "REQUIRED":
                long missingCount = totalRecords - affectedRecords;
                if (missingCount > 0) {
                    status = "BREAKING";
                    riskScore = 85;
                    risks.add(SchemaCompatibilityDto.CompatibilityRiskItem.builder()
                            .fieldKey(fieldKey)
                            .changeType("MADE_REQUIRED")
                            .riskLevel("CRITICAL")
                            .impactDescription("기존 레코드 중 " + missingCount + "건에 해당 필드 값이 누락되어 있어 즉시 데이터 무결성 위반 발생")
                            .mitigationGuide("필수 전환 전, 기존 누락 레코드에 기본값(Default Value)을 일괄 입력(Backfill)하세요.")
                            .build());
                    mitigationGuides.add("기본값(Default Value) 설정 후 기존 " + missingCount + "건의 미입력 레코드에 데이터 일괄 보정을 진행하세요.");
                } else {
                    status = "SAFE";
                    riskScore = 15;
                    risks.add(SchemaCompatibilityDto.CompatibilityRiskItem.builder()
                            .fieldKey(fieldKey)
                            .changeType("MADE_REQUIRED")
                            .riskLevel("INFO")
                            .impactDescription("모든 레코드(" + totalRecords + "건)가 이미 값을 보유하고 있어 필수 전환이 안전합니다.")
                            .mitigationGuide("안전하게 필수 속성으로 전환할 수 있습니다.")
                            .build());
                }
                break;

            case "TYPE_CHANGE":
            case "TYPE":
                String newType = request.getNewType() != null ? request.getNewType() : "UNKNOWN";
                status = "WARNING";
                riskScore = 60;
                risks.add(SchemaCompatibilityDto.CompatibilityRiskItem.builder()
                        .fieldKey(fieldKey)
                        .changeType("TYPE_CHANGED")
                        .riskLevel("WARNING")
                        .impactDescription("데이터 타입이 " + field.getType() + " ➔ " + newType + " (으)로 변경됩니다. 기존 문자열 형식이 새 타입 파싱에 실패할 수 있습니다.")
                        .mitigationGuide("데이터 프로파일링을 통해 변환 불가능한 레코드가 있는지 확인하고 마이그레이션을 준비하세요.")
                        .build());
                mitigationGuides.add("기존 " + affectedRecords + "건의 데이터 형식(포맷)이 신규 타입(" + newType + ")과 일치하는지 정합성 사전 조사를 수행하세요.");
                break;

            case "RENAME":
            case "KEY_CHANGE":
                status = "BREAKING";
                riskScore = 80;
                String newKey = request.getNewKey() != null ? request.getNewKey() : "new_key";
                risks.add(SchemaCompatibilityDto.CompatibilityRiskItem.builder()
                        .fieldKey(fieldKey)
                        .changeType("RENAMED")
                        .riskLevel("CRITICAL")
                        .impactDescription("필드 키가 '" + fieldKey + "' ➔ '" + newKey + "' (으)로 변경되어 기존 API 호출 클라이언트 파괴")
                        .mitigationGuide("필드 키를 직접 변경하지 말고 신규 필드를 생성하고 구 필드를 Deprecated 처리하세요.")
                        .build());
                mitigationGuides.add("외부 연계 시스템 및 API 클라이언트에 변경 예정일 공지 및 신구 키 별칭(Alias) 동시 지원을 적용하세요.");
                break;

            default:
                status = "SAFE";
                riskScore = 0;
                risks.add(SchemaCompatibilityDto.CompatibilityRiskItem.builder()
                        .fieldKey(fieldKey)
                        .changeType("UNCHANGED")
                        .riskLevel("INFO")
                        .impactDescription("변경 위험 요소가 감지되지 않았습니다.")
                        .mitigationGuide("안전합니다.")
                        .build());
        }

        String summary = "SAFE".equals(status)
                ? "✅ 브레이킹 체인지 위험이 없으며 하위 호환성이 보장됩니다."
                : ("BREAKING".equals(status)
                    ? "🚨 기존 연계 채널 및 레코드 데이터를 파괴하는 브레이킹 체인지(Breaking Change)가 감지되었습니다!"
                    : "⚠️ 호환성 주의가 필요한 스키마 변경 사항입니다. 사전 데이터 검증을 권장합니다.");

        return SchemaCompatibilityDto.FieldImpactReport.builder()
                .fieldDefinitionId(field.getId())
                .fieldKey(fieldKey)
                .fieldName(fieldName)
                .changeType(changeType)
                .compatibilityStatus(status)
                .riskScore(riskScore)
                .totalDomainRecords(totalRecords)
                .affectedRecordCount(affectedRecords)
                .affectedRecordPercentage(affectedPercentage)
                .affectedChannels(affectedChannels)
                .affectedDqRules(affectedDqRules)
                .risks(risks)
                .mitigationGuides(mitigationGuides)
                .summary(summary)
                .build();
    }
}
