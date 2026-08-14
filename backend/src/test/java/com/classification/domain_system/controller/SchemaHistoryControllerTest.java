package com.classification.domain_system.controller;

import com.classification.domain_system.dto.PageResponse;
import com.classification.domain_system.entity.SchemaHistory;
import com.classification.domain_system.repository.SchemaHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchemaHistoryControllerTest {

    @Mock
    private SchemaHistoryRepository historyRepository;

    @InjectMocks
    private SchemaHistoryController schemaHistoryController;

    private SchemaHistory createMockHistory(UUID domainId) {
        SchemaHistory history = new SchemaHistory();
        history.setId(UUID.randomUUID());
        history.setDomainId(domainId);
        history.setTargetType("FIELD");
        history.setTargetId(UUID.randomUUID());
        history.setAction("CREATE");
        history.setAfterData("{\"name\":\"test_field\"}");
        history.setChangedBy("admin");
        history.setChangedAt(LocalDateTime.now());
        return history;
    }

    @Test
    @DisplayName("도메인별 스키마 히스토리 페이징 조회 성공")
    void testGetSchemaHistory_Success() {
        UUID domainId = UUID.randomUUID();
        SchemaHistory history = createMockHistory(domainId);
        Page<SchemaHistory> page = new PageImpl<>(List.of(history));

        when(historyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ResponseEntity<PageResponse<SchemaHistory>> response = schemaHistoryController.getSchemaHistory(
                domainId, "FIELD", "CREATE", 0, 20
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().content().size());
        assertEquals("FIELD", response.getBody().content().get(0).getTargetType());
    }

    @Test
    @DisplayName("ID로 스키마 히스토리 단건 조회 성공")
    void testGetSchemaHistoryById_Success() {
        UUID historyId = UUID.randomUUID();
        SchemaHistory history = createMockHistory(UUID.randomUUID());
        history.setId(historyId);

        when(historyRepository.findById(historyId)).thenReturn(Optional.of(history));

        ResponseEntity<SchemaHistory> response = schemaHistoryController.getSchemaHistoryById(historyId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(historyId, response.getBody().getId());
    }

    @Test
    @DisplayName("존재하지 않는 스키마 히스토리 단건 조회 시 404 반환")
    void testGetSchemaHistoryById_NotFound() {
        UUID historyId = UUID.randomUUID();
        when(historyRepository.findById(historyId)).thenReturn(Optional.empty());

        ResponseEntity<SchemaHistory> response = schemaHistoryController.getSchemaHistoryById(historyId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}
