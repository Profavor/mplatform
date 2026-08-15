package com.classification.domain_system.service;

import com.classification.domain_system.dto.ColdStorageArchiveDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ColdStorageArchiveServiceTest {

    private ColdStorageArchiveService archiveService;

    @BeforeEach
    void setUp() {
        archiveService = new ColdStorageArchiveService();
    }

    @Test
    @DisplayName("createArchive & getArchives: 전사 콜드스토리지 아카이브 동결 생성")
    void testCreateArchive() {
        ColdStorageArchiveDto.CreateArchiveRequest req = ColdStorageArchiveDto.CreateArchiveRequest.builder()
                .archiveName("2026 하반기 정기 백업 패키지")
                .encrypt(true)
                .build();

        ColdStorageArchiveDto.ArchivePackageInfo pkg = archiveService.createArchive(req);

        assertThat(pkg).isNotNull();
        assertThat(pkg.getArchiveId()).startsWith("PKG-");
        assertThat(pkg.getChecksumSha256()).isNotNull();

        List<ColdStorageArchiveDto.ArchivePackageInfo> list = archiveService.getArchives();
        assertThat(list).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("simulateDrRestore: DR 복원 시뮬레이션 및 SHA-256 무결성 검증")
    void testSimulateDrRestore() {
        ColdStorageArchiveDto.DrSimulationResult res = archiveService.simulateDrRestore("PKG-20260815-001");

        assertThat(res).isNotNull();
        assertThat(res.isIntegrityVerified()).isTrue();
        assertThat(res.getDomainsRestored()).isGreaterThan(0);
        assertThat(res.getRecordsRestored()).isGreaterThan(0);
    }
}
