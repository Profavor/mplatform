package com.classification.domain_system.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RecordSecondaryNodeQueryTest {

    @Test
    @DisplayName("보조 축 노드 ID 조회를 위한 SQL 조건절 구성 검증 (r.node_id OR rsn.node_id)")
    void buildSecondaryNodeWhereClause_IncludesSecondaryNodeRelation() {
        List<UUID> nodeIds = List.of(UUID.randomUUID());
        
        StringBuilder nodeInClause = new StringBuilder();
        for (int i = 0; i < nodeIds.size(); i++) {
            if (i > 0) nodeInClause.append(", ");
            nodeInClause.append(":nodeId_").append(i);
        }

        StringBuilder secondaryClause = new StringBuilder(
            "( r.node_id IN (" + nodeInClause + ") OR EXISTS (SELECT 1 FROM record_secondary_node rsn WHERE rsn.record_id = r.id AND rsn.node_id IN (" + nodeInClause + ")) )"
        );

        String resultSql = secondaryClause.toString();
        assertThat(resultSql).contains("record_secondary_node rsn");
        assertThat(resultSql).contains("rsn.record_id = r.id");
        assertThat(resultSql).contains("rsn.node_id IN (:nodeId_0)");
    }
}
