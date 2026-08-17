# 8. 플랫폼 엔지니어링, 성능, 보안 & 협업 명세서 (Platform Features)

---

## 8.1 고성능 하이브리드 캐싱 (Hybrid Cache Infrastructure)
- **캐싱 대상**: `FieldDefinitionService.getEffectiveFields()` (노드 상속 트리 계산 결과)
- **적용 메커니즘**: Spring Cache (`@Cacheable("effectiveFields")`)
- **이중 스토리지 아키텍처**:
  - **Primary**: Redis 분산 캐시 (`RedisCacheConfig`)
  - **Fallback**: In-Memory `ConcurrentMap` (`LocalCacheConfig`) — Redis 서버 장애 또는 미설치 환경 시 자동으로 로컬 메모리 캐시로 전환되어 무중단 서비스를 보장한다.
- **캐시 무효화 (Eviction)**: 스키마 변경, 노드 추가/삭제, 결재 완료 시 `@CacheEvict(allEntries = true)`를 통해 캐시를 실시간 초기화한다.

---

## 8.2 HashiCorp Vault Transit HSM 암호화
- **Transit Secret Engine 연동**:
  - `security.encryption.type=VAULT` 활성화 시 HashiCorp Vault의 Transit API와 연동하여 데이터 키 암호화(DEK/KEK) 및 하드웨어 보안 모듈(HSM) 수준의 키 수명주기(Key Lifecycle) 관리를 지원한다.
- **봉투 암호화 (Envelope Encryption)**:
  - 마스터 키는 Vault 내부에 안전하게 격리되며, 애플리케이션은 Vault에서 유도된 토큰으로 데이터를 암호화/복호화한다.

---

## 8.3 AI 기반 이상 탐지 레이더 (Anomaly Detection Radar)
- **접근 이상 탐지 (`AnomalyAccessDetectionService`)**:
  - 짧은 시간 내 비정상적으로 대량의 마스킹 해제 요청이나 비인가 도메인 접근 시도를 감지하여 관리자에게 실시간 경고를 발송하고 세션을 차단한다.
- **대량 변동 레이더 (`VolumeAnomalyRadarService`)**:
  - 특정 도메인에서 평상시 임계치(Baseline)를 초과하는 대량의 레코드 생성/수정/삭제 변동이 발생할 경우 AI 레이더가 이를 이상 징후로 포착하여 대시보드에 시각화한다.

---

## 8.4 글로벌 시스템 통합 진단 (`GlobalSystemDiagnosticsService`)
- CPU 사용률, JVM Heap/Non-Heap 메모리, DB 커넥션 풀(HikariCP Active/Idle), 메시지 큐 대기열(Kafka/RabbitMQ), 스토리지 잔여 용량을 통합 수집하여 시스템 건강도(Health Score)를 실시간 산출한다.

---

## 8.5 데이터 신선도 히트맵 & SLA 계약 관리
- **데이터 신선도 히트맵 (`DataFreshnessHeatmapService`)**:
  - 각 도메인 및 노드별 데이터의 최종 갱신 시간과 수집 주기를 분석하여 신선도(Freshness)를 녹색/황색/적색 히트맵으로 시각화한다.
- **데이터 SLA 계약 (`DataSlaContractService`)**:
  - 데이터 공급 부서와 소비 부서 간 데이터 적시성(Latency) 및 품질(Quality) SLA 계약을 체결하고 준수율을 모니터링한다.

---

## 8.6 데이터 자산 가치평가 (`DataAssetValuationService`)
- 마스터 데이터의 레코드 건수, 사용 빈도, DQ 품질 지수, 참조 관계의 복잡성을 가중 결합하여 데이터 자산의 금전적 평가 가치(Valuation)를 산정한다.

---

## 8.7 OpenSearch 전역 전문 검색 & MinIO 스토리지
- **OpenSearch 2.11 전역 검색**:
  - 한글 형태소 분석기(Nori) 및 다중 필드 복합 쿼리를 적용하여 수백만 건의 레코드 속에서 100ms 이내의 초고속 전문 검색을 제공한다.
- **MinIO 분산 오브젝트 스토리지**:
  - AWS S3 표준 호환 SDK를 기반으로 대용량 첨부파일, 엑셀 익스포트 파일, 콜드 아카이브 파일을 안전하게 저장하고 Pre-signed URL로 다운로드를 지원한다.

---

## 8.8 Prometheus + Grafana 통합 모니터링
- **Micrometer 메트릭 수집**:
  - `/actuator/prometheus` 엔드포인트를 통해 HTTP 요청 지연(p95/p99), JVM GC Pause, DB HikariCP 상태를 Prometheus로 수집한다.
- **Grafana 대시보드 (`grafana/`)**:
  - 사전 프로비저닝된 대시보드를 통해 실시간 트래픽, 에러율, 시스템 리소스를 시각화한다.

---

## 8.9 인앱 협업 워크스페이스 & 시스템 라디오
- **8방향 리사이즈 & 드래그 메신저 (`InAppMessenger.vue`)**:
  - 화면 어디서나 자유롭게 배치 및 리사이징이 가능한 웹소켓 STOMP 메신저.
- **실시간 메시지 번역**:
  - 다국어 사용자를 위해 메시지 송수신 시 원클릭 다국어 번역 지원.
- **대화형 데이터 뷰어 (`ExcelPreviewModal`, `TableDataViewerModal`)**:
  - 대량 업로드 전 엑셀 시트를 브라우저 내에서 즉시 인터랙티브하게 탐색.
- **시스템 라디오 (`SystemRadioWidget.vue`)**:
  - 관리자가 유튜브 연동을 통해 사내 공지 및 배경음악을 제어하는 라디오 위젯.

---

## 8.10 AG-Grid Vue3 엔터프라이즈 그리드
- AG-Grid Vue3 (^34.3.1) 최신 객체 구문을 전면 도입하고 대용량 데이터 렌더링 시 가상 스크롤(Virtual Scroll) 및 서버 사이드 페이징(Server-Side Pagination)을 완벽 지원한다.

---

## 8.11 TDD & Nuxt AST 정적 컴파일 검증 파이프라인
- **결함 제로지향 파이프라인**:
  - 프론트엔드 유닛 테스트 91개와 백엔드 JUnit 테스트 47개를 완비.
  - `npm test` 구동 시 Vitest 검증과 함께 **`npm run build`(Nuxt Node 템플릿 컴파일 및 번들 정적 분석)**를 필수 결합하여 런타임 Vue AST 문법 오류와 SSR API 누출을 사전에 100% 억제한다.
