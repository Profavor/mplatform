package com.classification.domain_system.service;

import com.classification.domain_system.entity.PermissionGroup;
import com.classification.domain_system.entity.PermissionItem;
import com.classification.domain_system.repository.PermissionGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionMasterInitializer implements CommandLineRunner {

    private final PermissionGroupRepository groupRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking and initializing default Permission Master groups and items...");

        // 1. Admin System Group (전역 시스템 관리)
        createOrUpdateGroup("admin", "admin", "전역 시스템 관리 권한", "System Admin Permissions", "🛡️", "#ef4444", "red", 1, List.of(
                new ItemData("전역 모든 권한 (*)", "System Super All (*)", "*", 1),
                new ItemData("시스템 관리자 조회 (read)", "System Admin Read", "admin:read", 2),
                new ItemData("시스템 관리자 관리 (write)", "System Admin Write", "admin:write", 3)
        ));

        // 2. Domain Group (도메인 스키마)
        createOrUpdateGroup("domain", "domain", "도메인 스키마 권한", "Domain Schema Permissions", "🌐", "#3b82f6", "blue", 2, List.of(
                new ItemData("도메인 전체 (*)", "Domain All (*)", "domain:*", 1),
                new ItemData("도메인 조회 (read)", "Domain Read", "domain:read", 2),
                new ItemData("도메인 생성/수정 (write)", "Domain Write", "domain:write", 3)
        ));

        // 3. Node Group (분류체계 노드)
        createOrUpdateGroup("node", "node", "분류 노드 권한", "Category Node Permissions", "📁", "#10b981", "green", 3, List.of(
                new ItemData("노드 전체 (*)", "Node All (*)", "node:*", 1),
                new ItemData("노드 조회 (read)", "Node Read", "node:read", 2),
                new ItemData("노드 생성/수정 (write)", "Node Write", "node:write", 3)
        ));

        // 4. Field Group (속성 필드)
        createOrUpdateGroup("field", "field", "속성 필드 권한", "Field Permissions", "📌", "#f59e0b", "amber", 4, List.of(
                new ItemData("필드 전체 (*)", "Field All (*)", "field:*", 1),
                new ItemData("필드 조회 (read)", "Field Read", "field:read", 2),
                new ItemData("필드 생성/수정 (write)", "Field Write", "field:write", 3)
        ));

        // 5. Record Group (마스터 데이터 레코드)
        createOrUpdateGroup("record", "record", "마스터 데이터 레코드 권한", "Master Record Permissions", "📄", "#8b5cf6", "purple", 5, List.of(
                new ItemData("레코드 전체 (*)", "Record All (*)", "record:*", 1),
                new ItemData("레코드 조회 (read)", "Record Read", "record:read", 2),
                new ItemData("레코드 등록/수정 (write)", "Record Write", "record:write", 3),
                new ItemData("레코드 엑셀/내보내기 (export)", "Record Export", "record:export", 4)
        ));

        // 6. DQ Group (데이터 품질 관리)
        createOrUpdateGroup("dq", "dq", "데이터 품질 관리 권한 (DQ)", "DQ Permissions", "⚡", "#ec4899", "pink", 6, List.of(
                new ItemData("품질 관리 전체 (*)", "DQ All (*)", "dq:*", 1),
                new ItemData("품질 검사/규칙 조회 (read)", "DQ Read", "dq:read", 2),
                new ItemData("품질 규칙 생성/수정 (write)", "DQ Write", "dq:write", 3),
                new ItemData("품질 규칙 관리 (dq_rule:*)", "DQ Rule All", "dq_rule:*", 4),
                new ItemData("품질 정기 검사 관리 (dq_scan:*)", "DQ Scan All", "dq_scan:*", 5)
        ));

        // 7. Org Group (조직 관리)
        createOrUpdateGroup("org", "org", "조직 및 부서 권한 (ORG)", "Organization Permissions", "🏢", "#06b6d4", "cyan", 7, List.of(
                new ItemData("조직 권한 전체 (*)", "Org All (*)", "org:*", 1),
                new ItemData("조직/부서/팀 조회 (read)", "Org Read", "org:read", 2),
                new ItemData("조직/부서/팀 생성/수정 (write)", "Org Write", "org:write", 3)
        ));

        // 8. User Group (사용자 관리)
        createOrUpdateGroup("user", "user", "사용자 관리 권한", "User Management Permissions", "👤", "#6366f1", "indigo", 8, List.of(
                new ItemData("사용자 전체 (*)", "User All (*)", "user:*", 1),
                new ItemData("사용자 목록/정보 조회 (read)", "User Read", "user:read", 2),
                new ItemData("사용자 생성/수정 (write)", "User Write", "user:write", 3)
        ));

        // 9. Role Group (역할 및 권한 관리)
        createOrUpdateGroup("role", "role", "역할 및 세부 권한 관리", "Role & Permission Management", "🔑", "#14b8a6", "teal", 9, List.of(
                new ItemData("역할 전체 (*)", "Role All (*)", "role:*", 1),
                new ItemData("역할/권한 조회 (read)", "Role Read", "role:read", 2),
                new ItemData("역할/권한 설정 (write)", "Role Write", "role:write", 3)
        ));

        // 10. Workflow Group (워크플로우 & 결재 관리)
        createOrUpdateGroup("workflow", "workflow", "워크플로우 및 결재 권한", "Workflow & Approval Permissions", "🌿", "#84cc16", "lime", 10, List.of(
                new ItemData("워크플로우 전체 (*)", "Workflow All (*)", "workflow:*", 1),
                new ItemData("워크플로우/결재선 조회 (read)", "Workflow Read", "workflow:read", 2),
                new ItemData("결재선 설정/수정 (write)", "Workflow Write", "workflow:write", 3),
                new ItemData("결재 신청 권한 (request)", "Workflow Request", "workflow:request", 4),
                new ItemData("결재 승인/반려 (approve)", "Workflow Approve", "workflow:approve", 5)
        ));

        // 11. Log Group (시스템 로그 & 모니터링)
        createOrUpdateGroup("log", "log", "시스템 로그 및 감사 권한", "System Audit Log Permissions", "📜", "#64748b", "slate", 11, List.of(
                new ItemData("로그 전체 (*)", "Log All (*)", "log:*", 1),
                new ItemData("감사 로그 조회 (read)", "Log Read", "log:read", 2),
                new ItemData("시스템 접근/변경 로그 (system_log:read)", "System Log Read", "system_log:read", 3)
        ));

        // 12. Match Group (데이터 매칭 검토)
        createOrUpdateGroup("match", "match", "데이터 매칭 검토 권한", "Data Match Review Permissions", "🔍", "#d97706", "amber", 12, List.of(
                new ItemData("매칭 전체 (*)", "Match All (*)", "match:*", 1),
                new ItemData("매칭 후보 조회 (read)", "Match Read", "match:read", 2),
                new ItemData("매칭 승인/병합 (write)", "Match Write", "match:write", 3)
        ));

        // 13. Integration Group (외부 연동 관리)
        createOrUpdateGroup("integration", "integration", "외부 연동 채널 권한", "Integration Channel Permissions", "🔗", "#0284c7", "sky", 13, List.of(
                new ItemData("외부 연동 전체 (*)", "Integration All (*)", "integration:*", 1),
                new ItemData("연동 채널 조회 (read)", "Integration Read", "integration:read", 2),
                new ItemData("연동 채널/매핑 설정 (write)", "Integration Write", "integration:write", 3)
        ));

        log.info("Default Permission Master groups checking/seeding completed successfully.");
    }

    private void createOrUpdateGroup(String id, String code, String titleKo, String titleEn, String icon, String color, String chipClass, int sortOrder, List<ItemData> items) {
        Optional<PermissionGroup> groupOpt = groupRepository.findById(id);
        PermissionGroup group;
        if (groupOpt.isPresent()) {
            group = groupOpt.get();
        } else {
            group = new PermissionGroup();
            group.setId(id);
        }
        group.setCode(code);
        group.setTitleKo(titleKo);
        group.setTitleEn(titleEn);
        group.setIcon(icon);
        group.setColor(color);
        group.setChipClass(chipClass);
        group.setSortOrder(sortOrder);

        for (ItemData itemData : items) {
            boolean exists = group.getItems().stream()
                    .anyMatch(i -> i.getPermValue().equalsIgnoreCase(itemData.permValue));
            if (!exists) {
                PermissionItem item = new PermissionItem();
                item.setLabelKo(itemData.labelKo);
                item.setLabelEn(itemData.labelEn);
                item.setPermValue(itemData.permValue);
                item.setSortOrder(itemData.sortOrder);
                group.addItem(item);
            }
        }
        groupRepository.save(group);
    }

    private static class ItemData {
        String labelKo;
        String labelEn;
        String permValue;
        int sortOrder;

        ItemData(String labelKo, String labelEn, String permValue, int sortOrder) {
            this.labelKo = labelKo;
            this.labelEn = labelEn;
            this.permValue = permValue;
            this.sortOrder = sortOrder;
        }
    }
}
