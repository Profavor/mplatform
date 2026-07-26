package com.classification.domain_system.repository;

import com.classification.domain_system.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findAllByIsActiveTrueOrderBySortOrderAsc();
    List<Menu> findAllByOrderBySortOrderAsc();
    List<Menu> findByParentIdAndIsActiveTrueOrderBySortOrderAsc(Long parentId);
}
