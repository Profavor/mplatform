package com.classification.domain_system.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuDataMigrationInitializer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateMenuNamesToI18nJson() {
        try {
            log.info("Starting safety DB data migration: Updating legacy menu names to i18n JSON format...");

            updateMenuName("/", "{\"ko\":\"홈\",\"en\":\"Home\"}");
            updateMenuName("/schema", "{\"ko\":\"도메인 & 스키마 관리\",\"en\":\"Domain & Schema\"}");
            updateMenuName("/records", "{\"ko\":\"마스터 데이터 관리\",\"en\":\"Master Data\"}");
            updateMenuName("/dq-dashboard", "{\"ko\":\"데이터 품질 관리\",\"en\":\"Data Quality\"}");
            updateMenuName("/approvals", "{\"ko\":\"결재 & 승인 관리\",\"en\":\"Approvals & Governance\"}");
            updateMenuName("/match-candidates", "{\"ko\":\"데이터 매칭 후보\",\"en\":\"Match Candidates\"}");
            updateMenuName("/admin", "{\"ko\":\"시스템 관리\",\"en\":\"System Admin\"}");
            updateMenuName("/admin/organizations", "{\"ko\":\"조직 및 부서 관리\",\"en\":\"Organizations\"}");
            updateMenuName("/admin/users", "{\"ko\":\"사용자 및 권한 관리\",\"en\":\"Users & Roles\"}");
            updateMenuName("/admin/menus", "{\"ko\":\"메뉴 관리\",\"en\":\"Menu Management\"}");
            updateMenuName("/admin/system-logs", "{\"ko\":\"시스템 로그\",\"en\":\"System Audit Logs\"}");
            updateMenuName("/admin/match-review", "{\"ko\":\"매칭 검토\",\"en\":\"Match Review\"}");
            updateMenuName("/admin/workflow", "{\"ko\":\"워크플로우 관리\",\"en\":\"Workflow Config\"}");
            updateMenuName("/admin/integration/channels", "{\"ko\":\"외부 연동 관리\",\"en\":\"Integration Channels\"}");
            updateMenuName("/admin/matching-rules", "{\"ko\":\"매칭 규칙 관리\",\"en\":\"Matching Rules\"}");

            log.info("Completed safety DB data migration for menu i18n JSON names.");
        } catch (Exception e) {
            log.error("Failed to migrate menu names to i18n JSON format", e);
        }
    }

    private void updateMenuName(String path, String jsonName) {
        jdbcTemplate.update("UPDATE menu SET name = ? WHERE path = ?", jsonName, path);
    }
}
