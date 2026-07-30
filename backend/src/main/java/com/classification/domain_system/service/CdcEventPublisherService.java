package com.classification.domain_system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "mdm.cdc.enabled", havingValue = "true", matchIfMissing = false)
public class CdcEventPublisherService {

    // Normally would inject KafkaTemplate here
    
    public void publishMasterDataChangedEvent(UUID recordId, String action, String payload) {
        log.info("CDC Event: Publishing MasterDataChangedEvent for recordId: {}, action: {}", recordId, action);
        // Code to publish event to kafka topic
    }
}
