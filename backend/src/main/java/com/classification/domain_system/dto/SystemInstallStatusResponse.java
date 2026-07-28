package com.classification.domain_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemInstallStatusResponse {
    @JsonProperty("isInstalled")
    private boolean isInstalled;

    @JsonProperty("hasAdminAccount")
    private boolean hasAdminAccount;
}
