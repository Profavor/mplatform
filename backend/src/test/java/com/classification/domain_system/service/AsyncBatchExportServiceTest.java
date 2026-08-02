package com.classification.domain_system.service;

import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AsyncBatchExportServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private com.classification.domain_system.repository.FieldDefinitionRepository fieldDefinitionRepository;

    private AsyncBatchExportService asyncBatchExportService;

    @BeforeEach
    void setUp() {
        asyncBatchExportService = new AsyncBatchExportService(recordRepository, fieldDefinitionRepository);
    }

    @Test
    void testDownloadTaskFile() {
        byte[] bytes = asyncBatchExportService.downloadTaskFile("test-task-123");
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }
}
