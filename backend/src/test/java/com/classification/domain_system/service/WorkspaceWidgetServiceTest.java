package com.classification.domain_system.service;

import com.classification.domain_system.dto.WorkspaceWidgetDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkspaceWidgetServiceTest {

    private WorkspaceWidgetService widgetService;

    @BeforeEach
    void setUp() {
        widgetService = new WorkspaceWidgetService();
    }

    @Test
    @DisplayName("getUserWidgets: 기본 거버넌스 대시보드 위젯 목록 조회")
    void testGetUserWidgets() {
        List<WorkspaceWidgetDto.WidgetItem> list = widgetService.getUserWidgets("user_100");

        assertThat(list).hasSize(4);
        assertThat(list.get(0).getWidgetId()).isEqualTo("WDG-DQ-01");
        assertThat(list.get(0).isEnabled()).isTrue();
    }

    @Test
    @DisplayName("saveUserWidgets: 사용자 정의 위젯 배치 저장")
    void testSaveUserWidgets() {
        List<WorkspaceWidgetDto.WidgetItem> customWidgets = List.of(
                WorkspaceWidgetDto.WidgetItem.builder()
                        .widgetId("WDG-DQ-01")
                        .title("실시간 DQ 품질 지수")
                        .enabled(false)
                        .orderIndex(2)
                        .build()
        );

        List<WorkspaceWidgetDto.WidgetItem> saved = widgetService.saveUserWidgets("user_100", customWidgets);

        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).isEnabled()).isFalse();
    }
}
