package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordLayoutDto;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassificationNodeLayoutTest {

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private DomainRepository domainRepository;

    @InjectMocks
    private ClassificationNodeService nodeService;

    @InjectMocks
    private DomainService domainService;

    private Domain domain;
    private ClassificationNode parentNode;
    private ClassificationNode childNode;
    private UUID domainId;
    private UUID parentNodeId;
    private UUID childNodeId;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        parentNodeId = UUID.randomUUID();
        childNodeId = UUID.randomUUID();

        domain = new Domain();
        domain.setId(domainId);
        domain.setName(Map.of("ko", "테스트 도메인", "en", "Test Domain"));

        parentNode = new ClassificationNode();
        parentNode.setId(parentNodeId);
        parentNode.setDomain(domain);
        parentNode.setName(Map.of("ko", "상위 분류", "en", "Parent Category"));
        parentNode.setPath("/테스트 도메인/상위 분류");
        parentNode.setDepth(1);

        childNode = new ClassificationNode();
        childNode.setId(childNodeId);
        childNode.setDomain(domain);
        childNode.setParent(parentNode);
        childNode.setName(Map.of("ko", "하위 분류", "en", "Child Category"));
        childNode.setPath("/테스트 도메인/상위 분류/하위 분류");
        childNode.setDepth(2);
    }

    @Test
    @DisplayName("노드 레이아웃 저장 및 직접 조회 성공")
    void testSaveAndGetNodeLayout() {
        // Arrange
        RecordLayoutDto dto = new RecordLayoutDto();
        dto.setCols(12);
        dto.setRowHeight(44);

        Map<String, Object> imgWidget = new HashMap<>();
        imgWidget.put("id", "w1");
        imgWidget.put("type", "IMAGE");
        imgWidget.put("fieldKey", "profile_image");
        imgWidget.put("w", 3);
        imgWidget.put("h", 4);
        imgWidget.put("x", 0);
        imgWidget.put("y", 0);

        Map<String, Object> editorWidget = new HashMap<>();
        editorWidget.put("id", "w2");
        editorWidget.put("type", "EDITOR");
        editorWidget.put("fieldKey", "description");
        editorWidget.put("w", 12);
        editorWidget.put("h", 8);
        editorWidget.put("x", 0);
        editorWidget.put("y", 4);

        dto.setWidgets(List.of(imgWidget, editorWidget));

        when(nodeRepository.findById(childNodeId)).thenReturn(Optional.of(childNode));
        when(nodeRepository.save(any(ClassificationNode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act - Save
        Map<String, Object> saved = nodeService.saveNodeLayout(domainId, childNodeId, dto);

        // Assert - Save
        assertNotNull(saved);
        assertEquals(12, saved.get("cols"));
        assertEquals(44, saved.get("rowHeight"));
        List<?> widgets = (List<?>) saved.get("widgets");
        assertEquals(2, widgets.size());

        // Act - Get
        Map<String, Object> fetched = nodeService.getNodeLayout(domainId, childNodeId);

        // Assert - Get
        assertEquals(12, fetched.get("cols"));
        assertEquals(44, fetched.get("rowHeight"));
    }

    @Test
    @DisplayName("자식 노드 레이아웃 미설정 시 부모 노드 레이아웃 상속")
    void testInheritParentNodeLayout() {
        // Arrange
        Map<String, Object> parentLayout = new HashMap<>();
        parentLayout.put("cols", 12);
        parentLayout.put("rowHeight", 50);
        parentLayout.put("widgets", List.of(Map.of("id", "pw1", "type", "FIELD", "w", 6, "h", 1)));
        parentNode.setDetailLayoutConfig(parentLayout);

        when(nodeRepository.findById(childNodeId)).thenReturn(Optional.of(childNode));

        // Act
        Map<String, Object> inheritedLayout = nodeService.getNodeLayout(domainId, childNodeId);

        // Assert
        assertNotNull(inheritedLayout);
        assertEquals(12, inheritedLayout.get("cols"));
        assertEquals(50, inheritedLayout.get("rowHeight"));
        assertEquals(1, ((List<?>) inheritedLayout.get("widgets")).size());
    }

    @Test
    @DisplayName("노드 레이아웃 미설정 시 도메인 레이아웃 상속")
    void testInheritDomainLayout() {
        // Arrange
        Map<String, Object> domainLayout = new HashMap<>();
        domainLayout.put("cols", 12);
        domainLayout.put("rowHeight", 48);
        domainLayout.put("widgets", List.of(Map.of("id", "dw1", "type", "FIELD", "w", 4, "h", 1)));
        domain.setDetailLayoutConfig(domainLayout);

        when(nodeRepository.findById(childNodeId)).thenReturn(Optional.of(childNode));

        // Act
        Map<String, Object> inheritedLayout = nodeService.getNodeLayout(domainId, childNodeId);

        // Assert
        assertNotNull(inheritedLayout);
        assertEquals(12, inheritedLayout.get("cols"));
        assertEquals(48, inheritedLayout.get("rowHeight"));
        assertEquals(1, ((List<?>) inheritedLayout.get("widgets")).size());
    }

    @Test
    @DisplayName("다국어 레이아웃 명칭(KO/EN) 저장 및 다중 템플릿 목록 조회 성공")
    void testSaveAndGetMultilingualLayouts() {
        // Arrange
        RecordLayoutDto dto = new RecordLayoutDto();
        dto.setActiveLayoutId("layout_compact");

        Map<String, Object> layout1 = new HashMap<>();
        layout1.put("id", "layout_default");
        layout1.put("name", Map.of("ko", "기본 그리드 뷰", "en", "Default Grid View"));
        layout1.put("isDefault", false);
        layout1.put("cols", 12);
        layout1.put("rowHeight", 42);
        layout1.put("widgets", List.of());

        Map<String, Object> layout2 = new HashMap<>();
        layout2.put("id", "layout_compact");
        layout2.put("name", Map.of("ko", "컴팩트 요약 뷰", "en", "Compact Summary View"));
        layout2.put("isDefault", true);
        layout2.put("cols", 12);
        layout2.put("rowHeight", 36);
        layout2.put("widgets", List.of(Map.of("id", "w1", "type", "FIELD", "w", 6, "h", 1)));

        dto.setLayouts(List.of(layout1, layout2));

        when(nodeRepository.findById(childNodeId)).thenReturn(Optional.of(childNode));
        when(nodeRepository.save(any(ClassificationNode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Map<String, Object> saved = nodeService.saveNodeLayout(domainId, childNodeId, dto);

        // Assert
        assertNotNull(saved);
        assertEquals("layout_compact", saved.get("activeLayoutId"));
        List<Map<String, Object>> layouts = (List<Map<String, Object>>) saved.get("layouts");
        assertEquals(2, layouts.size());

        Map<String, Object> savedLayout1 = layouts.get(0);
        Map<?, ?> name1 = (Map<?, ?>) savedLayout1.get("name");
        assertEquals("기본 그리드 뷰", name1.get("ko"));
        assertEquals("Default Grid View", name1.get("en"));

        Map<String, Object> savedLayout2 = layouts.get(1);
        Map<?, ?> name2 = (Map<?, ?>) savedLayout2.get("name");
        assertEquals("컴팩트 요약 뷰", name2.get("ko"));
        assertEquals("Compact Summary View", name2.get("en"));
    }
}
