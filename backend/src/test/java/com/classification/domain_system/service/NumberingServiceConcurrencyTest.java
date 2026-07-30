package com.classification.domain_system.service;

import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.DomainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class NumberingServiceConcurrencyTest {

    @Autowired
    private NumberingService numberingService;

    @Autowired
    private DomainRepository domainRepository;

    @Test
    public void testConcurrentIssueNextCode() throws InterruptedException {
        // Setup
        Domain domain = new Domain();
        domain.setName(Map.of("ko", "Test Domain"));
        domain.setNumberingPattern("ITEM-{SEQ:4}");
        domain.setCurrentSequence(0L);
        Domain savedDomain = domainRepository.save(domain);

        int numberOfThreads = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

        List<String> generatedCodes = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    String code = numberingService.issueNextCode(savedDomain.getId());
                    generatedCodes.add(code);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Start all threads at once
        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);

        // Verify
        assertEquals(numberOfThreads, generatedCodes.size(), "All threads should generate a code");
        long uniqueCount = generatedCodes.stream().distinct().count();
        assertEquals(numberOfThreads, uniqueCount, "There should be no duplicate codes");

        Domain updatedDomain = domainRepository.findById(savedDomain.getId()).orElseThrow();
        assertEquals(numberOfThreads, updatedDomain.getCurrentSequence(), "Sequence should be exactly " + numberOfThreads);
        
        executorService.shutdown();
    }
}
