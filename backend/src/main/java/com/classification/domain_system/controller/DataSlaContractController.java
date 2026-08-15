package com.classification.domain_system.controller;

import com.classification.domain_system.dto.DataSlaContractDto;
import com.classification.domain_system.service.DataSlaContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/sla-contracts")
@RequiredArgsConstructor
public class DataSlaContractController {

    private final DataSlaContractService dataSlaContractService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'integration:read')")
    public ResponseEntity<DataSlaContractDto.DataSlaReport> getSlaContracts() {
        return ResponseEntity.ok(dataSlaContractService.getSlaContracts());
    }
}
