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
import java.util.Set;

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

    @Transactional
    public void syncMenus() {
        try {
            log.info("Syncing system menu tree from default_menus.json...");
            List<MenuSeedDto> defaultMenus = loadDefaultMenus();
            if (defaultMenus == null || defaultMenus.isEmpty()) {
                log.warn("No default menus found in default_menus.json!");
                return;
            }

            Map<Long, Long> jsonIdToDbId = new HashMap<>();
            
            List<Menu> existingMenus = menuRepository.findAll();
            Map<String, Menu> existingByPath = new HashMap<>();
            for (Menu m : existingMenus) {
                existingByPath.put(m.getPath(), m);
            }
            
            for (MenuSeedDto dto : defaultMenus) {
                if (existingByPath.containsKey(dto.getPath())) {
                    jsonIdToDbId.put(dto.getId(), existingByPath.get(dto.getPath()).getId());
                }
            }

            int processedCount = 0;
            int totalToProcess = defaultMenus.size();
            Set<Long> processedJsonIds = new HashSet<>();

            while (processedCount < totalToProcess) {
                boolean progressMade = false;
                for (MenuSeedDto dto : defaultMenus) {
                    if (processedJsonIds.contains(dto.getId())) {
                        continue;
                    }

                    Long parentDbId = null;
                    if (dto.getParentId() != null) {
                        if (!jsonIdToDbId.containsKey(dto.getParentId())) {
                            continue;
                        }
                        parentDbId = jsonIdToDbId.get(dto.getParentId());
                    }

                    Menu menu = existingByPath.get(dto.getPath());
                    if (menu == null) {
                        menu = new Menu();
                    }
                    
                    menu.setName(dto.getName());
                    menu.setPath(dto.getPath());
                    menu.setIcon(dto.getIcon());
                    menu.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
                    menu.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
                    menu.setParentId(parentDbId);

                    if (dto.getRequiredRoles() != null) {
                        if (menu.getRequiredRoles() == null) {
                            menu.setRequiredRoles(new HashSet<>());
                        } else {
                            menu.getRequiredRoles().clear();
                        }
                        menu.getRequiredRoles().addAll(dto.getRequiredRoles());
                    }

                    menu = menuRepository.save(menu);
                    jsonIdToDbId.put(dto.getId(), menu.getId());
                    processedJsonIds.add(dto.getId());
                    processedCount++;
                    progressMade = true;
                }

                if (!progressMade) {
                    log.error("Could not resolve parent dependencies for some menus. Sync incomplete.");
                    break;
                }
            }
            log.info("System menu sync completed successfully ({} menus processed).", processedCount);
        } catch (Exception e) {
            log.error("Failed to sync menu data", e);
            throw new RuntimeException("Menu sync failed", e);
        }
    }

    private List<MenuSeedDto> loadDefaultMenus() {
        try {
            java.io.InputStream is = null;
            String userDir = System.getProperty("user.dir");
            java.io.File localFile = java.nio.file.Paths.get(userDir, "src", "main", "resources", "default_menus.json").toFile();
            
            if (localFile.exists()) {
                is = new java.io.FileInputStream(localFile);
                log.info("Using local filesystem seed: {}", localFile.getAbsolutePath());
            } else {
                ClassPathResource resource = new ClassPathResource("default_menus.json");
                if (resource.exists()) {
                    is = resource.getInputStream();
                    log.info("Using classpath seed");
                }
            }
            
            if (is == null) {
                log.warn("default_menus.json not found in resources!");
                return null;
            }
            
            try (java.io.InputStream finalIs = is) {
                return objectMapper.readValue(finalIs, new TypeReference<List<MenuSeedDto>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to load default_menus.json", e);
            return null;
        }
    }
}
