package com.classification.domain_system.repository;

import com.classification.domain_system.entity.BatchJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BatchJobRepository extends JpaRepository<BatchJob, UUID> {
}
