package com.classification.domain_system.controller;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.enums.ApprovalStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ApprovalSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("ClassificationNode 직렬화 시 분류 체계 트리를 위해 children 필드가 정상 포함된다")
    void classificationNode_ChildrenIncludedOnSerialization() {
        ClassificationNode parent = new ClassificationNode();
        parent.setId(UUID.randomUUID());
        parent.setName(Map.of("ko", "부모노드"));
        parent.setPath("/parent");
        parent.setDepth(1);
        parent.setOrder(1);

        ClassificationNode child = new ClassificationNode();
        child.setId(UUID.randomUUID());
        child.setName(Map.of("ko", "자식노드"));
        child.setPath("/parent/child");
        child.setDepth(2);
        child.setOrder(1);
        child.setParent(parent);

        parent.getChildren().add(child);

        String json = assertDoesNotThrow(() -> objectMapper.writeValueAsString(parent));

        assertThat(json).contains("부모노드");
        assertThat(json).contains("자식노드");
        assertThat(json).contains("children");
    }

    @Test
    @DisplayName("ApprovalRequest 직렬화 시 classificationNode의 children 등 Lazy 속성은 무시되어 안전하게 직렬화된다")
    void approvalRequest_SerializationSuccess() {
        Domain domain = new Domain();
        domain.setId(UUID.randomUUID());
        domain.setName(Map.of("ko", "국회온 의안 마스터"));

        ClassificationNode node = new ClassificationNode();
        node.setId(UUID.randomUUID());
        node.setName(Map.of("ko", "제22대 의안"));
        node.setDomain(domain);

        ClassificationNode childNode = new ClassificationNode();
        childNode.setId(UUID.randomUUID());
        childNode.setName(Map.of("ko", "하위노드"));
        node.getChildren().add(childNode);

        ApprovalRequest approval = new ApprovalRequest();
        approval.setId(UUID.randomUUID());
        approval.setTargetType("RECORD");
        approval.setTargetId(UUID.randomUUID());
        approval.setRequesterId("tester");
        approval.setStatus(ApprovalStatus.APPROVED.name());
        approval.setChanges("{\"BILL_NO\":\"2208232\",\"BILL_CODE\":\"BIL-000001\"}");
        approval.setClassificationNode(node);

        String json = assertDoesNotThrow(() -> objectMapper.writeValueAsString(approval));

        assertThat(json).contains("국회온 의안 마스터");
        assertThat(json).contains("제22대 의안");
        assertThat(json).contains("BIL-000001");
        // ApprovalRequest 내부에서는 classificationNode의 children이 @JsonIgnoreProperties에 의해 무시되어야 함
        assertThat(json).doesNotContain("하위노드");
    }
}
