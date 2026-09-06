package com.classification.domain_system.controller;

import com.classification.domain_system.dto.RoiCalculationRequest;
import com.classification.domain_system.dto.RoiCalculationResponse;
import com.classification.domain_system.service.RoiCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roi")
@RequiredArgsConstructor
public class RoiCalculatorController {

    private final RoiCalculatorService roiCalculatorService;

    @PostMapping("/calculate")
    public ResponseEntity<RoiCalculationResponse> calculateRoi(@RequestBody(required = false) RoiCalculationRequest request) {
        RoiCalculationResponse response = roiCalculatorService.calculate(request);
        return ResponseEntity.ok(response);
    }
}
