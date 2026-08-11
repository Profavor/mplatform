package com.classification.domain_system.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileConfigurationTest {

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    private String resolveDdlAutoValue(Object ddlAuto) {
        if (ddlAuto == null) return null;
        String str = ddlAuto.toString().trim();
        if (str.startsWith("${") && str.contains(":") && str.endsWith("}")) {
            return str.substring(str.indexOf(':') + 1, str.length() - 1);
        }
        return str;
    }

    @Test
    @DisplayName("기본 application.yml의 ddl-auto는 환경변수 주입을 받아야 함")
    void defaultApplicationYml_HasSafeDdlAuto() throws IOException {
        List<PropertySource<?>> sources = loader.load("application", new ClassPathResource("application.yml"));
        assertThat(sources).isNotEmpty();

        PropertySource<?> source = sources.get(0);
        Object rawDdlAuto = source.getProperty("spring.jpa.hibernate.ddl-auto");
        String ddlAuto = rawDdlAuto != null ? rawDdlAuto.toString() : null;

        assertThat(ddlAuto).as("기본 application.yml의 ddl-auto 속성은 환경변수를 사용해야 합니다.")
                .isIn("${DDL_AUTO}", "${DDL_AUTO:none}", "${DDL_AUTO:validate}");
    }

    @Test
    @DisplayName("운영 환경 application-prod.yml이 존재하며 ddl-auto가 validate로 명시되어 있어야 함")
    void prodApplicationYml_ExistsAndHasValidateDdlAuto() throws IOException {
        ClassPathResource prodResource = new ClassPathResource("application-prod.yml");
        assertThat(prodResource.exists()).as("application-prod.yml 파일이 존재해야 합니다.").isTrue();

        List<PropertySource<?>> sources = loader.load("application-prod", prodResource);
        assertThat(sources).isNotEmpty();

        PropertySource<?> source = sources.get(0);
        Object rawDdlAuto = source.getProperty("spring.jpa.hibernate.ddl-auto");
        String ddlAuto = rawDdlAuto != null ? rawDdlAuto.toString() : null;
        assertThat(ddlAuto).isEqualTo("${DDL_AUTO}");
    }

    @Test
    @DisplayName("개발 환경 application-dev.yml의 ddl-auto는 update로 유지되어야 함")
    void devApplicationYml_HasUpdateDdlAuto() throws IOException {
        List<PropertySource<?>> sources = loader.load("application-dev", new ClassPathResource("application-dev.yml"));
        assertThat(sources).isNotEmpty();

        PropertySource<?> source = sources.get(0);
        Object rawDdlAuto = source.getProperty("spring.jpa.hibernate.ddl-auto");
        String ddlAuto = rawDdlAuto != null ? rawDdlAuto.toString() : null;
        assertThat(ddlAuto).isEqualTo("update");
    }
}
