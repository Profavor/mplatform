package com.classification.domain_system.repository;

import com.classification.domain_system.entity.PermissionGroup;
import com.classification.domain_system.entity.PermissionItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PermissionGroupRepositoryTest {

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("findAllByOrderBySortOrderAsc - EntityGraph로 items 컬렉션이 함께 로딩된다 (LAZY fetch 최적화)")
    void findAllByOrderBySortOrderAsc_FetchesItemsWithEntityGraph() {
        // Given
        PermissionGroup group = new PermissionGroup();
        group.setId("domain_test");
        group.setCode("domain_test");
        group.setTitleKo("도메인 테스트");
        group.setTitleEn("Domain Test");
        group.setSortOrder(1);

        PermissionItem item1 = new PermissionItem();
        item1.setLabelKo("조회");
        item1.setPermValue("domain:read");
        item1.setSortOrder(1);
        group.addItem(item1);

        PermissionItem item2 = new PermissionItem();
        item2.setLabelKo("작성");
        item2.setPermValue("domain:write");
        item2.setSortOrder(2);
        group.addItem(item2);

        permissionGroupRepository.saveAndFlush(group);
        entityManager.clear(); // 영속성 컨텍스트 초기화 (1차 캐시 비움)

        // When
        List<PermissionGroup> groups = permissionGroupRepository.findAllByOrderBySortOrderAsc();

        // Then
        assertThat(groups).isNotEmpty();
        PermissionGroup fetchedGroup = groups.stream()
                .filter(g -> "domain_test".equals(g.getId()))
                .findFirst()
                .orElse(null);

        assertThat(fetchedGroup).isNotNull();
        // items 컬렉션이 LAZY이지만 EntityGraph 덕분에 트랜잭션 내에서 정상 로딩
        assertThat(fetchedGroup.getItems()).hasSize(2);
        assertThat(fetchedGroup.getItems().get(0).getPermValue()).isEqualTo("domain:read");
        assertThat(fetchedGroup.getItems().get(1).getPermValue()).isEqualTo("domain:write");
    }

    @Test
    @DisplayName("findById - EntityGraph로 items 컬렉션이 함께 로딩된다")
    void findById_FetchesItemsWithEntityGraph() {
        // Given
        PermissionGroup group = new PermissionGroup();
        group.setId("node_test");
        group.setCode("node_test");
        group.setTitleKo("노드 테스트");
        group.setTitleEn("Node Test");
        group.setSortOrder(2);

        PermissionItem item = new PermissionItem();
        item.setLabelKo("관리");
        item.setPermValue("node:admin");
        item.setSortOrder(1);
        group.addItem(item);

        permissionGroupRepository.saveAndFlush(group);
        entityManager.clear();

        // When
        Optional<PermissionGroup> found = permissionGroupRepository.findById("node_test");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getItems()).hasSize(1);
        assertThat(found.get().getItems().get(0).getPermValue()).isEqualTo("node:admin");
    }
}
