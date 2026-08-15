package com.classification.domain_system.service;

import com.classification.domain_system.dto.WorkspaceWidgetDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceWidgetService {

    private final Map<String, List<WorkspaceWidgetDto.WidgetItem>> userWidgets = new ConcurrentHashMap<>();

    public List<WorkspaceWidgetDto.WidgetItem> getUserWidgets(String userId) {
        String key = userId != null ? userId : "default_user";
        return userWidgets.computeIfAbsent(key, k -> {
            List<WorkspaceWidgetDto.WidgetItem> defaultList = new ArrayList<>();
            defaultList.add(WorkspaceWidgetDto.WidgetItem.builder()
                    .widgetId("WDG-DQ-01")
                    .title("실시간 DQ 품질 지수 현황")
                    .description("도메인별 데이터 품질 지수 및 IQR 이상치 요약")
                    .category("QUALITY")
                    .enabled(true)
                    .orderIndex(1)
                    .gridSpan(1)
                    .build());

            defaultList.add(WorkspaceWidgetDto.WidgetItem.builder()
                    .widgetId("WDG-APP-01")
                    .title("결재 대기 & SLA 에스컬레이션")
                    .description("미결 결재 문서 및 SLA 초과 임박 건수 모니터링")
                    .category("WORKFLOW")
                    .enabled(true)
                    .orderIndex(2)
                    .gridSpan(1)
                    .build());

            defaultList.add(WorkspaceWidgetDto.WidgetItem.builder()
                    .widgetId("WDG-SEC-01")
                    .title("제로트러스트 보안 이상 탐지")
                    .description("비인가 대량 조회 및 이상 접근 실시간 위협 지수")
                    .category("GOVERNANCE")
                    .enabled(true)
                    .orderIndex(3)
                    .gridSpan(1)
                    .build());

            defaultList.add(WorkspaceWidgetDto.WidgetItem.builder()
                    .widgetId("WDG-INT-01")
                    .title("연계 채널 헬스 & DLQ 모니터")
                    .description("외부 연계 시스템 실시간 상태 및 실패 큐 재시도 요약")
                    .category("INFRA")
                    .enabled(true)
                    .orderIndex(4)
                    .gridSpan(1)
                    .build());

            return defaultList;
        });
    }

    public List<WorkspaceWidgetDto.WidgetItem> saveUserWidgets(String userId, List<WorkspaceWidgetDto.WidgetItem> widgets) {
        String key = userId != null ? userId : "default_user";
        userWidgets.put(key, widgets != null ? widgets : Collections.emptyList());
        return userWidgets.get(key);
    }
}
