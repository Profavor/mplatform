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
