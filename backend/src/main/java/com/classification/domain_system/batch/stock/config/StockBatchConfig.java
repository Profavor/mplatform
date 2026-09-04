package com.classification.domain_system.batch.stock.config;

import com.classification.domain_system.batch.stock.dto.StockApiRawItem;
import com.classification.domain_system.batch.stock.listener.StockJobExecutionListener;
import com.classification.domain_system.batch.stock.processor.StockItemProcessor;
import com.classification.domain_system.batch.stock.reader.StockMarketApiItemReader;
import com.classification.domain_system.batch.stock.writer.StockRecordItemWriter;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.service.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StockBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RecordRepository recordRepository;
    private final RecordHistoryRepository recordHistoryRepository;
    private final com.classification.domain_system.repository.FieldDefinitionRepository fieldDefinitionRepository;
    private final DomainRepository domainRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final RecordService recordService;
    private final StockJobExecutionListener stockJobExecutionListener;

    @Bean
    public Job stockMarketIngestionJob(Step stockMarketIngestionStep) {
        return new JobBuilder("stockMarketIngestionJob", jobRepository)
                .listener(stockJobExecutionListener)
                .start(stockMarketIngestionStep)
                .build();
    }

    @Bean
    public Step stockMarketIngestionStep() {
        return new StepBuilder("stockMarketIngestionStep", jobRepository)
                .<StockApiRawItem, Record>chunk(100, transactionManager)
                .reader(stockMarketApiItemReader(null))
                .processor(stockItemProcessor())
                .writer(stockRecordItemWriter(null))
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    @StepScope
    public StockMarketApiItemReader stockMarketApiItemReader(
            @Value("#{jobParameters['markets']}") String marketsParam) {
        List<String> markets = (marketsParam != null && !marketsParam.isBlank())
                ? List.of(marketsParam.split(","))
                : List.of("KOSPI", "KOSDAQ", "KONEX", "NASDAQ", "NYSE");
        return new StockMarketApiItemReader(new RestTemplate(), markets);
    }

    @Bean
    @StepScope
    public StockItemProcessor stockItemProcessor() {
        Domain domain = domainRepository.findBySpecializedCategory("STOCK").orElse(null);
        Map<String, ClassificationNode> nodeMap = new HashMap<>();
        ClassificationNode defaultNode = null;

        if (domain != null) {
            List<ClassificationNode> nodes = nodeRepository.findByDomain_IdAndIsDeletedFalse(domain.getId());
            for (ClassificationNode n : nodes) {
                String path = n.getPath() != null ? n.getPath().toUpperCase() : "";
                Map<String, String> nameMap = n.getName();
                String koName = nameMap != null && nameMap.get("ko") != null ? nameMap.get("ko").toUpperCase() : "";

                if (path.contains("KOSPI") || koName.contains("KOSPI")) {
                    nodeMap.put("KOSPI", n);
                } else if (path.contains("KOSDAQ") || koName.contains("KOSDAQ")) {
                    nodeMap.put("KOSDAQ", n);
                } else if (path.contains("KONEX") || koName.contains("KONEX")) {
                    nodeMap.put("KONEX", n);
                } else if (path.contains("미국") || path.contains("US") || koName.contains("미국")) {
                    nodeMap.put("US_MARKET", n);
                }
            }
            defaultNode = nodes.stream().filter(n -> n.getDepth() != null && n.getDepth() > 0).findFirst()
                    .orElse(nodes.isEmpty() ? null : nodes.get(0));
        }

        return new StockItemProcessor(recordService, nodeMap, defaultNode);
    }

    @Bean
    @StepScope
    public StockRecordItemWriter stockRecordItemWriter(
            @Value("#{jobParameters['requestedBy']}") String requestedBy) {
        return new StockRecordItemWriter(recordRepository, recordHistoryRepository, fieldDefinitionRepository, requestedBy != null ? requestedBy : "SPRING_BATCH");
    }
}
