package com.classification.domain_system.config;

import com.classification.domain_system.entity.Menu;
import com.classification.domain_system.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuDataInitializer {

    private final MenuRepository menuRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initMenus() {
        try {
            Menu adminMenu = menuRepository.findAll().stream()
                    .filter(m -> "관리자".equals(m.getName()) || "Admin".equalsIgnoreCase(m.getName()) || "/admin".equals(m.getPath()))
                    .findFirst().orElse(null);

            if (adminMenu != null) {
                Menu workflowMenu = menuRepository.findAll().stream()
                        .filter(m -> "/admin/workflow".equals(m.getPath()))
                        .findFirst().orElse(null);

                if (workflowMenu == null) {
                    workflowMenu = Menu.builder()
                            .name("Workflow Management")
                            .path("/admin/workflow")
                            .icon("account_tree")
                            .parentId(adminMenu.getId())
                            .sortOrder(6)
                            .requiredRole("ROLE_ADMIN")
                            .isActive(true)
                            .build();
                    workflowMenu.getRequiredRoles().add("ROLE_ADMIN");
                    menuRepository.save(workflowMenu);
                    log.info("Successfully registered /admin/workflow menu into DB under parentId: {}", adminMenu.getId());
                } else if (workflowMenu.getParentId() == null || !workflowMenu.getParentId().equals(adminMenu.getId())) {
                    workflowMenu.setParentId(adminMenu.getId());
                    workflowMenu.setSortOrder(6);
                    if (workflowMenu.getRequiredRoles().isEmpty()) {
                        workflowMenu.getRequiredRoles().add("ROLE_ADMIN");
                    }
                    menuRepository.save(workflowMenu);
                    log.info("Successfully updated /admin/workflow parentId to {}", adminMenu.getId());
                }
            }
        } catch (Exception e) {
            log.error("Failed to initialize menu data", e);
        }
    }
}
