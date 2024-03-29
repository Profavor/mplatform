package com.favorsoft.mplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"com.favorsoft.mplatform.cdn.repository.jpa"})
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")
public class JpaConfig {
}
