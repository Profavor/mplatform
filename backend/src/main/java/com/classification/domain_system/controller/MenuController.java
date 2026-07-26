package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Menu;
import com.classification.domain_system.entity.MenuAccessLog;
import com.classification.domain_system.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getMenuTree(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(menuService.getMenuTree(includeInactive));
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'menu:write')")
    public ResponseEntity<Menu> createMenu(@RequestBody Menu menu) {
        return ResponseEntity.ok(menuService.createMenu(menu));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'menu:write')")
    public ResponseEntity<Menu> updateMenu(@PathVariable Long id, @RequestBody Menu menu) {
        return ResponseEntity.ok(menuService.updateMenu(id, menu));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'menu:write')")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/access")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logAccess(@RequestBody Map<String, Object> payload, Authentication authentication, jakarta.servlet.http.HttpServletRequest request) {
        Long menuId = payload.get("menuId") != null ? Long.valueOf(payload.get("menuId").toString()) : null;
        String menuPath = (String) payload.get("menuPath");
        String userId = authentication != null ? authentication.getName() : "anonymous";
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIp(request);
        menuService.logAccess(menuId, menuPath, userId, userAgent, clientIp);
        return ResponseEntity.ok().build();
    }

    private String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'log:read')")
    public ResponseEntity<Page<MenuAccessLog>> getAccessLogs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String menuPath,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "accessedAt,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        
        return ResponseEntity.ok(menuService.getMenuAccessLogs(userId, menuPath, startDate, endDate, pageable));
    }
}
