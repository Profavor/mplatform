# 3. 비즈니스 로직 및 알고리즘 명세 (Business Logic Specifications)

본 문서는 플랫폼의 핵심 비즈니스 로직, 알고리즘, 캐싱 및 데이터 처리 파이프라인에 대한 상세 기술 명세서이다.

---

## 3.1 동적 스키마 상속 & EffectiveFields 2중 캐싱
- **다축 상속 알고리즘**:
  - 특정 노드 $N$의 유효 필드 집합 $E(N)$은 루트 노드부터 $N$까지의 조상 경로 상에 선언된 모든 `FieldDefinition`의 합집합으로 계산된다:
    $$E(N) = \bigcup_{p \in \text{Ancestors}(N) \cup \{N\}} \text{Fields}(p) \setminus \text{RemovedOverrides}$$
  - 동일한 키를 가진 필드가 하위 노드에서 재정의된 경우, 하위 노드의 설정(라벨, 옵션, 필수 여부 등)이 상위 노드의 설정을 오버라이드한다.
- **하이브리드 2중 캐시 (Hybrid Cache Infrastructure)**:
  - `FieldDefinitionService.getEffectiveFields()` 결과는 Spring Cache(`@Cacheable("effectiveFields")`)를 통해 **Redis 분산 캐시**에 저장된다.
  - Redis 서버 장애 또는 로컬 개발 환경 시 `LocalCacheConfig`에 의해 **In-Memory ConcurrentMap**으로 자동 전환(Fallback)되어 무중단 성능을 보장한다.
- **캐시 무효화 (Cache Eviction)**:
  - 필드/노드의 추가·수정·삭제, 스키마 승인 완료 시 `@CacheEvict(value = "effectiveFields", allEntries = true)`가 즉시 트리거되어 데이터 일관성을 유지한다.

---

## 3.2 데이터 품질(DQ) 룰 엔진, AI 추천 & 자율 치료
- **10종 룰 평가 매트릭스**:
  1. `NOT_NULL`: 필드 값의 존재 여부 및 빈 문자열 검사.
  2. `REGEX`: 지정된 정규표현식 일치 검사 (이메일, 사업자번호 등).
  3. `RANGE`: 숫자형 데이터의 최소/최대 범위 검사.
  4. `LENGTH`: 문자열 길이의 최소/최대 제약 검사.
  5. `ENUM`: 사전에 정의된 허용 목록 포함 여부 검사.
  6. `DATE_RANGE`: 날짜의 시작일-종료일 선후 관계 검사.
  7. `CROSS_FIELD`: SpEL 수식을 통한 복수 필드 간 상관관계 검사 (예: `type == 'VIP' ? discountRate >= 10 : true`).
  8. `UNIQUE`: 도메인 내 전체 레코드 간 해당 필드값의 중복 여부 검사.
  9. `SPEL_EXPRESSION`: 임의의 복합 비즈니스 로직 동적 검증.
  10. `CALCULATED_FIELD`: 타 필드값에 기반한 수식 계산 결과 검증.
- **Excel 대량 업로드 사전 검증 (`POST /records/batch-validate`)**:
  - 엑셀 업로드 시 DB에 즉시 저장하지 않고 메모리 상에서 행(Row)별로 전체 DQ 룰을 사전 평가하여 `BatchValidationResult`를 반환한다.
  - 위반 행에 대해 필드명, 룰 유형, 심각도(ERROR/WARNING), 다국어 사유, 입력값을 표 형태로 제공한다.
- **AI 룰 추천 (`DqRecommendationService`)**:
  - 도메인 데이터 프로파일링 결과(Null 비율, 고유값 비율, 평균 길이, 데이터 패턴)를 분석하여 최적의 DQ 룰 세트를 자동 추천한다.
- **자율 치료 및 정제 (`DqRemediationService`, `AutonomousCleansingService`)**:
  - 결측치 기본값 대체, 문자열 공백/특수문자 트림, 날짜 포맷 표준화 등 반복적인 DQ 결함에 대해 자율 정제 제안을 생성하고 일괄 자동 치유를 수행한다.

---

## 3.3 매칭(Matching), 피드백 루프 & Survivorship 병합 / Un-merge
- **퍼지 매칭 (Jaro-Winkler 유사도)**:
  - 두 문자열 $s_1, s_2$에 대해 일치하는 문자 수 $m$과 전치(Transposition) 수 $t$를 기반으로 기본 유사도 $Sim_j$를 계산하고, 공통 접두사 길이 $l$과 가중치 $p(0.1)$를 적용하여 최종 유사도를 산출한다:
    $$Sim_{jw} = Sim_j + l \cdot p \cdot (1 - Sim_j)$$
- **스튜어드 피드백 루프 (`MatchFeedbackService`)**:
  - 스튜어드가 매칭 의심 후보를 검토하여 '중복 확정(`CONFIRMED_MERGE`)' 또는 '오탐 반려(`REJECTED`)' 처리한 이력을 통계 집계한다.
  - 정밀도(Precision)가 높은 룰의 가중치를 상향하고 최적의 권장 임계값(`recommendedThreshold`)을 계산하여 스튜어드에게 제안한다.
- **서바이버십(Survivorship) 병합 & Un-merge**:
  - `SourcePriority`에 정의된 소스 시스템 신뢰도 순위(예: SAP ERP > Salesforce > Manual)에 따라 필드 단위로 생존 값을 결정하여 골든 레코드를 생성한다.
  - 잘못 병합된 경우 `POST /records/{id}/unmerge`를 호출하여 피병합 레코드를 `ACTIVE`로 복원하고 변경 이력(`RecordHistory`)에 `UNMERGED`를 기록한다.

---

## 3.4 지능형 결재(Approval), 위임, 에스컬레이션 & 샌드박스
- **다단계 결재선 및 조건부 동적 라우팅 (`DynamicRoutingService`)**:
  - 레코드의 특정 필드값(예: 거래 금액, 계약 등급)에 따라 `ApprovalRoutingTemplate`에 정의된 SpEL 조건식을 평가하여 결재 단계를 동적으로 분기 생성한다.
- **결재 위임 (`ApprovalDelegationService`)**:
  - 결재자가 등록한 위임 기간 동안 대결자(Delegatee)에게 결재 권한이 자동 부여되며, 대결자의 승인/반려 시 원 결재자와 대결자 정보가 감사 이력에 동시 기록된다.
- **결재 에스컬레이션 (`ApprovalEscalationService`)**:
  - 결재 단계의 SLA 제한 시간(예: 24시간)이 초과된 경우 상위 관리자나 지정된 에스컬레이션 담당자에게 결재권이 자동 승격된다.
- **결재 샌드박스 시뮬레이션 (`ApprovalSandboxService`)**:
  - 결재 승인 전, 해당 변경이 반영되었을 때 발생할 수 있는 스키마 충돌, DQ 위반, 연계 파급 효과를 가상 샌드박스에서 사전 시뮬레이션한다.
- **관리자 강제 개입**:
  - 최고 관리자(`SYSTEM_ADMIN`) 권한을 가진 사용자는 결재권자 부재 등의 비상 상황 시 `admin-approve` 또는 `admin-reject`를 실행할 수 있다.

---

## 3.5 제로 트러스트 보안, Vault Transit & 해시체인 원장
- **32바이트 AES 하이브리드 암호화 & SHA-256 HMAC Blind Indexing**:
  - 민감 필드(`isEncrypted=true`)는 32바이트 AES 대칭키로 암호화되어 Base64로 저장된다.
  - 암호화된 필드에 대해 원본 복원 키와 완전히 분리된 키를 사용하여 **SHA-256 HMAC Blind Index**를 별도 생성하여 DB에 인덱싱함으로써, 평문 노출 없이 정확 일치(Exact-Match) 조회를 수행한다.
- **HashiCorp Vault Transit HSM 연동**:
  - `security.encryption.type=VAULT` 설정 시 HashiCorp Vault의 Transit Secret Engine API를 호출하여 HSM(Hardware Security Module) 수준의 키 보호 및 봉투 암호화(Envelope Encryption)를 수행한다.
- **Golden Sample 회귀 불변성 검증**:
  - 암호화 로직 업그레이드 시 기존 DB 암호문의 복호화 호환성을 영구 보장하기 위해 고정 암호문(Golden Sample)을 복호화하는 TDD 단위 테스트를 상시 구동한다.
- **불변 해시체인 감사 원장 (`HashChainAuditService`)**:
  - 레코드의 모든 변경 스냅샷마다 블록 해시를 생성한다:
    $$\text{BlockHash}_n = \text{SHA-256}(\text{BlockHash}_{n-1} + \text{RecordId} + \text{Version} + \text{DataSnapshot} + \text{Timestamp})$$
  - 이전 해시와 체이닝되어 데이터가 단 1바이트라도 위변조될 경우 해시 체인 검증(`verifyLedger`)에서 즉시 탐지된다.

---

## 3.6 외부 연계 지수 백오프, DLQ & CDC 스트리밍
- **지수 백오프 (Exponential Backoff)**:
  - 연계 실패 시 다음 재시도 시각 $nextRetryAt$을 산출한다:
    $$nextRetryAt = \text{now}() + \text{retryBackoffMs} \times 2^{\text{retryCount}}$$
- **Dead-Letter Queue (DLQ) 격리**:
  - 재시도 횟수가 `maxRetries`를 초과하면 로그 상태를 `DEAD_LETTER`로 격리하고 관리자 전용 일괄 재시도 큐로 전환한다.
- **CDC 실시간 스트리밍 (`CdcStreamingService`)**:
  - 마스터 데이터 변경 시 Kafka 토픽으로 `MasterDataChangedEvent`를 발행하여 다운스트림 시스템으로 실시간 변경 이벤트를 동기화한다.
- **파이프라인 자가 치유 (`PipelineSelfHealingService`)**:
  - 연계 파이프라인에서 지속적인 실패가 감지되면 서킷 브레이커를 작동하고 사전 정의된 백업 채널로 트래픽을 자동 우회한다.

---

## 3.7 레코드 타임머신 & 데이터 라이프사이클
- **레코드 타임머신 (`RecordTimeMachineService`)**:
  - `RecordHistory` 테이블을 역추적하여 사용자가 지정한 과거 특정 시점($T_{\text{target}}$)의 레코드 및 스키마 상태를 재구성(As-Of Query)하고, 필요 시 원클릭으로 과거 버전으로 롤백한다.
- **콜드 스토리지 아카이빙 (`ColdStorageArchiveService`)**:
  - 1년 이상 미사용/비활성 레코드를 압축하여 MinIO/S3 오브젝트 스토리지의 콜드 티어로 이관하고 필요 시 복원한다.
- **데이터 보존 정책 (`DataRetentionPolicyService`)**:
  - 개인정보보호법 및 내부 정책에 따라 법적 보존 기한이 만료된 데이터를 식별하고 자동 파기/익명화한다.

---

## 3.8 자연어 스마트 쿼리 & 비정형 데이터 추출
- **스마트 쿼리 파서 (`SmartQueryParserService`)**:
  - "서울에 거주하는 VIP 고객 중 최근 3개월간 구매가 없는 회원"과 같은 자연어 질의를 파싱하여 도메인 필드 조건식(DSL)으로 변환하여 검색한다.
- **비정형 데이터 추출 (`UnstructuredDataExtractorService`)**:
  - 계약서 PDF, 이메일 본문, 비정형 텍스트에서 마스터 데이터 필드(사업자번호, 대표자명, 주소 등)를 자동 추출하여 레코드 생성 폼에 사전 매핑한다.

---

## 3.9 멱등성 기반 시딩 (Idempotent Seeding) & UI 규약
- **멱등성 시딩 가드**:
  - `PermissionMasterInitializer`, `MenuDataInitializer`, `CommonCodeInitializer`, `RoleInitializer` 등 기초 시더는 실행 전 반드시 `repository.count() > 0` 검증을 수행하여 중복 삽입 및 DB 충돌을 방지한다.
- **Zero-Fallback 다국어 & 타임존 거버넌스**:
  - 모든 프론트엔드 라벨은 `ko.json` 및 `en.json` 사전을 통해서만 렌더링되며 소스 내 하드코딩된 폴백 텍스트는 원천 차단된다.
  - 일시 표출 시 개인화 타임존 쿠키(`useTimezoneDate()`)와 `parseDate` 방어 함수를 필수 적용하여 GMT 시차 왜곡을 완벽 방지한다.
