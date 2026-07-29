# 7. 외부 연계 (Integration) & DLQ 명세

## 7.1 연계 채널 (IntegrationChannel) 설정
- **방향:** Inbound (시크릿 토큰 인증 Webhook) 및 Outbound (Spring Integration 동적 라우팅).
- **지원 프로토콜:** HTTP REST Webhook, JDBC Direct SQL, Apache Kafka, RabbitMQ (AMQP).
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
