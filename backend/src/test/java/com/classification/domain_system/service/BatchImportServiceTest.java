package com.classification.domain_system.service;

import com.classification.domain_system.entity.BatchJob;
import com.classification.domain_system.repository.BatchJobRepository;
import com.classification.domain_system.repository.StagingRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchImportServiceTest {

    @Mock
    private BatchJobRepository batchJobRepository;

    @Mock
    private StagingRecordRepository stagingRecordRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BatchImportService batchImportService;

    @Test
    void testCreateBatch() throws Exception {
        UUID domainId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        when(batchJobRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        BatchJob result = batchImportService.createBatch(
                domainId, nodeId, List.of(Map.of("key", "val")), "SYS", "admin");

        assertEquals("IMPORT", result.getJobType());
        assertEquals("QUEUED", result.getStatus());
        verify(stagingRecordRepository, times(1)).save(any());
    }
}
