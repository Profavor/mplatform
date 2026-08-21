package com.classification.domain_system.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Master Data Management (MDM) Enterprise Platform API")
                .version("1.0.0")
                .description("조직 내 마스터 데이터를 통합·정제하고 불변 감사 원장, AI 자율 치유, 골든 레코드 매칭 및 전자 결재 워크플로우를 제공하는 차세대 MDM 엔터프라이즈 REST API 명세서")
                .contact(new Contact().name("MDM Enterprise Architecture Team").email("admin@mplatform.com"))
                .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(List.of(
                new Server().url("/api").description("기본 Ingress API Gateway 경로"),
                new Server().url("/").description("직접 접속 Root 경로"),
                new Server().url("https://mplatform.local/api").description("K8s 프로덕션 인프라 경로")
            ))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                    .name(SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Keycloak SSO 및 MDM 자체 인증 JWT Access Token을 Bearer 스킴으로 전송합니다.")
                )
            );
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
            .group("00-all-apis")
            .pathsToMatch("/**")
            .build();
    }

    @Bean
    public GroupedOpenApi coreMdmApi() {
        return GroupedOpenApi.builder()
            .group("01-core-mdm")
            .pathsToMatch("/api/domains/**", "/api/records/**", "/api/fields/**", "/api/nodes/**", "/api/axes/**")
            .build();
    }

    @Bean
    public GroupedOpenApi dqGovernanceApi() {
        return GroupedOpenApi.builder()
            .group("02-dq-governance")
            .pathsToMatch("/api/dq/**", "/api/approvals/**", "/api/matching/**", "/api/survivorship/**")
            .build();
    }

    @Bean
    public GroupedOpenApi platformApi() {
        return GroupedOpenApi.builder()
            .group("03-platform-collaboration")
            .pathsToMatch("/api/auth/**", "/api/users/**", "/api/codes/**", "/api/menus/**", "/api/system/**", "/api/inbox/**", "/api/chat/**", "/api/integration/**", "/api/organizations/**", "/api/files/**")
            .build();
    }
}
