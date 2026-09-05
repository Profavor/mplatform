package com.classification.domain_system.service;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.CustomRecordRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordPagingDeterminismTest {

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @InjectMocks
    private RecordService recordService;

    @Test
    @DisplayName("#108: CustomRecordRepositoryImpl appendOrderByClause 타이브레이커(, r.id ASC) 보장 검증")
    void testAppendOrderByClauseDeterminism() throws Exception {
        CustomRecordRepositoryImpl repo = new CustomRecordRepositoryImpl();
        Method appendOrderMethod = CustomRecordRepositoryImpl.class.getDeclaredMethod("appendOrderByClause", StringBuilder.class, org.springframework.data.domain.Pageable.class);
        appendOrderMethod.setAccessible(true);

        // 1. Unsorted -> ORDER BY r.created_at DESC, r.id ASC
        StringBuilder sql1 = new StringBuilder("SELECT * FROM record r");
        appendOrderMethod.invoke(repo, sql1, PageRequest.of(0, 50));
        assertThat(sql1.toString()).contains("ORDER BY r.created_at DESC, r.id ASC");

        // 2. Custom sort without id -> ends with , r.id ASC
        StringBuilder sql2 = new StringBuilder("SELECT * FROM record r");
        appendOrderMethod.invoke(repo, sql2, PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "price")));
        assertThat(sql2.toString()).endsWith(", r.id ASC");

        // 3. Custom sort already containing id -> no duplicate r.id ASC
        StringBuilder sql3 = new StringBuilder("SELECT * FROM record r");
        appendOrderMethod.invoke(repo, sql3, PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "id")));
        assertThat(sql3.toString()).contains("r.id ASC");
        assertThat(sql3.toString()).doesNotContain("r.id ASC, r.id ASC");
    }

    @Test
    @DisplayName("#108: RecordService collectDescendantNodeIds 다단계 계층 재귀 탐색 검증")
    void testCollectDescendantNodeIdsRecursion() throws Exception {
        UUID rootId = UUID.randomUUID();
        UUID child1Id = UUID.randomUUID();
        UUID child2Id = UUID.randomUUID();
        UUID grandChildId = UUID.randomUUID();

        ClassificationNode child1 = new ClassificationNode();
        child1.setId(child1Id);
        ClassificationNode child2 = new ClassificationNode();
        child2.setId(child2Id);
        ClassificationNode grandChild = new ClassificationNode();
        grandChild.setId(grandChildId);

        when(nodeRepository.findByParentIdAndIsDeletedFalseOrderByOrderAsc(rootId))
                .thenReturn(List.of(child1, child2));
        when(nodeRepository.findByParentIdAndIsDeletedFalseOrderByOrderAsc(child1Id))
                .thenReturn(List.of(grandChild));
        when(nodeRepository.findByParentIdAndIsDeletedFalseOrderByOrderAsc(child2Id))
                .thenReturn(Collections.emptyList());
        when(nodeRepository.findByParentIdAndIsDeletedFalseOrderByOrderAsc(grandChildId))
                .thenReturn(Collections.emptyList());

        List<UUID> accumulatedIds = new ArrayList<>();
        accumulatedIds.add(rootId);

        Method collectMethod = RecordService.class.getDeclaredMethod("collectDescendantNodeIds", UUID.class, List.class);
        collectMethod.setAccessible(true);
        collectMethod.invoke(recordService, rootId, accumulatedIds);

        // Root + Child1 + Child2 + GrandChild 총 4개 노드가 모두 재귀 수집되어야 함
        assertThat(accumulatedIds).containsExactlyInAnyOrder(rootId, child1Id, child2Id, grandChildId);
    }
}
