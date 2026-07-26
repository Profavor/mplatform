package com.classification.domain_system.service;

import com.classification.domain_system.entity.Menu;
import com.classification.domain_system.entity.MenuAccessLog;
import com.classification.domain_system.repository.MenuAccessLogRepository;
import com.classification.domain_system.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuAccessLogRepository menuAccessLogRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMenuTree() {
        return getMenuTree(false);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMenuTree(boolean includeInactive) {
        List<Menu> allMenus = includeInactive
                ? menuRepository.findAllByOrderBySortOrderAsc()
                : menuRepository.findAllByIsActiveTrueOrderBySortOrderAsc();
        return buildTree(allMenus, null);
    }

    private List<Map<String, Object>> buildTree(List<Menu> allMenus, Long parentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Menu menu : allMenus) {
            if (Objects.equals(menu.getParentId(), parentId)) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", menu.getId());
                node.put("name", menu.getName());
                node.put("path", menu.getPath());
                node.put("icon", menu.getIcon());
                node.put("sortOrder", menu.getSortOrder());
                node.put("parentId", menu.getParentId());
                node.put("isActive", menu.getIsActive());

                Set<String> rolesSet = new LinkedHashSet<>();

                // Legacy requiredRole (if present)
                if (menu.getRequiredRole() != null && !menu.getRequiredRole().trim().isEmpty()) {
                    for (String r : menu.getRequiredRole().split(",")) {
                        if (!r.trim().isEmpty()) rolesSet.add(r.trim());
                    }
                }
                // 1NF normalized menu_roles table rows
                if (menu.getRequiredRoles() != null && !menu.getRequiredRoles().isEmpty()) {
                    menu.getRequiredRoles().forEach(r -> {
                        if (r != null && !r.trim().isEmpty()) rolesSet.add(r.trim());
                    });
                }
                
                List<Map<String, Object>> children = buildTree(allMenus, menu.getId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                    Set<String> unionRoles = new LinkedHashSet<>();
                    collectChildRoles(children, unionRoles);
                    node.put("requiredRoles", new ArrayList<>(unionRoles));
                    node.put("requiredRole", String.join(",", unionRoles));
                } else {
                    node.put("requiredRoles", new ArrayList<>(rolesSet));
                    node.put("requiredRole", String.join(",", rolesSet));
                }
                result.add(node);
            }
        }
        return result;
    }

    private void collectChildRoles(List<Map<String, Object>> children, Set<String> unionRoles) {
        for (Map<String, Object> child : children) {
            Object rolesListObj = child.get("requiredRoles");
            if (rolesListObj instanceof Collection<?>) {
                for (Object item : (Collection<?>) rolesListObj) {
                    if (item != null && !item.toString().trim().isEmpty()) {
                        unionRoles.add(item.toString().trim());
                    }
                }
            } else {
                Object legacyObj = child.get("requiredRole");
                if (legacyObj != null && !legacyObj.toString().trim().isEmpty()) {
                    for (String r : legacyObj.toString().split(",")) {
                        if (!r.trim().isEmpty()) unionRoles.add(r.trim());
                    }
                }
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> grandChildren = (List<Map<String, Object>>) child.get("children");
            if (grandChildren != null && !grandChildren.isEmpty()) {
                collectChildRoles(grandChildren, unionRoles);
            }
        }
    }

    @Transactional
    public Menu createMenu(Menu menu) {
        if (menu.getIsActive() == null) {
            menu.setIsActive(true);
        }
        if (menu.getRequiredRoles() != null) {
            Set<String> clean = menu.getRequiredRoles().stream()
                    .filter(r -> r != null && !r.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            menu.setRequiredRoles(clean);
            menu.setRequiredRole(String.join(",", clean));
        } else if (menu.getRequiredRole() != null) {
            Set<String> clean = Arrays.stream(menu.getRequiredRole().split(","))
                    .filter(r -> !r.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            menu.setRequiredRoles(clean);
            menu.setRequiredRole(String.join(",", clean));
        }
        return menuRepository.save(menu);
    }

    @Transactional
    public Menu updateMenu(Long id, Menu menuDetails) {
        Menu menu = menuRepository.findById(id).orElseThrow(() -> new RuntimeException("Menu not found"));
        if (menuDetails.getName() != null) menu.setName(menuDetails.getName());
        if (menuDetails.getPath() != null) menu.setPath(menuDetails.getPath());
        if (menuDetails.getIcon() != null) menu.setIcon(menuDetails.getIcon());
        menu.setParentId(menuDetails.getParentId());
        if (menuDetails.getSortOrder() != null) menu.setSortOrder(menuDetails.getSortOrder());
        if (menuDetails.getIsActive() != null) {
            menu.setIsActive(menuDetails.getIsActive());
        } else if (menu.getIsActive() == null) {
            menu.setIsActive(true);
        }

        // Update 1NF normalized menu_roles table rows
        if (menuDetails.getRequiredRoles() != null) {
            Set<String> cleanRoles = menuDetails.getRequiredRoles().stream()
                    .filter(r -> r != null && !r.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            menu.getRequiredRoles().clear();
            menu.getRequiredRoles().addAll(cleanRoles);
            menu.setRequiredRole(String.join(",", cleanRoles));
        } else if (menuDetails.getRequiredRole() != null) {
            Set<String> cleanRoles = Arrays.stream(menuDetails.getRequiredRole().split(","))
                    .filter(r -> !r.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            menu.getRequiredRoles().clear();
            menu.getRequiredRoles().addAll(cleanRoles);
            menu.setRequiredRole(String.join(",", cleanRoles));
        }

        return menuRepository.save(menu);
    }

    @Transactional
    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
    }

    @Transactional
    public void logAccess(Long menuId, String menuPath, String userId) {
        logAccess(menuId, menuPath, userId, null, null);
    }

    @Transactional
    public void logAccess(Long menuId, String menuPath, String userId, String userAgent) {
        logAccess(menuId, menuPath, userId, userAgent, null);
    }

    @Transactional
    public void logAccess(Long menuId, String menuPath, String userId, String userAgent, String clientIp) {
        MenuAccessLog log = MenuAccessLog.builder()
                .menuId(menuId)
                .menuPath(menuPath)
                .userId(userId)
                .userAgent(userAgent)
                .clientIp(clientIp)
                .build();
        menuAccessLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<MenuAccessLog> getMenuAccessLogs(String userId, String menuPath, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Specification<MenuAccessLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null && !userId.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (menuPath != null && !menuPath.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("menuPath")), "%" + menuPath.toLowerCase() + "%"));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("accessedAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("accessedAt"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return menuAccessLogRepository.findAll(spec, pageable);
    }
}
