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
            createMenuIfAbsent("홈", "/", "home", null, 1, Set.of("ROLE_USER", "ROLE_ADMIN"));

            // 2. Domain & Schema Management
            createMenuIfAbsent("도메인 & 스키마 관리", "/schema", "schema", null, 2, Set.of("ROLE_USER", "ROLE_ADMIN"));

            // 3. Master Data Records
            createMenuIfAbsent("마스터 데이터 관리", "/records", "table_chart", null, 3, Set.of("ROLE_USER", "ROLE_ADMIN"));

            // 4. Data Quality Dashboard
            createMenuIfAbsent("데이터 품질 관리", "/dq-dashboard", "insights", null, 4, Set.of("ROLE_USER", "ROLE_ADMIN"));

            // 5. Approvals
            createMenuIfAbsent("결재 & 승인 관리", "/approvals", "fact_check", null, 5, Set.of("ROLE_USER", "ROLE_ADMIN"));

            // 6. Match Candidates
            createMenuIfAbsent("데이터 매칭 후보", "/match-candidates", "find_replace", null, 6, Set.of("ROLE_USER", "ROLE_ADMIN"));

            // 7. System Admin Parent Menu
            Menu adminMenu = createMenuIfAbsent("시스템 관리", "/admin", "admin_panel_settings", null, 7, Set.of("ROLE_ADMIN"));

            if (adminMenu != null) {
                Long adminId = adminMenu.getId();
                createMenuIfAbsent("조직 및 부서 관리", "/admin/organizations", "corporate_fare", adminId, 1, Set.of("ROLE_ADMIN"));
                createMenuIfAbsent("사용자 및 권한 관리", "/admin/users", "group", adminId, 2, Set.of("ROLE_ADMIN"));
                createMenuIfAbsent("메뉴 관리", "/admin/menus", "menu_book", adminId, 3, Set.of("ROLE_ADMIN"));
                createMenuIfAbsent("시스템 로그", "/admin/system-logs", "receipt_long", adminId, 4, Set.of("ROLE_ADMIN"));
                createMenuIfAbsent("매칭 검토", "/admin/match-review", "fact_check", adminId, 5, Set.of("ROLE_ADMIN"));
                createMenuIfAbsent("워크플로우 관리", "/admin/workflow", "account_tree", adminId, 6, Set.of("ROLE_ADMIN"));
                createMenuIfAbsent("외부 연동 관리", "/admin/integration", "hub", adminId, 7, Set.of("ROLE_ADMIN"));
            }

            log.info("System menu tree initialization completed successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize menu data", e);
        }
    }

    private Menu createMenuIfAbsent(String name, String path, String icon, Long parentId, Integer sortOrder, Set<String> roles) {
        Menu existing = menuRepository.findAll().stream()
                .filter(m -> path.equalsIgnoreCase(m.getPath()))
                .findFirst().orElse(null);

        if (existing == null) {
            Menu newMenu = Menu.builder()
                    .name(name)
                    .path(path)
                    .icon(icon)
                    .parentId(parentId)
                    .sortOrder(sortOrder)
                    .requiredRole(roles.contains("ROLE_ADMIN") && roles.size() == 1 ? "ROLE_ADMIN" : "ROLE_USER")
                    .isActive(true)
                    .build();
            newMenu.getRequiredRoles().addAll(roles);
            Menu saved = menuRepository.save(newMenu);
            log.info("Initialized menu: [{}] -> {}", name, path);
            return saved;
        } else {
            // Update roles or parentId if missing
            boolean updated = false;
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
            }
            return existing;
        }
    }
}
