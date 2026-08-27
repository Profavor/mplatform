package com.classification.domain_system.repository;

import com.classification.domain_system.entity.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CustomRecordRepositoryImpl 단위 테스트.
 * H2가 PostgreSQL jsonb 연산자(@>)를 지원하지 않으므로
 * EntityManager를 Mock 처리하여 SQL 구성 및 파라미터 바인딩 로직을 검증한다.
 *
 * 참고: query/countQuery 둘 다 같은 Mock 인스턴스이므로
 *       setParameter 호출 횟수는 times(2) (query + countQuery) 기준으로 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class CustomRecordRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private CustomRecordRepositoryImpl customRecordRepository;

    private UUID nodeId;
    private UUID domainId;

    @BeforeEach
    void setUp() {
        nodeId = UUID.randomUUID();
        domainId = UUID.randomUUID();

        // query와 countQuery 모두 같은 mock 반환
        when(entityManager.createNativeQuery(anyString(), eq(Record.class))).thenReturn(query);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(0L);
        when(query.getResultList()).thenReturn(List.of());
    }

    // ──────────────────────────────────────────────
    // findDynamicRecords 테스트
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("EQ 조건 검색 시 jsonb @> 조건이 SQL에 포함되어야 한다")
    void findDynamicRecords_EQ_GeneratesJsonbContainsClause() {
        Map<String, String> params = new HashMap<>();
        params.put("TICKER", "006800");

        customRecordRepository.findDynamicRecords(
                List.of(nodeId), null, params, PageRequest.of(0, 10));

        verify(entityManager).createNativeQuery(contains("@>"), eq(Record.class));
    }

    @Test
    @DisplayName("검색 파라미터가 없으면 기본 WHERE 절만 생성된다")
    void findDynamicRecords_NoParams_GeneratesBaseQuery() {
        Map<String, String> params = new HashMap<>();

        customRecordRepository.findDynamicRecords(
                List.of(nodeId), null, params, PageRequest.of(0, 10));

        verify(entityManager).createNativeQuery(contains("node_id IN"), eq(Record.class));
        verify(entityManager).createNativeQuery(contains("node_id IN"));
    }

    @Test
    @DisplayName("EQ 조건 검색 시 대소문자 무관 파라미터(searchValStrLower)가 query/countQuery 각각 바인딩되어야 한다")
    void findDynamicRecords_EQ_BindsBothCaseParams() {
        Map<String, String> params = new HashMap<>();
        params.put("TICKER", "KT");

        customRecordRepository.findDynamicRecords(
                List.of(nodeId), null, params, PageRequest.of(0, 10));

        // query + countQuery 양쪽에 각 1회씩 → 총 2회
        verify(query, times(2)).setParameter(eq("searchValStr0"), anyString());
        verify(query, times(2)).setParameter(eq("searchValStrLower0"), anyString());
    }

    @Test
    @DisplayName("EQ 조건 검색 시 다국어 및 부분 문자열 매칭용 searchValLike 파라미터가 바인딩되어야 한다")
    void findDynamicRecords_MultilingualEQ_BindsLikeParams() {
        Map<String, String> params = new HashMap<>();
        params.put("stock_name", "삼성전자");

        customRecordRepository.findDynamicRecords(
                List.of(nodeId), null, params, PageRequest.of(0, 10));

        verify(query, times(2)).setParameter(eq("searchValLike0"), eq("삼성전자"));
    }

    @Test
    @DisplayName("STARTS_WITH 조건 검색 시 앞단어 와일드카드(val%) 파라미터가 바인딩되어야 한다")
    void findDynamicRecords_StartsWith_BindsLikeParams() {
        Map<String, String> params = new HashMap<>();
        params.put("stock_name", "삼성");
        params.put("op_stock_name", "STARTS_WITH");

        customRecordRepository.findDynamicRecords(
                List.of(nodeId), null, params, PageRequest.of(0, 10));

        verify(query, times(2)).setParameter(eq("searchValLike0"), eq("삼성%"));
    }

    @Test
    @DisplayName("ENDS_WITH 조건 검색 시 뒷단어 와일드카드(%val) 파라미터가 바인딩되어야 한다")
    void findDynamicRecords_EndsWith_BindsLikeParams() {
        Map<String, String> params = new HashMap<>();
        params.put("stock_name", "전자");
        params.put("op_stock_name", "ENDS_WITH");

        customRecordRepository.findDynamicRecords(
                List.of(nodeId), null, params, PageRequest.of(0, 10));

        verify(query, times(2)).setParameter(eq("searchValLike0"), eq("%전자"));
    }

    @Test
    @DisplayName("CONTAINS 조건 검색 시 ILIKE 조건과 searchValLike 파라미터가 정상 생성 및 바인딩되어야 한다")
    void findDynamicRecords_Contains_GeneratesLikeClauseAndBindsParams() {
        Map<String, String> params = new HashMap<>();
        params.put("stock_code", "9019");
        params.put("op_stock_code", "CONTAINS");

        customRecordRepository.findDynamicRecords(
                List.of(nodeId), null, params, PageRequest.of(0, 10));

        verify(entityManager).createNativeQuery(contains("ILIKE"), eq(Record.class));
        verify(query, times(2)).setParameter(eq("searchValLike0"), eq("%9019%"));
    }

    @Test
    @DisplayName("숫자 값 EQ 검색 시 숫자형 파라미터(searchValNum)가 query/countQuery 각각 바인딩되어야 한다")
    void findDynamicRecords_NumericEQ_BindsNumericParams() {
        Map<String, String> params = new HashMap<>();
        params.put("price", "100");

        customRecordRepository.findDynamicRecords(
                List.of(nodeId), null, params, PageRequest.of(0, 10));

        verify(query, times(2)).setParameter(eq("searchValNum0"), anyString());
        verify(query, times(2)).setParameter(eq("searchValNumLower0"), anyString());
    }

    @Test
    @DisplayName("BETWEEN 조건 검색 시 Min/Max 파라미터가 바인딩되어야 한다")
    void findDynamicRecords_BETWEEN_BindsMinMaxParams() {
        Map<String, String> params = new HashMap<>();
        params.put("price", "100");
        params.put("op_price", "BETWEEN");
        params.put("price_max", "200");

        customRecordRepository.findDynamicRecords(
                List.of(nodeId), null, params, PageRequest.of(0, 10));

        // query + countQuery 양쪽 → times(2)
        verify(query, times(2)).setParameter(eq("searchValMin0"), eq(100.0));
        verify(query, times(2)).setParameter(eq("searchValMax0"), eq(200.0));
    }

    @Test
    @DisplayName("GT 조건 검색 시 searchVal 파라미터가 바인딩되어야 한다")
    void findDynamicRecords_GT_BindsSingleParam() {
        Map<String, String> params = new HashMap<>();
        params.put("price", "50");
        params.put("op_price", "GT");

        customRecordRepository.findDynamicRecords(
                List.of(nodeId), null, params, PageRequest.of(0, 10));

        verify(query, times(2)).setParameter(eq("searchVal0"), eq(50.0));
    }

    @Test
    @DisplayName("GTE, LT, LTE 연산자 검색 시 수치 비교 SQL 및 searchVal 파라미터가 바인딩되어야 한다")
    void findDynamicRecords_NumericComparisonOperators_BindsParams() {
        Map<String, String> params = new HashMap<>();
        params.put("price", "100");
        params.put("op_price", "GTE");

        customRecordRepository.findDynamicRecords(
                List.of(nodeId), null, params, PageRequest.of(0, 10));

        verify(entityManager).createNativeQuery(contains(">="), eq(Record.class));
        verify(query, times(2)).setParameter(eq("searchVal0"), eq(100.0));
    }

    @Test
    @DisplayName("페이징 적용 시 limit/offset 파라미터가 바인딩되어야 한다")
    void findDynamicRecords_Paged_BindsLimitOffset() {
        Map<String, String> params = new HashMap<>();

        customRecordRepository.findDynamicRecords(
                List.of(nodeId), null, params, PageRequest.of(2, 10));

        verify(query).setParameter(eq("limit"), eq(10));
        verify(query).setParameter(eq("offset"), eq(20L));
    }

    @Test
    @DisplayName("op_ 키와 _max 키는 검색 조건으로 무시된다")
    void findDynamicRecords_OpAndMaxKeys_AreIgnored() {
        Map<String, String> params = new HashMap<>();
        params.put("op_price", "BETWEEN");
        params.put("price_max", "200");

        customRecordRepository.findDynamicRecords(
                List.of(nodeId), null, params, PageRequest.of(0, 10));

        // op_, _max 키는 조건에 추가되지 않으므로 @> 절 없음
        verify(entityManager, never()).createNativeQuery(contains("@>"), eq(Record.class));
    }

    // ──────────────────────────────────────────────
    // findDynamicRecordsByDomain 테스트
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("도메인 검색 시 domain_id JOIN 절이 SQL에 포함되어야 한다")
    void findDynamicRecordsByDomain_GeneratesDomainJoinClause() {
        Map<String, String> params = new HashMap<>();

        customRecordRepository.findDynamicRecordsByDomain(
                domainId, params, PageRequest.of(0, 10));

        verify(entityManager).createNativeQuery(contains("domain_id"), eq(Record.class));
    }

    @Test
    @DisplayName("도메인 EQ 검색 시 대소문자 무관 파라미터가 query/countQuery 각각 바인딩되어야 한다")
    void findDynamicRecordsByDomain_EQ_BindsBothCaseParams() {
        Map<String, String> params = new HashMap<>();
        params.put("TICKER", "KT");

        customRecordRepository.findDynamicRecordsByDomain(
                domainId, params, PageRequest.of(0, 10));

        verify(query, times(2)).setParameter(eq("searchValStr0"), anyString());
        verify(query, times(2)).setParameter(eq("searchValStrLower0"), anyString());
    }

    @Test
    @DisplayName("검색 시 safeKey에 대한 _mask_ 필드 조건이 SQL에 포함되어 마스킹 정보로 조회가 가능해야 한다")
    void findDynamicRecords_IncludesMaskFieldInSql() {
        Map<String, String> params = new HashMap<>();
        params.put("resident_number", "900101");
        params.put("op_resident_number", "STARTS_WITH");

        customRecordRepository.findDynamicRecords(
                List.of(nodeId), null, params, PageRequest.of(0, 10));

        verify(entityManager).createNativeQuery(contains("_mask_resident_number"), eq(Record.class));
    }
}

