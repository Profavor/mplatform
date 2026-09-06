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

---

## 3.10 로그인 이중화 (2FA / OTP) 알고리즘
- **RFC 6238 TOTP 검증 알고리즘**:
  - 공유 비밀키 $K$와 현재 유닉스 타임스탬프 $T$, 시간 스텝 $X=30$초를 기반으로 시간 카운터 $C = \lfloor T / X \rfloor$를 계산한다.
  - HMAC-SHA1 연산 결과의 하위 4비트를 오프셋으로 추출하여 동적 6자리 OTP 코드를 생성:
    $$\text{Code} = (\text{HMAC-SHA1}(K, C) \bmod 10^6)$$
  - 클라이언트-서버 간 시간차를 수용하기 위해 전후 1스텝($\pm 30$초) 윈도우 오차를 자동 보정한다.
- **일회용 비상 백업코드 관리**:
  - 8자리 무작위 영숫자 코드 8개를 생성하고, DB에는 `SHA-256(Code + Salt)` 해시값만 저장한다.
  - 인증 성공 시 해당 코드는 즉시 `is_used = true`로 마킹되어 재사용이 원천 차단된다.
- **이메일 OTP 제한 및 TTL**:
  - 6자리 숫자 코드를 발급하여 Redis에 TTL 300초(5분)로 적재한다.
  - 브루트포스 방지를 위해 5회 이상 불일치 시 코드를 즉시 폐기하고 재발급을 요구한다.

---

## 3.11 B2B 셀프 온보딩 & 원클릭 프로비저닝 엔진
- **단일 트랜잭션 도메인 프로비저닝**:
  - 고객이 가입 시 선택한 템플릿(`LEASE_CONTRACT`, `PRODUCT`, `CUSTOMER`)에 따라 아래 리소스를 단일 `@Transactional` 단위로 자동 프로비저닝한다:
    1. 기본 `Domain` 엔티티 생성 및 표준 식별자/표시명 필드 지정.
    2. 기본 `ClassificationAxis` 및 업종별 표준 루트/자식 `ClassificationNode` 트리 계층 구성.
    3. 업종 필수 `FieldDefinition`(타입, 필수여부, 단위, 유효성 정규식) 일괄 생성 및 노드 매핑.
    4. 업종 표준 `DqRule`(필수값, 범위, 포맷 규칙) 및 `MatchingRule` 자동 등록.
    5. 초기 체험용 샘플 레코드 3~5건 인서트 및 OpenSearch 인덱스 동기화.

---

## 3.12 MDM ROI 계산 수식 & 리드마그넷
- **마스터 데이터 오류 비용 절감액 산출 수식**:
  $$\text{Savings} = (\text{RecordCount} \times \text{ErrorRate} \times \text{CostPerError}) + (\text{ManualHours} \times \text{HourlyRate} \times \text{AutomationEfficiency})$$
  - `RecordCount`: 기업 보유 총 마스터 레코드 수
  - `ErrorRate`: 업계 평균 마스터 데이터 결함률 (기본값: 15%)
  - `CostPerError`: 데이터 결함당 발생하는 배송/청구/클레임 처리 비용 (기본값: ₩25,000)
  - `AutomationEfficiency`: MDM 자율 정제 도입을 통한 수작업 공수 절감율 (기본값: 70%)

---

## 3.13 부동산 임대차 (`LEASE_CONTRACT`) 리스크 산출 알고리즘
- **만기 D-Day 및 리스크 지수**:
  - 계약 만기일 $T_{\text{expire}}$과 현재 일자 $T_{\text{now}}$의 일수 차이 $\Delta D = T_{\text{expire}} - T_{\text{now}}$ 계산:
    - $\Delta D \le 0$: **만기 경과 (EXPIRED)**
    - $0 < \Delta D \le 7$: **초고위험 (CRITICAL, D-7)**
    - $7 < \Delta D \le 30$: **고위험 (WARNING, D-30)**
- **월세 연체 리스크 복합 산출**:
  - 납부 상태(`UNPAID`) 및 연체 개월 수 $M_{\text{unpaid}}$에 따라 대시보드 리스크 점수 가산:
    $$\text{RiskScore} = (\text{Deposit} - \text{MonthlyRent} \times M_{\text{unpaid}}) / \text{Deposit} \times 100$$
  - 보증금 잔존율이 50% 미만으로 하락 시 대시보드 긴급 회수 경보 트리거.

---

## 3.14 데이터 계보 (Data Lineage) 5단계 파이프라인 그래프
- **전 구간 5단계 변환 체인 빌드**:
  1. `INGESTION`: 원천 시스템(Webhook, Direct DB, CSV)에서 로우 데이터 인입 시각 및 원본 페이로드 기록.
  2. `DQ_VALIDATION`: 10종 룰 엔진 통과 여부, 위반 필드 및 결함 스코어 측정.
  3. `CLEANSING`: AI 추천 룰 및 표준화 함수를 거친 데이터 전후 Diff 생성.
  4. `GOLDEN_RECORD`: 다중 소스 병합 규칙(Survivorship)에 의해 최종 선택된 필드 출처 매핑.
  5. `TARGET_EGRESS`: ERP, CRM, 데이터레이크 등 다운스트림으로의 아웃바운드 전송 완료 상태 기록.
- **이상 노드 감지**: 각 단계의 실행 소요 시간 및 상태를 분석하여 병목 노드(빨간색 경고 뱃지) 실시간 시각화.

---

## 3.15 세분화 RBAC 스코프 검증 & 컬럼 수준 동적 마스킹
- **도메인 및 노드 스코프 인터셉터**:
  - 사용자가 특정 레코드 조회 시 `DomainNodePermission`을 평가하여 소속 부서 노드의 상속 경로 상에 포함되지 않은 비인가 노드의 레코드는 조회 결과에서 제외(`403 Forbidden` 또는 목록 필터링).
- **런타임 컬럼 동적 마스킹**:
  - Jackson 직렬화 시점 또는 DTO 매핑 시점에 사용자의 역할을 확인하여 `ColumnMaskingRule`에 따라 주민번호 뒷자리(`******`), 계좌번호 중간자리, 급여/보증금(`***`)으로 자동 치환.

---

## 3.16 스키마 변경 하위호환성 사전 시뮬레이션
- **Breaking Change 판정 규칙**:
  1. 필수(`isRequired = true`) 필드를 신규 추가하면서 기본값(`defaultValue`)이 지정되지 않은 경우.
  2. 기존 필드의 데이터 타입을 축소하는 경우 (예: `VARCHAR(100)` → `VARCHAR(20)`).
  3. 열거형(`ENUM`) 필드에서 기존에 사용 중인 허용 코드를 삭제하는 경우.
- **영향도 시뮬레이션**:
  - 실제 스키마를 즉시 변경하지 않고 기존 레코드 전체에 대해 새 룰을 가상 적용하여 부적합 레코드 건수 및 위반 목록을 사전 보고서로 출력.

