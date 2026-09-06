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

## 8.10 AG-Grid Vue3 엔터프라이즈 그리드 대용량 최적화
- **Enterprise 가상 스크롤 (Virtual Scrolling)**:
  - 수만 건 이상의 마스터 레코드를 렌더링할 때 브라우저 DOM 노드를 뷰포트 내의 행만 동적으로 유지하여 메모리 누수와 렌더링 병목을 원천 제거.
- **서버사이드 페이징 & 정렬/필터 (Server-Side Operations)**:
  - 클라이언트로 전건을 수신하지 않고 AG-Grid의 `ServerSideRowModel` 및 백엔드 Spring Data JPA `Pageable`을 연동하여, 수십만 건 데이터에 대해 100ms 이내의 실시간 페이징 및 다중 컬럼 정렬/필터링을 지원.

---

## 8.11 TDD & Nuxt AST 정적 컴파일 검증 파이프라인
- **실측 테스트 스위트 현황**:
  - **백엔드**: **253개**의 JUnit 5 단위 및 통합 테스트 클래스 운영 (Golden Sample 암호화 회귀 불변성 포함).
  - **프론트엔드**: **243개**의 Vitest 컴포넌트 및 비즈니스 훅 단위 테스트 스펙 완비.
- **결함 제로지향 정적 컴파일 파이프라인**:
  - `npm test` 구동 시 Vitest 검증과 함께 **`npm run build`(Nuxt Node 템플릿 컴파일 및 번들 정적 분석)**를 필수 결합하여 런타임 Vue AST 문법 오류와 SSR API 누출을 사전에 100% 억제한다.

---

## 8.12 로그인 이중화 (2FA / OTP) 플랫폼 보안
- **엔터프라이즈 다중 인증**:
  - RFC 6238 표준 TOTP, 사내 메일서버 연동 이메일 OTP(TTL 5분), 8자리 일회용 백업코드 체계를 Spring Security 필터 체인에 통합.
  - 관리자 및 스튜어드 등 주요 권한 보유자의 계정 탈취를 원천 차단.

---

## 8.13 B2B 셀프 온보딩 & 원클릭 템플릿 프로비저닝 엔진
- **업종별 템플릿 카탈로그**:
  - 부동산 임대차/자산관리, 고객 마스터, 상품 마스터 등 산업별 표준 도메인 템플릿을 제공.
- **원클릭 자동 프로비저닝**:
  - 회원가입 완료 즉시 단일 트랜잭션 단위로 도메인 메타데이터, 다축 노드 트리, 필드 정의, 기본 DQ 룰 및 초기 샘플 레코드를 일괄 자동 프로비저닝.

---

## 8.14 소개 홈페이지(랜딩 페이지) & 인가 라우팅 제어
- **반응형 인터랙티브 랜딩 (`pages/index.vue`)**:
  - 비로그인 방문자 대상 Why MDM, 플랫폼 아키텍처, 고객 성공 사례, ROI 계산기 및 FAQ를 제공하는 전용 랜딩 레이아웃(`layouts/landing.vue`) 서빙.
- **인증 상태별 지능형 라우팅 (`auth.global.ts`)**:
  - 비로그인 방문자는 랜딩 페이지를 자유롭게 탐색할 수 있으며, 로그인 완료 상태 감지 시 즉시 내부 업무 대시보드(`/dashboard`)로 전환.

---

## 8.15 부동산 임대차 (`LEASE_CONTRACT`) 리스크 대시보드 위젯
- **리스크 관제 패널**:
  - 계약 만기 D-30, D-7 임박 알림 및 월세 연체 상태를 실시간 집계하여 대시보드 상단 위젯에 시각화.
  - 고위험 계약 클릭 시 즉각적인 레코드 상세 모달 연계.

---

## 8.16 데이터 계보 (Data Lineage) 5단계 시각화 엔진
- **전 구간 실시간 파이프라인 그래프**:
  - Ingestion → DQ Validation → Cleansing → Golden Record → Target Egress의 5단계 변환 흐름을 인터랙티브 DAG 그래프로 렌더링.
  - 노드 클릭 시 단계별 데이터 Diff 및 실행 소요 시간 실시간 표출.

