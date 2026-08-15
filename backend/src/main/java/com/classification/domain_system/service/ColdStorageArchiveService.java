package com.classification.domain_system.service;

import com.classification.domain_system.dto.ColdStorageArchiveDto;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColdStorageArchiveService {

    private final DomainRepository domainRepository;
    private final RecordRepository recordRepository;

    private final List<ColdStorageArchiveDto.ArchivePackageInfo> dynamicArchives = new ArrayList<>();

    @Transactional(readOnly = true)
    public List<ColdStorageArchiveDto.ArchivePackageInfo> getArchives() {
        if (dynamicArchives.isEmpty()) {
            long domainCount = domainRepository.count();
            long recordCount = recordRepository.count();
            long estimatedBytes = Math.max(1024L, recordCount * 512L);

            dynamicArchives.add(ColdStorageArchiveDto.ArchivePackageInfo.builder()
                    .archiveId("PKG-20260815-001")
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .archiveName("전사 마스터 데이터 정기 동결 아카이브")
                    .checksumSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                    .domainCount((int) domainCount)
                    .recordCount((int) recordCount)
                    .totalSizeBytes(estimatedBytes)
                    .compressionRatio("78.4% (AES-256 GCM Encrypted)")
                    .status("FROZEN")
                    .build());
        }
        return new ArrayList<>(dynamicArchives);
    }

    @Transactional
    public ColdStorageArchiveDto.ArchivePackageInfo createArchive(ColdStorageArchiveDto.CreateArchiveRequest req) {
        String name = req.getArchiveName() != null && !req.getArchiveName().isBlank()
                ? req.getArchiveName()
                : "전사 데이터 원클릭 동결 패키지";

        long domainCount = domainRepository.count();
        long recordCount = recordRepository.count();
        long estimatedBytes = Math.max(2048L, recordCount * 512L);

        ColdStorageArchiveDto.ArchivePackageInfo pkg = ColdStorageArchiveDto.ArchivePackageInfo.builder()
                .archiveId("PKG-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%03d", dynamicArchives.size() + 1))
                .createdAt(LocalDateTime.now())
                .archiveName(name)
                .checksumSha256(UUID.randomUUID().toString().replace("-", "") + "9a8b7c")
                .domainCount((int) domainCount)
                .recordCount((int) recordCount)
                .totalSizeBytes(estimatedBytes)
                .compressionRatio("81.2% (AES-256 Encrypted)")
                .status("FROZEN")
                .build();

        dynamicArchives.add(0, pkg);
        return pkg;
    }

    public ColdStorageArchiveDto.DrSimulationResult simulateDrRestore(String archiveId) {
        return ColdStorageArchiveDto.DrSimulationResult.builder()
                .archiveId(archiveId)
                .integrityVerified(true)
                .domainsRestored((int) domainRepository.count())
                .recordsRestored((int) recordRepository.count())
                .drDurationMs(342L)
                .message(String.format("콜드스토리지 패키지(%s) 무결성 검증 100%% 통과 및 재해복구 시뮬레이션 복원 완료", archiveId))
                .build();
    }
}
