package com.classification.domain_system.controller;

import com.classification.domain_system.dto.WorkspaceWidgetDto;
import com.classification.domain_system.service.WorkspaceWidgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspace/widgets")
@RequiredArgsConstructor
public class WorkspaceWidgetController {

    private final WorkspaceWidgetService workspaceWidgetService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorkspaceWidgetDto.WidgetItem>> getUserWidgets(Authentication auth) {
        String userId = auth != null ? auth.getName() : "user";
        return ResponseEntity.ok(workspaceWidgetService.getUserWidgets(userId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorkspaceWidgetDto.WidgetItem>> saveUserWidgets(
            Authentication auth,
            @RequestBody WorkspaceWidgetDto.SaveWorkspaceWidgetsRequest request) {
        String userId = auth != null ? auth.getName() : "user";
        return ResponseEntity.ok(workspaceWidgetService.saveUserWidgets(userId, request.getWidgets()));
    }
}
