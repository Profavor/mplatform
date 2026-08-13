# 7. 외부 연계 (Integration) & DLQ 명세

## 7.1 연계 채널 (IntegrationChannel) 설정
- **방향:** Inbound (시크릿 토큰 인증 Webhook) 및 Outbound (Spring Integration 동적 라우팅).
- **지원 프로토콜:** HTTP REST Webhook, JDBC Direct SQL, Apache Kafka, RabbitMQ (AMQP).
- **보안 및 안정성 (JDBC/Webhook):**
  - 자격증명(비밀번호 등)은 AES-256-GCM 알고리즘으로 암호화되어 DB에 저장 및 관리되며, API 조회 시 마스킹 처리됨.
  - 외부 입력(테이블/컬럼명)에 대한 엄격한 식별자 검증 및 DBMS별 Quoting 처리를 통해 SQL Injection 차단.
  - JDBC 채널별 커넥션 풀(`HikariDataSource`)을 적용하여 재사용성을 높이고 설정된 타임아웃을 통해 리소스 대기 상태 방지.
- **재시도 백오프 설정:**
  - `maxRetries`: 최대 재시도 횟수 (기본 3회)
  - `retryBackoffMs`: 기본 백오프 간격 (ms, 기본 1000ms)
  - `useExponentialBackoff`: 지수 백오프 적용 여부 (기본 true)

---

## 7.2 지수 백오프 & Dead-Letter Queue (DLQ) 메커니즘
- **다음 재시도 시각 계산:**
  $$nextRetryAt = now + retryBackoffMs \times 2^{retryCount}$$
- **DLQ 상태 전환:**
  - `retryCount >= maxRetries`에 도달하면 `IntegrationLog` 상태가 `FAIL`에서 `DEAD_LETTER`로 격리된다.
- **자동 재시도 스케줄러 (`IntegrationRetryScheduler`):**
  - `@Scheduled(cron = "0 * * * * ?")` 1분 간격으로 `nextRetryAt`이 지닌 `FAIL` 연계 로그를 자동 재시도 전송한다.
- **수동 관리 API:**
  - `GET /api/admin/integration/logs/dead-letter`: DLQ 목록 조회
  - `POST /api/admin/integration/logs/{logId}/retry`: 개별 로그 1건 수동 재시도
  - `POST /api/admin/integration/logs/dead-letter/retry-all`: DLQ 전체 일괄 재시도

---

## 7.3 인바운드 연계 인증
- 외부 시스템에서 플랫폼으로 데이터를 전송할 때는 채널별로 발급된 고유 시크릿 토큰을 HTTP 헤더를 통해 검증하는 Webhook 인증 메커니즘을 사용한다.

## 7.4 아웃바운드 연계 프로토콜별 상세
- **HTTP REST Webhook:** Spring Integration HTTP Outbound Gateway를 통해 RESTful API 호출 수행.
- **JDBC Direct:** MySQL, MariaDB, Oracle, MSSQL 등 내장 드라이버를 활용해 Spring Integration JDBC Outbound Adapter로 직접 SQL 쿼리 실행.
- **Apache Kafka:** Spring Kafka Producer를 이용한 메시지 스트림 전송.
- **RabbitMQ AMQP:** Spring AMQP Template을 활용한 메시지 큐 전송.

## 7.5 연계 채널 SpEL 매핑
- 채널에 정의된 `mapping_config_json` 설정을 통해, 수신되거나 발신되는 페이로드를 Spring Expression Language(SpEL) 기반으로 동적 구조 변환 및 데이터 매핑한다.

## 7.6 비동기 대량 데이터 내보내기
- 수십만 건의 대용량 레코드 조회를 엑셀로 내보낼 시 브라우저 타임아웃을 방지하기 위해 `AsyncBatchExportService`를 사용한다. 내보내기 작업은 백그라운드에서 비동기로 수행되며 완료된 파일은 MinIO 오브젝트 스토리지에 업로드되어 알림을 통해 다운로드 링크가 제공된다.
