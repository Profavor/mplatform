package com.classification.domain_system.config;

import com.classification.domain_system.entity.Menu;
import com.classification.domain_system.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuDataInitializer {

    private final MenuRepository menuRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initMenus() {
        try {
            log.info("Checking and initializing system menu tree in DB...");

            // 1. Home / Dashboard
            createMenuIfAbsent("{\"ko\":\"홈\",\"en\":\"Home\"}", "/", "home", null, 1, Set.of("ROLE_USER", "ROLE_ADMIN"));

            // 2. Domain & Schema Management
            createMenuIfAbsent("{\"ko\":\"도메인 & 스키마 관리\",\"en\":\"Domain & Schema\"}", "/schema", "schema", null, 2, Set.of("ROLE_USER", "ROLE_ADMIN"));

            // 3. Master Data Records
            createMenuIfAbsent("{\"ko\":\"마스터 데이터 관리\",\"en\":\"Master Data\"}", "/records", "table_chart", null, 3, Set.of("ROLE_USER", "ROLE_ADMIN"));

            // 4. Data Quality Dashboard
            createMenuIfAbsent("{\"ko\":\"데이터 품질 관리\",\"en\":\"Data Quality\"}", "/dq-dashboard", "insights", null, 4, Set.of("ROLE_USER", "ROLE_ADMIN"));

            // 5. Approvals
            createMenuIfAbsent("{\"ko\":\"결재 & 승인 관리\",\"en\":\"Approvals & Governance\"}", "/approvals", "fact_check", null, 5, Set.of("ROLE_USER", "ROLE_ADMIN"));

            // 6. Match Candidates
            createMenuIfAbsent("{\"ko\":\"데이터 매칭 후보\",\"en\":\"Match Candidates\"}", "/match-candidates", "find_replace", null, 6, Set.of("ROLE_USER", "ROLE_ADMIN"));

            // 7. System Admin Parent Menu
            Menu adminMenu = createMenuIfAbsent("{\"ko\":\"시스템 관리\",\"en\":\"System Admin\"}", "/admin", "admin_panel_settings", null, 7, Set.of("ROLE_ADMIN"));

            if (adminMenu != null) {
                Long adminId = adminMenu.getId();
                createMenuIfAbsent("{\"ko\":\"조직 및 부서 관리\",\"en\":\"Organizations\"}", "/admin/organizations", "corporate_fare", adminId, 1, Set.of("ROLE_ADMIN"));
                createMenuIfAbsent("{\"ko\":\"사용자 및 권한 관리\",\"en\":\"Users & Roles\"}", "/admin/users", "group", adminId, 2, Set.of("ROLE_ADMIN"));
                createMenuIfAbsent("{\"ko\":\"메뉴 관리\",\"en\":\"Menu Management\"}", "/admin/menus", "menu_book", adminId, 3, Set.of("ROLE_ADMIN"));
                createMenuIfAbsent("{\"ko\":\"시스템 로그\",\"en\":\"System Audit Logs\"}", "/admin/system-logs", "receipt_long", adminId, 4, Set.of("ROLE_ADMIN"));
                createMenuIfAbsent("{\"ko\":\"매칭 검토\",\"en\":\"Match Review\"}", "/admin/match-review", "fact_check", adminId, 5, Set.of("ROLE_ADMIN"));
                createMenuIfAbsent("{\"ko\":\"워크플로우 관리\",\"en\":\"Workflow Config\"}", "/admin/workflow", "account_tree", adminId, 6, Set.of("ROLE_ADMIN"));
                createMenuIfAbsent("{\"ko\":\"외부 연동 관리\",\"en\":\"Integration Channels\"}", "/admin/integration/channels", "hub", adminId, 7, Set.of("ROLE_ADMIN"));
                createMenuIfAbsent("{\"ko\":\"매칭 규칙 관리\",\"en\":\"Matching Rules\"}", "/admin/matching-rules", "rule", adminId, 8, Set.of("ROLE_ADMIN"));
            }

            log.info("System menu tree initialization completed successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize menu data", e);
        }
    }

    private Menu createMenuIfAbsent(String name, String path, String icon, Long parentId, Integer sortOrder, Set<String> roles) {
        Menu existing = menuRepository.findAll().stream()
                .filter(m -> path.equalsIgnoreCase(m.getPath()) || ("/admin/integration/channels".equals(path) && "/admin/integration".equalsIgnoreCase(m.getPath())))
                .findFirst().orElse(null);

        if (existing == null) {
            Menu newMenu = Menu.builder()
                    .name(name)
                    .path(path)
                    .icon(icon)
                    .parentId(parentId)
                    .sortOrder(sortOrder)
                    .isActive(true)
                    .build();
            newMenu.getRequiredRoles().addAll(roles);
            Menu saved = menuRepository.save(newMenu);
            log.info("Initialized menu: [{}] -> {}", name, path);
            return saved;
        } else {
            // Update name (if legacy single string), path, roles or parentId if missing
            boolean updated = false;
            if (!name.equals(existing.getName())) {
                existing.setName(name);
                updated = true;
            }
            if (!path.equalsIgnoreCase(existing.getPath())) {
                existing.setPath(path);
                updated = true;
            }
            if (existing.getParentId() == null && parentId != null) {
                existing.setParentId(parentId);
                updated = true;
            }
            if (existing.getRequiredRoles().isEmpty()) {
                existing.getRequiredRoles().addAll(roles);
                updated = true;
            }
            if (updated) {
                menuRepository.save(existing);
                log.info("Updated menu i18n/roles for path: {}", path);
            }
            return existing;
        }
    }
}
