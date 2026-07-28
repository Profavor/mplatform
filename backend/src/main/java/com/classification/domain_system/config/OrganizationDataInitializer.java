package com.classification.domain_system.config;

import com.classification.domain_system.repository.OrganizationRepository;
import com.classification.domain_system.service.RoleInitializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrganizationDataInitializer implements ApplicationRunner {

    private final OrganizationRepository organizationRepository;
    private final RoleInitializer roleInitializer;

    @Override
    public void run(ApplicationArguments args) {
        // Master organization creation is now handled dynamically via SystemInstallWizard (/install)
        log.info("OrganizationDataInitializer skipped. Master organization creation is handled via System Installation Wizard.");
    }
}
