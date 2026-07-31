package com.classification.domain_system.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuDataInitializer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initMenus() {
        try {
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM menu", Long.class);
            if (count != null && count > 0) {
                log.info("Menu data already exists ({} rows). Skipping initialization.", count);
                return;
            }

            log.info("No menu data found. Initializing system menu tree via SQL...");

            // ── Top-level menus ──────────────────────────────────────────
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"홈\",\"en\":\"Home\"}', '/', 'home', NULL, 1, TRUE)");
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"도메인 & 스키마 관리\",\"en\":\"Domain & Schema\"}', '/schema', 'schema', NULL, 2, TRUE)");
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"마스터 데이터 관리\",\"en\":\"Master Data\"}', '/records', 'table_chart', NULL, 3, TRUE)");
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"데이터 품질 관리\",\"en\":\"Data Quality\"}', '/dq-dashboard', 'insights', NULL, 4, TRUE)");
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"결재 & 승인 관리\",\"en\":\"Approvals & Governance\"}', '/approvals', 'fact_check', NULL, 5, TRUE)");
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"데이터 매칭 후보\",\"en\":\"Match Candidates\"}', '/match-candidates', 'find_replace', NULL, 6, TRUE)");
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"시스템 관리\",\"en\":\"System Admin\"}', '/admin', 'admin_panel_settings', NULL, 7, TRUE)");

            // ── Lookup admin parent ID ───────────────────────────────────
            Long adminId = jdbcTemplate.queryForObject("SELECT id FROM menu WHERE path = '/admin'", Long.class);

            // ── Admin sub-menus ──────────────────────────────────────────
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"조직 및 부서 관리\",\"en\":\"Organizations\"}', '/admin/organizations', 'corporate_fare', ?, 1, TRUE)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"사용자 및 권한 관리\",\"en\":\"Users & Roles\"}', '/admin/users', 'group', ?, 2, TRUE)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"메뉴 관리\",\"en\":\"Menu Management\"}', '/admin/menus', 'menu_book', ?, 3, TRUE)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"시스템 로그\",\"en\":\"System Audit Logs\"}', '/admin/system-logs', 'receipt_long', ?, 4, TRUE)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"매칭 검토\",\"en\":\"Match Review\"}', '/admin/match-review', 'fact_check', ?, 5, TRUE)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"워크플로우 관리\",\"en\":\"Workflow Config\"}', '/admin/workflow', 'account_tree', ?, 6, TRUE)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"외부 연동 관리\",\"en\":\"Integration Channels\"}', '/admin/integration/channels', 'hub', ?, 7, TRUE)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active) VALUES ('{\"ko\":\"매칭 규칙 관리\",\"en\":\"Matching Rules\"}', '/admin/matching-rules', 'rule', ?, 8, TRUE)", adminId);

            // ── Role mappings (menu_roles) ───────────────────────────────
            // path 기반으로 menu_id를 조회하여 역할을 삽입
            Map<String, Set<String>> roleMap = Map.ofEntries(
                Map.entry("/",                          Set.of("ROLE_USER", "ROLE_ADMIN", "DATA_STEWARD", "DOMAIN_EDITOR", "DQ_MANAGER", "ORG_ADMIN", "VIEWER")),
                Map.entry("/schema",                    Set.of("ROLE_ADMIN", "DATA_STEWARD", "DOMAIN_EDITOR")),
                Map.entry("/records",                   Set.of("ROLE_USER", "ROLE_ADMIN", "DATA_STEWARD")),
                Map.entry("/dq-dashboard",              Set.of("ROLE_ADMIN", "DQ_MANAGER")),
                Map.entry("/approvals",                 Set.of("ROLE_USER", "ROLE_ADMIN", "DATA_STEWARD", "DOMAIN_EDITOR", "DQ_MANAGER", "ORG_ADMIN", "VIEWER")),
                Map.entry("/match-candidates",          Set.of("DATA_STEWARD")),
                Map.entry("/admin",                     Set.of("ROLE_ADMIN")),
                Map.entry("/admin/organizations",       Set.of("ROLE_ADMIN", "ORG_ADMIN")),
                Map.entry("/admin/users",               Set.of("ROLE_ADMIN", "ORG_ADMIN")),
                Map.entry("/admin/menus",               Set.of("ROLE_ADMIN")),
                Map.entry("/admin/system-logs",         Set.of("ROLE_ADMIN")),
                Map.entry("/admin/match-review",        Set.of("ROLE_ADMIN", "DATA_STEWARD")),
                Map.entry("/admin/workflow",            Set.of("ROLE_ADMIN", "ORG_ADMIN")),
                Map.entry("/admin/integration/channels", Set.of("ROLE_ADMIN")),
                Map.entry("/admin/matching-rules",      Set.of("DATA_STEWARD"))
            );

            List<Map<String, Object>> menus = jdbcTemplate.queryForList("SELECT id, path FROM menu");
            for (Map<String, Object> menu : menus) {
                Long menuId = ((Number) menu.get("id")).longValue();
                String path = (String) menu.get("path");
                Set<String> roles = roleMap.get(path);
                if (roles != null) {
                    for (String role : roles) {
                        jdbcTemplate.update("INSERT INTO menu_roles (menu_id, role_name) VALUES (?, ?)", menuId, role);
                    }
                }
            }

            log.info("System menu tree initialization completed successfully ({} menus created).", menus.size());
        } catch (Exception e) {
            log.error("Failed to initialize menu data", e);
        }
    }
}
