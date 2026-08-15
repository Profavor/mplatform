package com.classification.domain_system.controller;

import com.classification.domain_system.dto.GovernanceCopilotDto;
import com.classification.domain_system.service.GovernanceCopilotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/governance/copilot")
@RequiredArgsConstructor
public class GovernanceCopilotController {

    private final GovernanceCopilotService governanceCopilotService;

    @PostMapping("/chat")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'domain:read') or hasPermission(null, 'record:read')")
    public ResponseEntity<GovernanceCopilotDto.CopilotChatResponse> askCopilot(
            @RequestBody GovernanceCopilotDto.CopilotChatRequest request) {
        return ResponseEntity.ok(governanceCopilotService.askCopilot(request));
    }
}
