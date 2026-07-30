package com.classification.domain_system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class CdcEventPublisherServiceTest {

    @InjectMocks
    private CdcEventPublisherService cdcEventPublisherService;

    @Test
    void testPublishMasterDataChangedEvent() {
        assertDoesNotThrow(() -> {
            cdcEventPublisherService.publishMasterDataChangedEvent(UUID.randomUUID(), "UPDATE", "{}");
        });
    }
}
