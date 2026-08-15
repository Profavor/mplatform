package com.classification.domain_system.controller;

import com.classification.domain_system.dto.RecordTimeMachineDto;
import com.classification.domain_system.service.RecordTimeMachineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/records/{recordId}/timemachine")
@RequiredArgsConstructor
public class RecordTimeMachineController {

    private final RecordTimeMachineService timeMachineService;

    @GetMapping("/diff")
    @PreAuthorize("hasPermission(null, 'record:read')")
    public ResponseEntity<RecordTimeMachineDto.TimeMachineDiffResponse> getTimelineAndDiff(
            @PathVariable UUID recordId,
            @RequestParam(required = false) Integer v1,
            @RequestParam(required = false) Integer v2) {
        return ResponseEntity.ok(timeMachineService.getTimelineAndDiff(recordId, v1, v2));
    }
}
