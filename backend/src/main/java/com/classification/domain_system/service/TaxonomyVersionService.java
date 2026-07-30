package com.classification.domain_system.service;

import com.classification.domain_system.entity.TaxonomyVersion;
import com.classification.domain_system.repository.TaxonomyVersionRepository;
// import com.classification.domain_system.repository.ClassificationNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxonomyVersionService {

    private final TaxonomyVersionRepository taxonomyVersionRepository;

    @Transactional
    public TaxonomyVersion createSnapshot(UUID domainId, String label, String publishedBy) {
        TaxonomyVersion version = new TaxonomyVersion();
        version.setDomainId(domainId);
        version.setVersionLabel(label);
        version.setPublishedBy(publishedBy);
        // Normally we would query ClassificationNodeRepository, convert to JSON, and save.
        version.setSnapshotData("[]"); // Mock snapshot data
        
        return taxonomyVersionRepository.save(version);
    }

    @Transactional(readOnly = true)
    public List<TaxonomyVersion> getVersions(UUID domainId) {
        return taxonomyVersionRepository.findByDomainIdOrderByCreatedAtDesc(domainId);
    }

    @Transactional(readOnly = true)
    public String getSnapshotData(UUID versionId) {
        return taxonomyVersionRepository.findById(versionId)
                .map(TaxonomyVersion::getSnapshotData)
                .orElse(null);
    }
}
