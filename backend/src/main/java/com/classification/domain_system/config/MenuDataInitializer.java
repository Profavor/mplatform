package com.classification.domain_system.config;

import com.classification.domain_system.dto.MenuSeedDto;
import com.classification.domain_system.entity.Menu;
import com.classification.domain_system.repository.MenuRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuDataInitializer {

    private final MenuRepository menuRepository;
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initMenus() {
        try {
            if (menuRepository.count() > 0) {
                log.info("Menu data already exists. Skipping initialization.");
                return;
            }

            log.info("No menu data found. Initializing system menu tree from default_menus.json...");

            List<MenuSeedDto> defaultMenus = loadDefaultMenus();
            if (defaultMenus == null || defaultMenus.isEmpty()) {
                log.warn("No default menus found in default_menus.json!");
                return;
            }

            Map<Long, Long> idMapping = new HashMap<>();
            int savedCount = 0;
            int totalToSave = defaultMenus.size();

            while (savedCount < totalToSave) {
                boolean progressMade = false;
                for (MenuSeedDto dto : defaultMenus) {
                    if (idMapping.containsKey(dto.getId())) {
                        continue;
                    }

                    Long newParentId = null;
                    if (dto.getParentId() != null) {
                        if (!idMapping.containsKey(dto.getParentId())) {
                            continue;
                        }
                        newParentId = idMapping.get(dto.getParentId());
                    }

                    Menu menu = new Menu();
                    menu.setName(dto.getName());
                    menu.setPath(dto.getPath());
                    menu.setIcon(dto.getIcon());
                    menu.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
                    menu.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
                    menu.setParentId(newParentId);

                    if (dto.getRequiredRoles() != null) {
                        menu.setRequiredRoles(new HashSet<>(dto.getRequiredRoles()));
                    }

                    menu = menuRepository.save(menu);
                    idMapping.put(dto.getId(), menu.getId());
                    savedCount++;
                    progressMade = true;
                }

                if (!progressMade) {
                    log.error("Could not resolve parent dependencies for some menus. Initialization incomplete.");
                    break;
                }
            }

            log.info("System menu tree initialization completed successfully ({} menus created).", savedCount);
        } catch (Exception e) {
            log.error("Failed to initialize menu data", e);
        }
    }

    private List<MenuSeedDto> loadDefaultMenus() {
        try {
            ClassPathResource resource = new ClassPathResource("default_menus.json");
            if (!resource.exists()) {
                log.warn("default_menus.json not found in resources!");
                return null;
            }
            try (InputStream is = resource.getInputStream()) {
                return objectMapper.readValue(is, new TypeReference<List<MenuSeedDto>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to load default_menus.json", e);
            return null;
        }
    }
}
