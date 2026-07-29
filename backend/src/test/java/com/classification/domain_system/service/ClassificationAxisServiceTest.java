package com.classification.domain_system.service;

import com.classification.domain_system.dto.ClassificationAxisRequest;
import com.classification.domain_system.dto.ClassificationAxisResponse;
import com.classification.domain_system.entity.ClassificationAxis;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ClassificationAxisRepository;
import com.classification.domain_system.repository.DomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassificationAxisServiceTest {

    @Mock
    private ClassificationAxisRepository axisRepository;

    @Mock
    private DomainRepository domainRepository;

    @InjectMocks
    private ClassificationAxisService axisService;

    private UUID domainId;
    private Domain domain;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        domain = new Domain();
        domain.setId(domainId);
    }

    @Test
    @DisplayName("신규 분류축을 정상적으로 생성한다")
    void createAxis_Success() {
        ClassificationAxisRequest req = new ClassificationAxisRequest();
        req.setAxisCode("DEPT");
        req.setName(Map.of("ko", "부서 분류축", "en", "Department Axis"));
        req.setDescription("부서 조직도 기준 분류축");
        req.setIsDefault(false);
        req.setSortOrder(1);

        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(axisRepository.existsByDomainIdAndAxisCode(domainId, "DEPT")).thenReturn(false);
        when(axisRepository.save(any(ClassificationAxis.class))).thenAnswer(invocation -> {
            ClassificationAxis saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        ClassificationAxisResponse res = axisService.createAxis(domainId, req);

        assertThat(res).isNotNull();
        assertThat(res.getAxisCode()).isEqualTo("DEPT");
        assertThat(res.getDomainId()).isEqualTo(domainId);
        assertThat(res.getIsDefault()).isFalse();
    }

    @Test
    @DisplayName("중복된 axisCode 등록 시 BusinessException 예외를 발생시킨다")
    void createAxis_DuplicateCode_ThrowsException() {
        ClassificationAxisRequest req = new ClassificationAxisRequest();
        req.setAxisCode("DEPT");

        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(axisRepository.existsByDomainIdAndAxisCode(domainId, "DEPT")).thenReturn(true);

        assertThatThrownBy(() -> axisService.createAxis(domainId, req))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("기본 분류축이 없으면 자동 생성한다")
    void getOrCreateDefaultAxis_CreatesIfMissing() {
        when(axisRepository.findByDomainIdAndIsDefaultTrue(domainId)).thenReturn(Optional.empty());
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(axisRepository.save(any(ClassificationAxis.class))).thenAnswer(invocation -> {
            ClassificationAxis saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        ClassificationAxis defaultAxis = axisService.getOrCreateDefaultAxis(domainId);

        assertThat(defaultAxis).isNotNull();
        assertThat(defaultAxis.getAxisCode()).isEqualTo("DEFAULT");
        assertThat(defaultAxis.getIsDefault()).isTrue();
    }

    @Test
    @DisplayName("도메인의 모든 분류축 목록을 조회한다")
    void getAxesByDomain_ReturnsList() {
        ClassificationAxis axis1 = new ClassificationAxis();
        axis1.setId(UUID.randomUUID());
        axis1.setAxisCode("DEFAULT");

        ClassificationAxis axis2 = new ClassificationAxis();
        axis2.setId(UUID.randomUUID());
        axis2.setAxisCode("EMPLOYMENT");

        when(axisRepository.findByDomainIdOrderBySortOrderAsc(domainId)).thenReturn(List.of(axis1, axis2));

        List<ClassificationAxisResponse> list = axisService.getAxesByDomain(domainId);

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getAxisCode()).isEqualTo("DEFAULT");
        assertThat(list.get(1).getAxisCode()).isEqualTo("EMPLOYMENT");
    }

    @Test
    @DisplayName("존재하지 않는 분류축 삭제 시 ResourceNotFoundException 예외를 발생시킨다")
    void deleteAxis_NotFound_ThrowsException() {
        UUID axisId = UUID.randomUUID();
        when(axisRepository.findById(axisId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> axisService.deleteAxis(axisId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
