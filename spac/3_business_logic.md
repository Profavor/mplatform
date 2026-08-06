# 4. 비즈니스 로직 스펙

## 4.1 스키마 관리 & EffectiveFields 캐싱
- **분류축(ClassificationAxis) 및 트리의 상속:**
  - 노드는 상위 조상 노드들의 `FieldDefinition`을 계층 구조에 따라 순차적으로 상속받는다.
  - 도메인은 다중 분류축(`ClassificationAxis`)을 보유할 수 있으며, `isDefault=true`인 축이 기본 상속 트리를 제공한다.
- **EffectiveFields 캐싱 및 무효화 전략:**
  - 노드별 최종 상속 필드 수집 함수 `FieldDefinitionService.getEffectiveFields()` 결과는 `@Cacheable("effectiveFields")`로 캐싱된다.
  - Redis 연동 시 분산 캐시로 작동하며, Redis 장애/미설치 시 `LocalCacheConfig`를 통해 인메모리 캐시로 자동 전환(Fallback)된다.
  - 필드/노드 추가·수정·삭제 또는 스키마 변경 승인 시 `@CacheEvict(value = "effectiveFields", allEntries = true)`를 통해 캐시가 자동으로 무효화된다.

---

## 4.2 데이터 품질(DQ) 룰 검증 & 시계열 트렌드
- **실시간 차단 (Hard-Block):**
  - 레코드 기안/수정 요청 시 `ApprovalService`가 동기적으로 `DqRuleEngine.evaluate()`를 호출한다.
  - 위반 사항 발생 시 예외를 throw하여 결재 요청 및 데이터 저장을 차단한다.
- **Excel 대량 업로드 사전 검증 리포트 (Row Validation):**
  - `POST /records/batch-validate` API는 대량 업로드 파일 전송 전에 각 행의 DQ 룰 위반 여부를 **사전 검사**한다 (저장하지 않음).
  - 행별 위반 필드, 룰 명칭, 심각도(ERROR/WARNING), 다국어 사유, 입력값을 포함하는 `BatchValidationResult` DTO를 반환하여 프론트엔드 리포트 UI(`ExcelUploader.vue`)를 구성한다.
- **DQ 스코어 기록 & 시계열 트렌드 (DqScoreSnapshot):**
  - 매일 새벽 크론 스캔(`DqScheduledScanService`) 또는 사용자 수동 스캔 실행 완료 시 `DqScoreSnapshotService`가 스냅샷(`score`, `totalRecords`, `totalViolations`, `scanType`)을 `dq_score_snapshot` 테이블에 기록한다.
  - `GET /domains/{domainId}/dq-score/trend` 및 `recent` API를 통해 품질 점수 시계열 차트 UI를 제공한다.

---

## 4.3 매칭(Matching), 피드백 루프 & 병합(Merge) / Un-merge
- **퍼지 매칭 및 Golden Record 생성:**
  - Jaro-Winkler 알고리즘 기반 유사도 매칭 및 소스 시스템 우선순위(`SourcePriority`) 기반 서바이버십 병합을 제공한다.
- **스튜어드 검토 피드백 루프 (Match Feedback Loop):**
  - 스튜어드의 매칭 후보 검토 결과(`CONFIRMED_MERGE`, `REJECTED`)를 통계 분석하여 `MatchFeedbackService`가 각 매칭 룰의 정밀도 및 권장 임계값(`recommendedThreshold`)을 계산하여 안내한다.
- **Un-merge (병합 해제):**
  - 잘못 병합된 `MERGED` 상태의 레코드를 `POST /api/records/{id}/unmerge` API를 통해 `ACTIVE` 상태로 복원하고, `RecordHistory`에 `UNMERGED` 변경 이력을 남긴다.

---

## 4.4 연계(Integration) 지수 백오프 & Dead-Letter Queue (DLQ)
- **지수 백오프 (Exponential Backoff):**
  - 연계 채널별 설정(`maxRetries`, `retryBackoffMs`, `useExponentialBackoff`)에 따라 오류 발생 시 다음 재시도 예정 시각(`nextRetryAt`)을 $backoffMs \times 2^{retryCount}$ 배율로 산출한다.
- **Dead-Letter Queue (DLQ) 격리:**
  - `retryCount >= maxRetries`에 도달하면 상태를 `DEAD_LETTER`로 격리하여 정상 재시도 파이프라인에서 제외하고 관리자 큐로 분리한다.
- **자동 & 수동 재시도 (IntegrationRetryScheduler):**
  - `IntegrationRetryScheduler`가 1분 간격으로 `nextRetryAt`이 지난 `FAIL` 건을 자동 재시도한다.
  - 관리자 전용 API(`GET /dead-letter`, `POST /{logId}/retry`, `POST /dead-letter/retry-all`)를 통해 수동 개별/일괄 재시도를 지원한다.

---

## 4.5 개인정보 마스킹 및 접근 권한 관리
- **정규식 기반 마스킹 (Masking Policy):**
  - 주민등록번호(RRN/SSN), 전화번호(PHONE), 카드번호(CARD) 등 주요 민감 정보에 대해 동적 마스킹을 적용한다.
  - `maskingFormatter.ts`를 통해 클라이언트 및 서버 단에서 원본 데이터를 안전하게 치환(예: `123456-*******`)하여 화면에 노출한다.
- **민감 데이터 원본 열람 및 감사 로그 (Audit Log):**
  - 업무상 반드시 원본 데이터 확인이 필요한 경우, `access_reason`(접근 사유)을 필수 입력받아 `SensitiveDataAccessLog`에 기록한다.
  - 열람자 정보, 대상 레코드, 열람한 필드, 접속 IP 등을 감사(Audit) 기록으로 남겨 보안 컴플라이언스를 준수한다.

---

## 4.6 결재(Approval) 워크플로우 관리자 개입
- **관리자(Admin) 개입 로직:**
  - 결재 단계(`ApprovalStep`)에 지정된 담당자(`isAssignee` 또는 `hasRole`)가 부재 중이거나 시스템적 처리가 필요한 경우, 최고 관리자(Admin) 권한을 가진 사용자가 결재를 강제 승인(`admin-approve`) 하거나 반려(`admin-reject`)할 수 있다.
  - 해당 내역은 이력에 별도로 기록되어 추적 가능하다.

---

## 4.7 데이터 암호화(Field Encryption) 및 Blind Indexing
- **하이브리드 암호화 키 분리 전략:**
  - `FieldEncryptionService`는 복호화가 필요한 레코드 필드(`isEncrypted=true`)에 대해 기존 운영 DB 데이터와의 호환성을 100% 유지하는 32바이트 AES-256 키 유도 로직을 적용한다.
  - 암호화된 상태에서도 데이터 일치 검색 및 인덱싱이 가능토록, 암호화 키와 분리된 비밀키 기반의 **SHA-256 HMAC Blind Index** 값을 별도 생성 및 관리한다.
- **Golden Sample 회귀 검증:**
  - 키 유도 방식이나 암호화 라이브러리 업그레이드 시 복호화 로직이 깨지는 것을 원천 봉쇄하기 위해, 하드코딩된 암호화 텍스트(Golden Sample)를 복호화하는 TDD 단위 테스트(`testGoldenSample_BackwardCompatibility`)를 상시 구동하여 역방향 호환성을 강제한다.

---

## 4.8 멱등성 기반 초기화 (Idempotent Data Seeding) 및 Prod DDL 검증
- **안전한 데이터 초기화 (Idempotent Guard):**
  - `PermissionMasterInitializer`, `MenuDataInitializer`, `CommonCodeInitializer` 등 애플리케이션 시작 시 동작하는 모든 시더(Seeder)는 DB 조작 전에 반드시 기존 레코드 유무(`repository.count() > 0` 또는 특정 식별자 조회)를 검증한다.
  - 데이터가 이미 존재할 경우 불필요한 DML 삽입을 Skip하여, 재부팅 시 DB 충돌을 방지하고 불필요한 트랜잭션 오버헤드를 막는다.
- **프로덕션(`prod`) 스키마 무결성:**
  - 실 운영 환경(PostgreSQL)에서는 `ddl-auto: validate` 모드를 철저히 유지하여 애플리케이션 기동 시 예고 없는 테이블 변경이나 데이터 드랍이 일어나지 않도록 보호한다. DB 조작 시에도 기존 레코드를 날리는 `TRUNCATE` 대신 명시적 개별 조회를 통한 안전 관리를 원칙으로 한다.

---

## 4.9 UI 다국어 (Zero-Fallback i18n) & SSR 안전 격리 구조
- **Zero-Fallback 다국어 원칙:**
  - Vue 컴포넌트 내에서 라벨이나 텍스트를 하드코딩하거나 `$t('key', 'Fallback')` 형태로 임의 폴백 문자열을 삽입하는 행위가 엄격히 금지된다. 모든 문구는 `ko.json` 및 `en.json` 사전에 100% 동등하게 정의되어야 하며, 누락 키 자동 검출 검증을 거친다.
- **SSR 및 브라우저 전용 API 격리 (Vite / Node 호환):**
  - Nuxt 서버 사이드 렌더링(SSR) 구동 시 Node.js 환경에 존재하지 않는 브라우저 API(`indexedDB`, `window` 등) 호출 라이브러리(`vue3-emoji-picker` 등)로 인해 발생하는 런타임 충돌 에러를 원천 차단한다.
  - 이를 위해 정적 `import` 대신 `defineAsyncComponent` 동적 로딩을 사용하고, 템플릿 영역을 `<ClientOnly>` 태그로 철저히 감싸서 서버단 렌더링을 격리한다.
