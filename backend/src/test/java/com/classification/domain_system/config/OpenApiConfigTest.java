package com.classification.domain_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    @DisplayName("OpenAPI 메인 빈 검증: 제목, 버전, JWT Bearer SecurityScheme이 올바르게 구성되어야 한다.")
    void customOpenAPIShouldConfigureSecuritySchemeAndInfo() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).contains("MDM");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");

        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("BearerAuth");

        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("BearerAuth");
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");

        assertThat(openAPI.getSecurity()).isNotEmpty();
        assertThat(openAPI.getSecurity().get(0)).containsKey("BearerAuth");
    }

    @Test
    @DisplayName("GroupedOpenApi 빈 검증: 전사 API, 코어 MDM, 거버넌스, 플랫폼 서브시스템별 그룹핑이 구성되어야 한다.")
    void groupedOpenApiShouldBeConfigured() {
        GroupedOpenApi allApi = openApiConfig.allApi();
        assertThat(allApi.getGroup()).isEqualTo("00-all-apis");

        GroupedOpenApi coreApi = openApiConfig.coreMdmApi();
        assertThat(coreApi.getGroup()).isEqualTo("01-core-mdm");

        GroupedOpenApi dqApi = openApiConfig.dqGovernanceApi();
        assertThat(dqApi.getGroup()).isEqualTo("02-dq-governance");

        GroupedOpenApi platformApi = openApiConfig.platformApi();
        assertThat(platformApi.getGroup()).isEqualTo("03-platform-collaboration");
    }
}
