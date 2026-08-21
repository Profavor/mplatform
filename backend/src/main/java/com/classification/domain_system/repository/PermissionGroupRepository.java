package com.classification.domain_system.repository;

import com.classification.domain_system.entity.PermissionGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, String> {

    @Override
    @EntityGraph(attributePaths = {"items"})
    List<PermissionGroup> findAll();

    @Override
    @EntityGraph(attributePaths = {"items"})
    Optional<PermissionGroup> findById(String id);

    @EntityGraph(attributePaths = {"items"})
    List<PermissionGroup> findAllByOrderBySortOrderAsc();
}
