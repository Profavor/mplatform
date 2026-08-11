package com.classification.domain_system.service;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.classification.domain_system.repository.RecordRepository;

import java.util.UUID;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordIndexServiceTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private RecordIndexService recordIndexService;

    private Record mockRecord;

    @BeforeEach
    void setUp() {
        mockRecord = new Record();
        mockRecord.setId(UUID.randomUUID());
        mockRecord.setData("{\"name\":\"Test Item\"}");
        mockRecord.setSearchableData("Test Item");

        Domain domain = new Domain();
        domain.setId(UUID.randomUUID());

        ClassificationNode node = new ClassificationNode();
        node.setId(UUID.randomUUID());
        node.setDomain(domain);

        mockRecord.setNode(node);
    }

    @Test
    void testIndexRecord_Success() {
        when(elasticsearchOperations.save(any(RecordDocument.class))).thenReturn(new RecordDocument());

        recordIndexService.indexRecord(mockRecord);

        verify(elasticsearchOperations, times(1)).save(any(RecordDocument.class));
    }

    @Test
    void testIndexRecord_ElasticsearchFailure_ShouldSwallowException() {
        when(elasticsearchOperations.save(any(RecordDocument.class))).thenThrow(new RuntimeException("ES Down"));

        // Should not throw exception, just log warning
        recordIndexService.indexRecord(mockRecord);

        verify(elasticsearchOperations, times(1)).save(any(RecordDocument.class));
    }

    @Test
    void testDeleteRecord_Success() {
        String recordId = UUID.randomUUID().toString();
        
        doReturn(null).when(elasticsearchOperations).delete(eq(recordId), eq(RecordDocument.class));

        recordIndexService.deleteRecord(recordId);

        verify(elasticsearchOperations, times(1)).delete(eq(recordId), eq(RecordDocument.class));
    }
}
