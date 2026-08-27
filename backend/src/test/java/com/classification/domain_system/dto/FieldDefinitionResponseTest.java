package com.classification.domain_system.dto;

import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.FieldGroup;
import com.classification.domain_system.entity.Sector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FieldDefinitionResponseTest {

    @Test
    @DisplayName("FieldDefinition entity에 FieldGroup과 Sector가 존재할 때 Response DTO에 sector 객체가 누락 없이 매핑되어야 한다")
    void shouldMapFieldGroupAndSectorCompletely() {
        // Given
        UUID domainId = UUID.randomUUID();
        Domain domain = new Domain();
        domain.setId(domainId);
        domain.setName(Map.of("ko", "고객 도메인"));

        UUID sectorId = UUID.randomUUID();
        Sector sector = new Sector();
        sector.setId(sectorId);
        sector.setDomain(domain);
        sector.setName(Map.of("ko", "기본 정보 섹터"));
        sector.setSortOrder(1);

        UUID groupId = UUID.randomUUID();
        FieldGroup group = new FieldGroup();
        group.setId(groupId);
        group.setDomain(domain);
        group.setSector(sector);
        group.setName(Map.of("ko", "식별자 그룹"));
        group.setSortOrder(10);
        group.setIsDefaultOpen(true);

        FieldDefinition field = new FieldDefinition();
        field.setId(UUID.randomUUID());
        field.setDomain(domain);
        field.setKey("customer_name");
        field.setType("TEXT");
        field.setName(Map.of("ko", "고객명"));
        field.setFieldGroup(group);

        // When
        FieldDefinitionResponse response = FieldDefinitionResponse.from(field);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFieldGroup()).isNotNull();
        assertThat(response.getFieldGroup().getId()).isEqualTo(groupId);
        assertThat(response.getFieldGroup().getName()).isEqualTo(Map.of("ko", "식별자 그룹"));
        assertThat(response.getFieldGroup().getSectorId()).isEqualTo(sectorId);

        // 핵심 검증: groupDto 내 sector 객체가 반드시 매핑되어 있어야 함
        assertThat(response.getFieldGroup().getSector())
                .as("FieldGroupDto의 sector 객체가 null이면 안 됩니다")
                .isNotNull();
        assertThat(response.getFieldGroup().getSector().getId()).isEqualTo(sectorId);
        assertThat(response.getFieldGroup().getSector().getName()).isEqualTo(Map.of("ko", "기본 정보 섹터"));
        assertThat(response.getFieldGroup().getSector().getSortOrder()).isEqualTo(1);
    }
}
