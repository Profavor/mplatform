package com.classification.domain_system.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuDataInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private MenuDataInitializer menuDataInitializer;

    @Test
    @DisplayName("메뉴 데이터가 없을 때 시스템 기본 메뉴 트리를 SQL INSERT로 시딩한다")
    void initMenus_seedsDefaultMenuTreeWhenEmpty() {
        // given - menu 테이블이 비어있을 때
        given(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM menu"), eq(Long.class)))
                .willReturn(0L);
        given(jdbcTemplate.queryForObject(eq("SELECT id FROM menu WHERE path = '/admin'"), eq(Long.class)))
                .willReturn(7L);
        given(jdbcTemplate.queryForList(eq("SELECT id, path FROM menu")))
                .willReturn(java.util.List.of());

        // when
        menuDataInitializer.initMenus();

        // then - 7 top-level INSERT (no params) + 10 admin sub-menu INSERT (with adminId param)
        verify(jdbcTemplate, times(7)).update(contains("INSERT INTO menu"));
        verify(jdbcTemplate, times(10)).update(contains("INSERT INTO menu"), any(Long.class));
    }

    @Test
    @DisplayName("메뉴 데이터가 이미 있지만 하위 메뉴가 없을 경우 새로 추가한다")
    void initMenus_addsApprovalMonitorSubMenuWhenMissing() {
        // given - menu 테이블에 데이터가 있고, '/admin' parent가 있지만 하위 메뉴가 없을 때
        given(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM menu"), eq(Long.class)))
                .willReturn(15L);
        given(jdbcTemplate.queryForList(eq("SELECT id FROM menu WHERE path = '/admin' AND parent_id IS NULL"), eq(Long.class)))
                .willReturn(java.util.List.of(7L));
        given(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString(), eq(7L)))
                .willReturn(0);
        given(jdbcTemplate.queryForList(anyString(), eq(Long.class), anyString(), eq(7L)))
                .willReturn(java.util.List.of(99L));
        given(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(99L), anyString()))
                .willReturn(0);

        // when
        menuDataInitializer.initMenus();

        // then - 10개 하위 메뉴 INSERT 검증
        verify(jdbcTemplate, atLeastOnce()).update(contains("INSERT INTO menu"), any(), any(), any(), eq(7L), any());
    }
}
