# Master Data Management (MDM) System

이 프로젝트는 조직 내 흩어진 핵심 데이터(Master Data)를 통합, 정제, 일관성 있게 관리하기 위한 Domain / Master Data Management(MDM) 시스템입니다.

## 🎯 Project Purpose (프로젝트 전체 목표)
초기에는 각 부서나 시스템별로 파편화된 **Domain Management** 기능을 제공하여 개별 데이터를 관리하는 수준에서 시작했습니다. 하지만 궁극적인 목표는 **Master Data Management(MDM) 플랫폼으로의 진화**입니다.
기업의 핵심 데이터인 Customer, Product, Vendor, Employee 등의 마스터 데이터를 중앙 집중식으로 수집하고, 데이터 품질(Data Quality)을 검증하여, 중복을 제거한 **Golden Record**를 생성하고 이를 외부 시스템으로 전파하는 것을 목표로 합니다.

> 현재 상태: 동적 스키마(승인 워크플로우 포함), DQ 룰 엔진(실시간 차단 + 정기 스캔), 정확/퍼지(유사도) 중복 검사, 소스 우선순위 기반 필드 단위 병합(Survivorship)을 포함한 Golden Record 생성, Excel 대량 업로드, 인바운드/아웃바운드 연계까지 구현되어 있습니다. 세부 내용은 [🔑 Key Features](#-key-features) 참고.

## 🏛 Architecture Overview
Frontend와 Backend가 완전히 분리된 구조로 REST API를 통해 통신하며, 런타임에 동적으로 스키마를 구성하는 메타데이터 드리븐 아키텍처를 채택했습니다.

```mermaid
graph TD
    A[Frontend: Nuxt 3] -->|REST API| B[Backend: Spring Boot]
    B --> C[(PostgreSQL 15)]
    B --> FS[로컬 파일시스템<br/>파일 저장]
    EXT[외부 시스템] -->|Inbound Webhook<br/>채널 시크릿 인증| B
    B -->|MasterDataChangedEvent| OUT[Outbound Integration<br/>Spring Integration]
    OUT -->|HTTP / JDBC / Kafka / RabbitMQ| EXT

    subgraph MDM Core
        S1[Dynamic Schema Engine]
        S2[DQ Rule Engine]
        S3[Matching Service - 정확/퍼지 일치 + Survivorship 병합]
        S4[Approval Workflow - Record]
        S1 --> S2 --> S3 --> S4
    end
    B -.-> S1
```

> **보안 및 인프라 안내:** 
> - 백엔드는 Spring Security 기반 자체 JWT 토큰 인증 방식을 사용합니다.
> - `application.yml`은 `JWT_SECRET`, `DB_USERNAME`, `DB_PASSWORD` 등 환경변수를 필수로 참조합니다.

## 🛠 Tech Stack 상세
- **Frontend**
  - **Framework**: Nuxt 3 (^3.17.7), Vue 3
  - **Language**: TypeScript (^5.9.3)
  - **UI Library**: Vuestic UI (^1.10.3)
  - **Data Grid & Chart**: AG Grid Vue3 / Enterprise (^34.3.1), Apache ECharts (^5.6.0)
  - **i18n**: @nuxtjs/i18n (한국어 기본, 영어 지원)
- **Backend**
  - **Framework**: Spring Boot 4.1.0
  - **Language**: Java 17
  - **ORM**: Spring Data JPA, Hibernate, Spring Data Envers
  - **Security**: Spring Security, 자체 발급 JWT (jjwt) — 환경변수(`JWT_SECRET`) 필수 참조
  - **Integration**: Spring Integration (HTTP / JDBC / Event), Spring Retry, Spring Kafka, Spring AMQP(RabbitMQ) — 아웃바운드 연계 채널용
- **Database & Infrastructure**
  - **RDBMS**: PostgreSQL 15
  - **Container**: Docker & Docker Compose (Postgres 기본 제공)

## 📌 [Roadmap / 향후 확장 예정 인프라]
- **Keycloak**: 외부 IAM (OAuth2 / OIDC) 로그인 연동 준비 중
- **Redis**: 현재 Domain 조회 캐싱(`RedisCacheConfig`)에는 이미 사용 중이나, 분산 세션 관리 및 API Rate Limiting으로의 확장은 예정 단계
- **MinIO**: 오브젝트 스토리지 첨부파일 관리 도입 예정

## 🚀 Quick Start

### 1. 환경 변수 설정
최상위 디렉토리의 `.env.example`을 복사하여 `.env` 파일을 작성합니다.
```bash
cp .env.example .env
```
필요한 경우 `.env` 파일 내 `JWT_SECRET`, `DB_USERNAME`, `DB_PASSWORD` 등을 프로젝트 실행 환경에 맞추어 수정합니다.

### 2. 인프라 실행 (Docker Compose)
```bash
docker-compose up -d
```
기본적으로 PostgreSQL 데이터베이스가 기동됩니다.

### 3. 백엔드 서버 구동
백엔드는 환경변수 주입을 필수로 요구합니다. CLI 또는 IDE에서 `.env` 환경변수를 로드하여 구동하거나 아래 명령어로 구동할 수 있습니다:
```bash
cd backend
export JWT_SECRET="your_jwt_secret_key_which_must_be_at_least_256_bits_long_for_hs256_security"
export DB_USERNAME="postgres"
export DB_PASSWORD="your_postgres_password_here"
./mvnw clean spring-boot:run
```

### 4. 프론트엔드 서버 구동
```bash
cd frontend
npm install
npm run dev
```

## 🔑 Key Features

**[구현 완료]**
- **Dynamic Domain / Schema Engine**: 하드코딩된 테이블 스키마 없이 Domain → Classification Node(트리, 필드 상속) → FieldGroup/Sector → FieldDefinition을 런타임에 동적으로 구성.
- **Record 승인 워크플로우**: 레코드 생성/수정/삭제 시 다단계 결재(Pending → Approved/Rejected), 결재자별 Before/After 비교 및 코멘트, 반려 시 원본 데이터 유지.
- **스키마 변경 승인 워크플로우 및 감사 로그**: 필드/노드 변경도 `ApprovalService`(SCHEMA_CHANGE 워크플로우)를 통해 결재를 거쳐 반영되며, `SchemaHistory`에 변경 전/후(Before/After) 스냅샷이 기록됨.
- **Data Quality Rule Engine**: NotNull, Regex, Range, Length, Enum, DateRange, CrossField, Unique, SpEL(커스텀 수식) 등 룰 기반 검증기. `ApprovalService`에서 레코드 기안/수정 시 **동기적으로 검증하여 위반 시 상신 자체를 차단**(hard-block)하며, `DqScheduledScanService`가 매일 새벽 크론(`0 0 2 * * ?`)으로 전체 재검사를 자동 수행.
- **Matching / 중복 검사 및 Golden Record 병합**: 정확 일치(EXACT)뿐 아니라 Jaro-Winkler 유사도 기반 **퍼지(FUZZY) 매칭**을 지원(`MatchingService`). 인바운드 연계 시 중복이 감지되면 `SourcePriority`(소스 시스템별 우선순위) 기준으로 **필드 단위 서바이버십 병합**을 수행하고 `RecordFieldSource`에 필드별 출처를 기록. 수동 병합(`/api/records/merge`, `/merge/auto`)과 서바이버십 규칙 관리 API도 제공.
- **필드 단위 소스 계보(Lineage) 노출**: `RecordFieldSource`를 프론트엔드 레코드 상세 화면(`RecordDetailDrawer.vue`)과 결재 상세 화면(`ApprovalDetailsViewer.vue`)에서 필드별 출처 시스템으로 시각화.
- **Excel 대량 업로드**: `ExcelUploader.vue` + `/api/nodes/{id}/records/batch`를 통한 템플릿 다운로드 → 컬럼 매핑 → 배치(100건 단위) 업로드.
- **외부 시스템 연계 (Integration)**: Inbound(채널별 시크릿 토큰 인증 Webhook)와 Outbound(Spring Integration 기반 HTTP/JDBC/Kafka/RabbitMQ 동적 라우팅).
- **RBAC 및 조직 구조**: Role/UserRole 기반 권한, Organization/Department/Team 조직도, 도메인·노드 단위 세부 권한(`DomainPermission`).
- **데이터 변경 감사(Audit)**: `RecordHistory`에 생성/수정/삭제 스냅샷을 버전과 함께 저장.
- **캐싱**: `RedisCacheConfig` + `@Cacheable`로 Domain 조회 결과를 캐싱(`DomainService`).
- **다국어(i18n)**: 한국어/영어 UI, 필드 자체 다국어 지원.

**[부분 구현 / 알려진 한계]**
- 병합 취소(**Un-merge**) 로직은 `RecordMergeService.unmergeRecord()`에 구현되어 있으나, **REST 컨트롤러에 노출되어 있지 않아** 현재 API/UI로는 호출할 수 없음.
- Excel 대량 업로드는 배치 단위로 서버에 전송만 할 뿐, **행(row) 단위로 어떤 데이터가 DQ 룰에 위반됐는지 짚어주는 검증 리포트는 없음**(실패 시 포괄적인 에러 메시지만 표시).
- `@Cacheable` 캐싱은 Domain 엔티티 조회에만 적용되어 있고, 트리를 순회해 계산하는 **EffectiveFields(유효 필드) 조회는 캐싱되지 않음**.
- 매칭 후보 검토 큐(`match-candidates.vue`, `MatchCandidateController`)는 있으나, 스튜어드의 검토 결과(오탐 여부)가 매칭 룰 임계값에 자동 반영되는 피드백 루프는 없음.
- 암호화(`FieldEncryptionService`)는 애플리케이션 전역에서 **단일 정적 마스터 키**(환경변수)를 사용하며, 필드별/도메인별 키 분리나 외부 KMS(AWS KMS, Vault 등) 연동은 없음.

## 💡 MDM 기능 개선 제안 (Proposed Enhancements)
아래는 실제 백엔드/프론트엔드 소스코드를 직접 확인해 검증한, **현재 미구현이거나 부분적으로만 구현된** 개선 과제입니다. (이미 구현된 기능은 위 [🔑 Key Features](#-key-features) 참고)

### 1. 매칭 & 골든 레코드(Match / Merge)
- **Un-merge API 노출**: `RecordMergeService.unmergeRecord()`는 이미 구현되어 있으나 `RecordMergeController`에 엔드포인트가 없어 UI/API로 호출 불가. 잘못된 병합을 되돌리는 필수 기능이므로 컨트롤러 매핑 및 화면 버튼 추가가 시급함.
- **매칭 후보 검토 피드백 루프**: `match-candidates.vue`에서 스튜어드가 "매칭 아님"으로 판정한 이력을 `MatchingRule.similarityThreshold` 튜닝에 참고 지표로 반영하는 기능은 없음.

### 2. 데이터 품질(DQ)
- **DQ 스코어 트렌드 대시보드**: `dq-dashboard.vue`는 현재 스냅샷 위주이며 시계열(추이) 차트가 없음. 도메인/노드별 품질 점수 변화를 추적할 수 있도록 개선.
- **Excel 대량 업로드 행 단위 검증 리포트**: 현재 배치 업로드 실패 시 포괄적 에러 메시지만 표시됨. 어떤 행이 어떤 DQ 룰에 위반됐는지 행 단위로 짚어주는 리포트 UI 추가.

### 3. 거버넌스 / 보안
- **암호화 키 관리(KMS) 연동**: `FieldEncryptionService`가 단일 정적 마스터 키(환경변수)만 사용 중. 필드별/도메인별 키 분리 및 AWS KMS·HashiCorp Vault 등 외부 KMS 연동 검토.
- **스키마 변경 시점(as-of) 조회**: `SchemaHistory`에 Before/After는 기록되지만, 과거 특정 시점의 EffectiveFields를 재구성해 보여주는 조회 API는 없음.

### 4. 분류체계 / 데이터 모델
- **다축 분류(Multi-axis) 지원**: 현재는 단일 트리(`parent_id`) 구조만 지원. "고용형태 × 부서"처럼 두 개 이상의 축이 동시에 필요한 경우를 위해 노드를 DAG로 확장하거나 별도 분류축(Axis) 개념 도입 검토.

### 5. 검색 / 성능
- **EffectiveFields 계산 캐싱**: `@Cacheable`은 현재 Domain 엔티티 조회에만 적용(`DomainService`). 트리를 매번 순회해 계산하는 `FieldDefinitionService.getEffectiveFields()`는 캐싱되지 않아, 노드/필드 수가 늘어나면 조회 비용 증가. Redis 캐시 + 스키마 변경 시 무효화(invalidate) 전략 적용 검토.
- **Elasticsearch/OpenSearch 연동**: `is_searchable` 표현식 인덱스만으로는 다중 필드 조합·전문 검색에 한계. 검색 패턴이 늘어나면 검색 엔진 도입 검토.

### 6. 연계(Integration)
- **아웃바운드/인바운드 실패 알림**: 연계 실패 시 `NotificationService`를 통한 관리자 자동 알림 연동이 확인되지 않음. `IntegrationLog` 실패율 임계치 초과 시 알림 발송 추가.
- **재시도 정책 고도화**: Dead-letter queue 및 채널별 재시도 백오프 정책 설정 기능 검토.

> 우선순위를 정해 구체적인 구현 방안(설계/코드)까지 진행하고 싶은 항목이 있다면 별도로 요청해 주세요.

## 🧪 Testing
백엔드는 `backend/src/test/java`에 컨트롤러/서비스/리포지토리 단위의 JUnit 테스트가 다수(43개 클래스) 구성되어 있습니다. 새 기능 추가나 버그 수정 시 관련 범위의 테스트를 먼저 확인하고, 필요한 경우 테스트를 추가/보완하는 것을 권장합니다.
```bash
cd backend
./mvnw test
```

## 📚 Data Model 개요
시스템은 정형화된 데이터 모델 대신, 런타임에 데이터 스키마를 구성할 수 있는 **동적 도메인 메타데이터(Dynamic Domain Metadata)** 구조를 사용합니다.

1. **Domain**: 최상위 기준(예: 임직원, 상품, 거래처). Identifier Field 및 Display Name Field 지정 필수.
2. **Classification Node**: 도메인 하위의 분류 트리 (예: 정규직, 계약직). 데이터와 결재 워크플로우의 기준 단위이며, 도메인 자체도 트리의 루트 노드로 취급됨.
3. **Sector & Group**: 화면 입력을 구성하는 탭(Sector)과 필드 그룹(FieldGroup). 노드 간에 공통으로 상속·재사용됨.
4. **Field (FieldDefinition)**: 실제 데이터 항목 정의. 텍스트/숫자/날짜/선택/다국어/파일/참조 등 다양한 타입을 지원하며, 정의된 노드와 모든 하위 노드에 상속됨.
5. **Record & Approval**: 생성/수정/삭제 요청이 `ApprovalRequest` + `ApprovalStep`으로 묶여 다단계 결재를 거치고, 최종 승인 후 `Record`가 버전과 함께 활성화(`ACTIVE`)됨.
6. **DQ Rule / DQ Violation**: 필드 또는 노드 단위로 정의되는 데이터 품질 규칙과, 위반 이력.
7. **Matching Rule**: 도메인/노드 단위로 정의되는 중복 판별 대상 필드 조합.
8. **Integration Channel / Integration Log**: 외부 시스템과의 연계 채널 설정(Inbound/Outbound, 타입, 매핑 규칙)과 연계 처리 이력.

## 🌐 API Base URL & 주요 Endpoint
- **Base URL**: `http://localhost:8080/api`

| 구분 | Method & Path | 설명 |
|---|---|---|
| 도메인/스키마 | `GET /domains` | 도메인 목록 조회 |
| 도메인/스키마 | `POST /domains` | 신규 도메인 생성 |
| 도메인/스키마 | `GET /domains/{id}/nodes` | 도메인 하위 분류 노드 조회 |
| 데이터 품질 | `GET /domains/{domainId}/dq-score` | 도메인 DQ 점수 조회 |
| 데이터 품질 | `POST /domains/{domainId}/dq-scan` | 도메인 전체 레코드 DQ 재검사 실행 |
| 데이터 품질 | `GET /domains/{domainId}/dq-violations` | DQ 위반 목록 조회 (페이징) |
| 레코드 | `POST /nodes/{nodeId}/records` | 데이터 생성 (결재 기안) |
| 레코드 | `GET /records/domain/{domainId}` | 도메인 기준 레코드 조회 (검색·페이징) |
| 레코드 | `POST /records/{id}/update-request` | 레코드 수정 요청 (결재 기안) |
| 레코드 | `POST /records/{id}/delete-request` | 레코드 삭제 요청 (결재 기안) |
| 결재 | `GET /approval-requests/todos` | 내 결재함(대기 목록) 조회 |
| 결재 | `POST /approval-requests/steps/{stepId}/approve` | 단계별 결재 승인 |
| 연계 | `POST /integration/inbound/{channelId}` | 외부 시스템 인바운드 데이터 수신 (채널 시크릿 인증) |
| 연계 | `GET /admin/integration/channels` | 연계 채널 목록 조회 |

## 🤝 Contributing
기여 방법, 브랜치 전략, 커밋 규약 등은 [`CONTRIBUTING.md`](./CONTRIBUTING.md)를 참고해 주세요.
