# 7. 예시 시나리오
1. 관리자가 도메인 `임직원` 생성. 이때 identifier_field_id에 "사번" 필드를, display_name_field_id에 "성명" 필드를 매핑.
2. 하위 노드 `정규직`, `비정규직` 생성.
3. `비정규직` 하위에 `계약직`, `파견직` 생성.
4. `임직원` 노드에 필드 `입사일(DATE)`, `이름(TEXT)` 추가.
5. `계약직` 노드에 필드 `계약종료일(DATE)` 추가.
6. `비정규직` 노드에서 `입사일` 필드를 is_removed=true로 오버라이드.
   → 결과: 정규직은 이름, 입사일 보유. 계약직은 이름, 계약종료일 보유. 파견직은 이름만 보유.
7. (이동 시나리오) `파견직`을 비정규직에서 정규직 아래로 이동.
   → 파견직의 EffectiveField에 입사일이 다시 생김. 기존 파견직 레코드들에 입사일 값이 없으므로 status='MISMATCHED'로 변경되어 기본 조회에서 숨김 처리됨.
8. (참조 시나리오) 프로젝트 도메인에서 임직원 도메인을 참조하는 담당자(REFERENCE) 필드 생성.
   → 프론트엔드에서 담당자 검색 팝업 호출 시, 백엔드는 임직원 도메인의 display_name_field_id(성명)를 기준으로 동적 쿼리를 만들어 검색 결과를 제공함.

---

# 8. 요약 ERD

> **구현 현황 안내:** 아래 ERD는 최초 설계(5-way 라우팅) 기준이다. 실제 구현에서는 `record_table_field` / `record_encrypted_value` / `record_file` / `record_relation`이 별도 테이블로 존재하지 않고, 전부 `record.data` 단일 컬럼에 통합 저장된다. 반면 최초 설계에 없던 `sector`(화면 탭) → `field_group`(필드 그룹) → `field_definition` 계층과, 매칭/병합용 `matching_rule` / `source_priority` / `survivorship_rule` / `record_field_source`, 거버넌스용 `role` / `permission_item` / `domain_permission` / `schema_history`가 실제로 추가 구현되어 있다. 상세는 `2_data_model.md`, `6_governance.md` 참고.

```text
domain (1) ──< classification_node (1, self-referencing tree via parent_id)
domain (1) ──< field_definition (N)  // 도메인 식별자/표시명 필드 매핑 (FK)
domain (1) ──< sector (N) ──< field_group (N)                 // 실제 구현: 화면 탭/그룹 계층
classification_node (1) ──< field_definition (N)

classification_node (1) ──< record (N)
record (1) ──< record_field_source (N)                        // 실제 구현: 필드별 소스 계보

domain (1) ──< matching_rule (N)                               // 실제 구현: 정확/퍼지 매칭 룰
domain (1) ──< survivorship_rule (N)                           // 실제 구현: 필드별 병합 전략
domain (1) ──< source_priority (N)                             // 실제 구현: 소스 시스템 우선순위
classification_node (1) ──< schema_history (N)                 // 실제 구현: 스키마 변경 감사 로그

field_definition (1) ──< dq_rule (N) ──< dq_violation (N)       // 실제 구현: DQ 룰 엔진
```

**(참고, 미구현) 최초 설계상의 5-way 라우팅:**
```text
record (1) ──< record_table_field (N)      [type='TABLE']   — 미구현, record.data로 대체
record (1) ──< record_encrypted_value (N)  [is_encrypted=true] — 미구현, record.data로 대체
record (1) ──< record_file (N)             [type='FILE']    — 미구현, record.data로 대체
record (1) ──< record_relation (N)         [type='REFERENCE'] — 미구현, record.data로 대체
```

---

# 9. 향후 확장 고려사항 (업데이트: 구현 여부 반영)
- **다축 분류(Multi-axis)** (예: 고용형태 축 + 부서 축 동시 적용): **미구현.** 노드를 다중 트리(DAG)로 확장하거나, 분류축(Axis) 개념을 별도로 두는 설계 검토가 여전히 필요함.
- **암호화 필드의 키 분리**(필드별/도메인별 키, KMS 연동): **미구현.** 현재 애플리케이션 전역 단일 정적 마스터 키(환경변수)만 사용 중. 보안 요구가 커지면 KMS(AWS KMS, Vault 등) 연동 검토.
- **검색 엔진 도입**(Elasticsearch 등): **미구현.** `is_searchable` 기반 표현식 인덱스만으로 다양한 필드 조합 검색을 지원 중이며, 검색 패턴이 복잡해지면 여전히 검토 필요.
- **권한 관리**(어떤 관리자가 어떤 노드/필드를 수정할 수 있는지): **부분 구현.** `Role`(ROLE_ADMIN/ORG_ADMIN/DATA_STEWARD/DOMAIN_EDITOR/VIEWER) + `PermissionItem`(기능 단위 권한) + `DomainPermission`(도메인 단위 접근 제어)까지는 구현되었으나, **노드 단위**의 세분화된 권한 부여는 아직 없음.
- **(신규) Golden Record 병합 취소 API 노출**: `RecordMergeService.unmergeRecord()` 로직은 구현되어 있으나 `RecordMergeController`에 엔드포인트가 없어 실제로 호출할 수 없음. 우선순위 높은 마감 항목.
- **(신규) EffectiveFields 계산 캐싱**: 현재 `@Cacheable`은 Domain 조회에만 적용되어 있고, 트리를 순회하는 EffectiveFields 계산에는 캐싱이 없어 조회 비용 최적화 여지가 있음.
