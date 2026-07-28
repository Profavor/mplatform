package com.classification.domain_system.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("!test")
@Slf4j
public class DatabaseInitializer implements ApplicationRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void run(ApplicationArguments args) {
        try {
            // Check if current DB is PostgreSQL before running PostgreSQL-specific GIN index creation
            Object dialect = entityManager.getEntityManagerFactory().getProperties().get("hibernate.dialect");
            String dialectStr = dialect != null ? dialect.toString() : "";
            if (dialectStr.contains("H2")) {
                log.info("Skipping GIN index creation for H2 database.");
                return;
            }

            String createIndexSql = "CREATE INDEX IF NOT EXISTS idx_record_data_gin ON record USING GIN (CAST(data AS jsonb))";
            entityManager.createNativeQuery(createIndexSql).executeUpdate();
            log.info("Successfully ensured GIN index on record.data column for performance optimization.");
        } catch (Exception e) {
            log.warn("Failed to create GIN index on record.data. It may already exist or DB is not PostgreSQL: {}", e.getMessage());
        }
    }
}
