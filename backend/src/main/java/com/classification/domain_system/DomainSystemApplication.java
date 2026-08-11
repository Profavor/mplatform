package com.classification.domain_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "com.classification.domain_system.repository")
public class DomainSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(DomainSystemApplication.class, args);
	}

}
