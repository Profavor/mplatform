package com.classification.domain_system.dto;

import lombok.Data;

@Data
public class SystemInstallRequest {
    private String adminUsername;
    private String adminPassword;
    private String adminEmail;
    private String organizationName;
    private String organizationNameKo;
    private String organizationNameEn;
    private String timezone;
}
