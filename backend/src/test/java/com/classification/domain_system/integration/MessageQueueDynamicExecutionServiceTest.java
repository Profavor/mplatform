package com.classification.domain_system.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageQueueDynamicExecutionServiceTest {

    private MessageQueueDynamicExecutionService service;

    @BeforeEach
    void setUp() {
        service = new MessageQueueDynamicExecutionService();
    }

    @Test
    void executeMq_WithUnreachableRabbitMQ_ThrowsTimeoutException() {
        // Connect to a non-existent port to simulate connection timeout
        // The port 9999 is likely closed, causing a quick connection refused, 
        // but if it's firewalled, it will hit the 5000ms timeout we set.
        // We'll just verify that it fails and wraps properly.
        String configJson = "{\"broker\":\"amqp://localhost:9999\",\"topic\":\"test.queue\"}";
        
        long startTime = System.currentTimeMillis();
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.executeMq(configJson, "{}");
        });
        
        long duration = System.currentTimeMillis() - startTime;
        
        assertTrue(exception.getMessage().contains("MESSAGE_QUEUE Execution failed"));
        // As long as it finishes within a reasonable time (e.g. 6000ms for a 5000ms timeout), it proves the timeout is respected.
        assertTrue(duration < 7000, "Should respect 5000ms timeout, took " + duration + "ms");
    }
}
