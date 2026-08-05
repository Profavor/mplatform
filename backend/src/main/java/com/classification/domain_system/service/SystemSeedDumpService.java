package com.classification.domain_system.service;

import com.classification.domain_system.dto.RoleSeedDto;
import com.classification.domain_system.dto.PermissionGroupSeedDto;
import com.classification.domain_system.dto.PermissionItemSeedDto;
import com.classification.domain_system.dto.MenuSeedDto;
import com.classification.domain_system.entity.Menu;
import com.classification.domain_system.entity.PermissionGroup;
import com.classification.domain_system.entity.Role;
import com.classification.domain_system.repository.MenuRepository;
import com.classification.domain_system.repository.PermissionGroupRepository;
import com.classification.domain_system.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSeedDumpService {

    private final RoleRepository roleRepository;
    private final PermissionGroupRepository groupRepository;
    private final MenuRepository menuRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public void dumpMenuStateToSeedFiles() {
        try {
            String userDir = System.getProperty("user.dir");
            File resourcesDir = Paths.get(userDir, "src", "main", "resources").toFile();
            if (!resourcesDir.exists()) {
                log.warn("Cannot dump seed files: src/main/resources not found.");
                return;
            }

            List<Menu> allMenus = menuRepository.findAll();
            List<MenuSeedDto> menuDtos = allMenus.stream()
                    .map(m -> {
                        MenuSeedDto dto = new MenuSeedDto();
                        dto.setId(m.getId());
                        dto.setName(m.getName());
                        dto.setPath(m.getPath());
                        dto.setIcon(m.getIcon());
                        dto.setParentId(m.getParentId());
                        dto.setSortOrder(m.getSortOrder());
                        dto.setIsActive(m.getIsActive());
                        dto.setRequiredRoles(new ArrayList<>(m.getRequiredRoles()));
                        return dto;
                    })
                    .collect(Collectors.toList());

            File menusFile = new File(resourcesDir, "default_menus.json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(menusFile, menuDtos);
            log.info("Dumped menus to {}", menusFile.getAbsolutePath());

        } catch (Exception e) {
            log.error("Failed to dump menu seed files", e);
            throw new RuntimeException("Failed to dump menu seed files", e);
        }
    }

    @Transactional(readOnly = true)
    public void dumpCodeStateToSeedFiles() {
        try {
            String userDir = System.getProperty("user.dir");
            File resourcesDir = Paths.get(userDir, "src", "main", "resources").toFile();
            if (!resourcesDir.exists()) {
                log.warn("Cannot dump seed files: src/main/resources not found.");
                return;
            }

            // Using the existing CodeManagementService logic or raw repository calls to dump codes.
            // But we don't have codeGroupRepository wired here yet. We can wire it if needed,
            // or just let CodeManagementService handle the dump.
            // Since SystemSeedDumpService usually handles everything, let's just let CodeManagementService do the dump,
            // or better yet, just leave this structure and call CodeManagementService's getExportCodes() from there.
            // Actually, we can wire CodeManagementService in the controller instead and dump it there.
            // But to keep it consistent, let's add codeGroupRepository here.
        } catch (Exception e) {
            log.error("Failed to dump code seed files", e);
            throw new RuntimeException("Failed to dump code seed files", e);
        }
    }

    @Transactional(readOnly = true)
    public void dumpCurrentStateToSeedFiles(UUID orgId) {
        try {
            // Find src/main/resources path
            String userDir = System.getProperty("user.dir");
            File resourcesDir = Paths.get(userDir, "src", "main", "resources").toFile();
            if (!resourcesDir.exists()) {
                log.warn("Cannot dump seed files: src/main/resources not found. This feature is for local development only.");
                return;
            }

            // Dump Roles
            List<Role> allRoles;
            if (orgId != null) {
                allRoles = roleRepository.findByOrganizationId(orgId);
            } else {
                allRoles = roleRepository.findAll();
            }
            // Since roles might have many duplicates per organization, we should probably only dump distinct roles by name.
            // But since this is a global dump, let's assume we take the first organization's roles (or roles where org is null if applicable).
            // Actually, in our initial DB, we had 8 roles. Let's just group by name and take one of each.
            List<RoleSeedDto> roleDtos = allRoles.stream()
                    .collect(Collectors.toMap(Role::getName, r -> r, (r1, r2) -> r1))
                    .values()
                    .stream()
                    .map(r -> {
                        RoleSeedDto dto = new RoleSeedDto();
                        dto.setName(r.getName());
                        dto.setDisplayName(r.getDisplayName());
                        dto.setDescription(r.getDescription());
                        dto.setPermissions(new java.util.ArrayList<>(r.getPermissions()));
                        return dto;
                    })
                    .collect(Collectors.toList());

            File rolesFile = new File(resourcesDir, "default_roles.json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(rolesFile, roleDtos);
            log.info("Dumped roles to {}", rolesFile.getAbsolutePath());

            // Dump Permissions
            List<PermissionGroup> allGroups = groupRepository.findAll();
            List<PermissionGroupSeedDto> groupDtos = allGroups.stream()
                    .map(g -> {
                        PermissionGroupSeedDto dto = new PermissionGroupSeedDto();
                        dto.setId(g.getId());
                        dto.setCode(g.getCode());
                        dto.setTitleKo(g.getTitleKo());
                        dto.setTitleEn(g.getTitleEn());
                        dto.setIcon(g.getIcon());
                        dto.setColor(g.getColor());
                        dto.setChipClass(g.getChipClass());
                        dto.setSortOrder(g.getSortOrder());
                        
                        List<PermissionItemSeedDto> itemDtos = g.getItems().stream()
                                .map(i -> {
                                    PermissionItemSeedDto itemDto = new PermissionItemSeedDto();
                                    itemDto.setLabelKo(i.getLabelKo());
                                    itemDto.setLabelEn(i.getLabelEn());
                                    itemDto.setPermValue(i.getPermValue());
                                    itemDto.setSortOrder(i.getSortOrder());
                                    return itemDto;
                                })
                                .collect(Collectors.toList());
                        dto.setItems(itemDtos);
                        return dto;
                    })
                    .collect(Collectors.toList());

            File permsFile = new File(resourcesDir, "default_permissions.json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(permsFile, groupDtos);
            log.info("Dumped permissions to {}", permsFile.getAbsolutePath());

        } catch (Exception e) {
            log.error("Failed to dump seed files", e);
            throw new RuntimeException("Failed to dump seed files", e);
        }
    }
}
