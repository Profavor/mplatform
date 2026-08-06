package com.classification.domain_system.service;

import com.classification.domain_system.entity.PermissionGroup;
import com.classification.domain_system.entity.PermissionItem;
import com.classification.domain_system.repository.PermissionGroupRepository;
import com.classification.domain_system.dto.PermissionGroupSeedDto;
import com.classification.domain_system.dto.PermissionItemSeedDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionMasterInitializer implements CommandLineRunner {

    private final PermissionGroupRepository groupRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (groupRepository.count() > 0) {
            log.info("Permission Master data already exists. Skipping initialization.");
            return;
        }

        log.info("No permission master data found. Initializing default groups and items from default_permissions.json...");

        List<PermissionGroupSeedDto> defaultPermissions = loadDefaultPermissions();
        if (defaultPermissions == null || defaultPermissions.isEmpty()) {
            log.warn("No default permissions found in default_permissions.json!");
            return;
        }

        for (PermissionGroupSeedDto dto : defaultPermissions) {
            createOrUpdateGroup(dto);
        }

        log.info("Default Permission Master groups checking/seeding completed successfully.");
    }

    private List<PermissionGroupSeedDto> loadDefaultPermissions() {
        try {
            java.io.InputStream is = null;
            String userDir = System.getProperty("user.dir");
            java.io.File localFile = java.nio.file.Paths.get(userDir, "src", "main", "resources", "default_permissions.json").toFile();
            
            if (localFile.exists()) {
                is = new java.io.FileInputStream(localFile);
                log.info("Using local filesystem seed: {}", localFile.getAbsolutePath());
            } else {
                ClassPathResource resource = new ClassPathResource("default_permissions.json");
                if (resource.exists()) {
                    is = resource.getInputStream();
                    log.info("Using classpath seed");
                }
            }
            
            if (is == null) {
                log.warn("default_permissions.json not found in resources!");
                return null;
            }
            
            try (java.io.InputStream finalIs = is) {
                return objectMapper.readValue(finalIs, new TypeReference<List<PermissionGroupSeedDto>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to load default_permissions.json", e);
            return null;
        }
    }

    private void createOrUpdateGroup(PermissionGroupSeedDto dto) {
        Optional<PermissionGroup> groupOpt = groupRepository.findById(dto.getId());
        PermissionGroup group;
        if (groupOpt.isPresent()) {
            group = groupOpt.get();
        } else {
            group = new PermissionGroup();
            group.setId(dto.getId());
        }
        group.setCode(dto.getCode());
        group.setTitleKo(dto.getTitleKo());
        group.setTitleEn(dto.getTitleEn());
        group.setIcon(dto.getIcon());
        group.setColor(dto.getColor());
        group.setChipClass(dto.getChipClass());
        group.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        if (dto.getItems() != null) {
            for (PermissionItemSeedDto itemData : dto.getItems()) {
                boolean exists = group.getItems().stream()
                        .anyMatch(i -> i.getPermValue().equalsIgnoreCase(itemData.getPermValue()));
                if (!exists) {
                    PermissionItem item = new PermissionItem();
                    item.setLabelKo(itemData.getLabelKo());
                    item.setLabelEn(itemData.getLabelEn());
                    item.setPermValue(itemData.getPermValue());
                    item.setSortOrder(itemData.getSortOrder() != null ? itemData.getSortOrder() : 0);
                    group.addItem(item);
                }
            }
        }
        groupRepository.save(group);
    }
}
