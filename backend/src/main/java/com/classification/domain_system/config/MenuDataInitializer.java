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
                log.info("Menu data already exists ({} rows). Checking missing sub-menus...", count);
                ensureMissingSubMenus();
                return;
            }

            log.info("No menu data found. Initializing system menu tree via SQL...");

            // ── Top-level menus ──────────────────────────────────────────
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"홈\",\"en\":\"Home\"}', '/', 'home', NULL, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"도메인 스키마 관리\",\"en\":\"Domain Schema Management\"}', '/schema', 'schema', NULL, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"마스터 데이터 레코드 관리\",\"en\":\"Master Data Records\"}', '/records', 'table_chart', NULL, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"데이터 품질 진단 대시보드\",\"en\":\"Data Quality Dashboard\"}', '/dq-dashboard', 'insights', NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"결재 및 승인 관리\",\"en\":\"Approvals & Governance\"}', '/approvals', 'fact_check', NULL, 5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"중복 후보 검토 큐\",\"en\":\"Match Candidates Queue\"}', '/match-candidates', 'find_replace', NULL, 6, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"시스템 관리\",\"en\":\"System Admin\"}', '/admin', 'admin_panel_settings', NULL, 7, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");

            // ── Lookup admin parent ID ───────────────────────────────────
            Long adminId = jdbcTemplate.queryForObject("SELECT id FROM menu WHERE path = '/admin'", Long.class);

            // ── Admin sub-menus ──────────────────────────────────────────
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"결재 진행 모니터링\",\"en\":\"Approval Progress Monitor\"}', '/admin/approval-monitor', 'fact_check', ?, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"조직 및 부서 관리\",\"en\":\"Organizations & Departments\"}', '/admin/organizations', 'corporate_fare', ?, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"사용자 및 권한 관리\",\"en\":\"Users & Permissions\"}', '/admin/users', 'group', ?, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"메뉴 관리\",\"en\":\"Menu Management\"}', '/admin/menus', 'menu_book', ?, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"시스템 로그 및 연계 관제\",\"en\":\"System Audit & Integration Logs\"}', '/admin/system-logs', 'receipt_long', ?, 5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"매칭 검토\",\"en\":\"Match Review\"}', '/admin/match-review', 'fact_check', ?, 6, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"워크플로우 관리\",\"en\":\"Workflow Management\"}', '/admin/workflow', 'account_tree', ?, 7, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"외부 연동 채널 관리\",\"en\":\"Integration Channels\"}', '/admin/integration/channels', 'hub', ?, 8, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"매칭 규칙 관리\",\"en\":\"Matching Rules\"}', '/admin/matching-rules', 'rule', ?, 9, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", adminId);
            jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES ('{\"ko\":\"생존 규칙 관리\",\"en\":\"Survivorship Rules\"}', '/admin/survivorship', 'alt_route', ?, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", adminId);

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
                Map.entry("/admin/approval-monitor",    Set.of("ROLE_ADMIN")),
                Map.entry("/admin/organizations",       Set.of("ROLE_ADMIN", "ORG_ADMIN")),
                Map.entry("/admin/users",               Set.of("ROLE_ADMIN", "ORG_ADMIN")),
                Map.entry("/admin/menus",               Set.of("ROLE_ADMIN")),
                Map.entry("/admin/system-logs",         Set.of("ROLE_ADMIN")),
                Map.entry("/admin/match-review",        Set.of("ROLE_ADMIN", "DATA_STEWARD")),
                Map.entry("/admin/workflow",            Set.of("ROLE_ADMIN", "ORG_ADMIN")),
                Map.entry("/admin/integration/channels", Set.of("ROLE_ADMIN")),
                Map.entry("/admin/matching-rules",      Set.of("DATA_STEWARD")),
                Map.entry("/admin/survivorship",        Set.of("ROLE_ADMIN", "DATA_STEWARD"))
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

    private void ensureMissingSubMenus() {
        try {
            List<Long> adminParents = jdbcTemplate.queryForList("SELECT id FROM menu WHERE path = '/admin' AND parent_id IS NULL", Long.class);
            if (!adminParents.isEmpty()) {
                Long adminId = adminParents.get(0);

                List<SubMenuDef> missingDefs = List.of(
                    new SubMenuDef("{\"ko\":\"결재 진행 모니터링\",\"en\":\"Approval Monitor\"}", "/admin/approval-monitor", "fact_check", 1, Set.of("ROLE_ADMIN")),
                    new SubMenuDef("{\"ko\":\"조직 및 부서 관리\",\"en\":\"Organizations\"}", "/admin/organizations", "corporate_fare", 2, Set.of("ROLE_ADMIN", "ORG_ADMIN")),
                    new SubMenuDef("{\"ko\":\"사용자 및 권한 관리\",\"en\":\"Users & Roles\"}", "/admin/users", "group", 3, Set.of("ROLE_ADMIN", "ORG_ADMIN")),
                    new SubMenuDef("{\"ko\":\"메뉴 관리\",\"en\":\"Menu Management\"}", "/admin/menus", "menu_book", 4, Set.of("ROLE_ADMIN")),
                    new SubMenuDef("{\"ko\":\"시스템 로그\",\"en\":\"System Audit Logs\"}", "/admin/system-logs", "receipt_long", 5, Set.of("ROLE_ADMIN")),
                    new SubMenuDef("{\"ko\":\"매칭 검토\",\"en\":\"Match Review\"}", "/admin/match-review", "fact_check", 6, Set.of("ROLE_ADMIN", "DATA_STEWARD")),
                    new SubMenuDef("{\"ko\":\"워크플로우 관리\",\"en\":\"Workflow Config\"}", "/admin/workflow", "account_tree", 7, Set.of("ROLE_ADMIN", "ORG_ADMIN")),
                    new SubMenuDef("{\"ko\":\"외부 연동 관리\",\"en\":\"Integration Channels\"}", "/admin/integration/channels", "hub", 8, Set.of("ROLE_ADMIN")),
                    new SubMenuDef("{\"ko\":\"매칭 규칙 관리\",\"en\":\"Matching Rules\"}", "/admin/matching-rules", "rule", 9, Set.of("DATA_STEWARD")),
                    new SubMenuDef("{\"ko\":\"생존 규칙 관리\",\"en\":\"Survivorship Rules\"}", "/admin/survivorship", "alt_route", 10, Set.of("ROLE_ADMIN", "DATA_STEWARD"))
                );

                for (SubMenuDef def : missingDefs) {
                    Integer subCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM menu WHERE path = ? AND parent_id = ?", Integer.class, def.path, adminId);
                    if (subCount == null || subCount == 0) {
                        jdbcTemplate.update("INSERT INTO menu (name, path, icon, parent_id, sort_order, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                                def.name, def.path, def.icon, adminId, def.sortOrder);
                    }
                    List<Long> subMenuIds = jdbcTemplate.queryForList("SELECT id FROM menu WHERE path = ? AND parent_id = ?", Long.class, def.path, adminId);
                    if (!subMenuIds.isEmpty()) {
                        Long subMenuId = subMenuIds.get(0);
                        for (String role : def.roles) {
                            Integer roleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM menu_roles WHERE menu_id = ? AND role_name = ?", Integer.class, subMenuId, role);
                            if (roleCount == null || roleCount == 0) {
                                jdbcTemplate.update("INSERT INTO menu_roles (menu_id, role_name) VALUES (?, ?)", subMenuId, role);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to ensure missing sub-menus", e);
        }
    }

    private static class SubMenuDef {
        final String name;
        final String path;
        final String icon;
        final int sortOrder;
        final Set<String> roles;

        SubMenuDef(String name, String path, String icon, int sortOrder, Set<String> roles) {
            this.name = name;
            this.path = path;
            this.icon = icon;
            this.sortOrder = sortOrder;
            this.roles = roles;
        }
    }
}
