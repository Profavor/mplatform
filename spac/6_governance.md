# 6. 거버넌스 & 스키마 감사

## 6.1 스키마 변경 워크플로우 & SchemaHistory
- 스키마 필드 및 노드의 생성/수정/삭제 조작은 `ApprovalService`를 거치며 `SCHEMA_CHANGE` 워크플로우 승인 후 반영된다.
- 스키마 변경 시 `SchemaHistory` 엔티티에 Before/After JSON 스냅샷 및 변경 사유가 시계열 기록된다.

## 6.2 스키마 과거 특정 시점 (As-Of) 조회
- `GET /api/nodes/{nodeId}/effective-fields/as-of?timestamp=` API를 통해 과거 특정 시점의 유효 필드 상태(EffectiveFields)를 재구성하여 조회할 수 있다.
- 스키마 변경 이력을 기반으로 레코드의 작성 당시 스키마 상태를 정확히 추적할 수 있다.

## 6.3 필드 수준 출처 계보 (Field Lineage)
- `RecordFieldSource` 엔티티가 `(record_id, field_key)`별로 최근 데이터 출처 소스 시스템명(`source_system`)과 변경 시각을 기록한다.
- 프론트엔드 레코드 상세 Drawer 및 결재 상세 Viewer에서 필드 단위 출처 정보가 시각화된다.

## 6.4 암호화 하위 호환성 (Golden Sample Verification) 및 Blind Indexing 거버넌스
- **Golden Sample 회귀 불변성 보장:** 암호화 알고리즘 개선이나 시스템 라이브러리 교체 시 과거 암호화된 DB 레코드 불만족(복호화 불가) 사고를 100% 차단하기 위해, JUnit 5 기반의 고정 샘플(`FieldEncryptionServiceTest.testGoldenSample_BackwardCompatibility`) 검증이 CI/CD의 사령탑 역할을 수행한다.
- **키 분리 거버넌스:** 복호화를 위한 AES 대칭키 유도 로직과 검색을 위한 SHA-256 HMAC Blind Index 생성 키는 서로 무관한 분리 연산으로 유도되어, Blind Index가 노출되어도 원본 데이터를 역산할 수 없도록 보안 컴플라이언스를 만족한다.

## 6.5 화면 및 데이터 표출 거버넌스
- **무의미한 Raw UUID 노출 금지:** 프론트엔드 화면(사용자 목록, 레코드 조회, 결재 모니터링)에 `340a0917-af0b-...` 형태의 원시 UUID 식별자를 날것 그대로 표시하는 것을 엄격히 금지한다. 노출이 불가피한 경우 식별 접두사를 부착한 축약 코드(예: `REC-340a0917` 혹은 `WF-8302f1a2`)나 실제 도메인/명칭 속성으로 가공하여 표출해야 한다.
- **다국어 및 개인화 타임존 의무화:** 하드코딩된 한국어/영어 라벨 및 날짜 오차를 강제 방지하기 위해, 모든 UI 스트링은 `vue-i18n` 사전에 정의해야 하며 일시 표출 시에는 사용자 개인화 쿠키(`useTimezoneDate()`)를 기반으로 한 현지 시차 변환 및 `parseDate` 방어 함수가 필수로 거쳐야 한다.

## 6.6 도메인 접근 권한 거버넌스
- `DomainAccessRequest` 및 `DomainPermission` 엔티티를 기반으로 사용자별/역할별 특정 도메인의 데이터 읽기/쓰기 권한을 엄격히 제어한다.

## 6.7 시스템 감사 로그
- 사용자의 모든 중요 활동은 감사 로그로 남는다. 로그인 이력은 `LoginLog`, 메뉴 접근 이력은 `MenuAccessLog`, 시스템 내 오류 발생 이력은 `ErrorLog`에 기록되어 철저한 보안 추적이 가능하다.

## 6.8 조직 변경 이력
- `UserOrgHistory` 엔티티를 통해 사용자의 조직 이동 및 인사 발령 이력을 시계열로 보관하여, 과거 시점의 조직 데이터를 조회할 수 있다.

## 6.9 분류체계 버전 관리
- `TaxonomyVersion` 엔티티를 활용해 도메인의 분류체계 변경 사항을 스냅샷으로 저장하고, 필요 시 과거 버전의 분류체계를 복원하거나 비교 분석할 수 있다.
