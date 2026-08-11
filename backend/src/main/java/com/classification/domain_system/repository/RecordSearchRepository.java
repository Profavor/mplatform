package com.classification.domain_system.repository;

import com.classification.domain_system.entity.RecordDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordSearchRepository extends ElasticsearchRepository<RecordDocument, String> {
    List<RecordDocument> findByDomainId(String domainId);
    List<RecordDocument> findBySearchableDataContaining(String text);
}
