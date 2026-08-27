# 1. 개요 (Platform Overview)

> **문서 버전 (Version)**: v1.1.0

## 1.1 플랫폼 비전 및 목표
본 플랫폼은 기업 및 조직 내에 파편화되어 분산 관리되던 마스터 데이터(Customer, Product, Vendor, Employee, Organization 등)를 단일 플랫폼으로 통합하고 정제하여 **전사 단일 진실 공급원(Single Source of Truth, Golden Record)**을 확립하기 위한 **엔터프라이즈 마스터 데이터 관리(Master Data Management, MDM) 시스템**이다.

초기의 단순 도메인/코드 관리 수준을 넘어, 테이블 DDL 변경 없이 런타임에 유연하게 구조를 확장하는 **메타데이터 드리븐(Metadata-Driven) 동적 스키마 엔진**, AI 기반의 **데이터 품질(DQ) 자율 정제 및 치료**, 블록체인 원리 기반의 **해시체인(Hash-Chain) 불변 감사 원장**, **CDC(Change Data Capture) 실시간 스트리밍**, **HashiCorp Vault Transit 하드웨어급 암호화**, **다중 리전(Multi-Region) 충돌 해결 및 멀티 테넌트(Multi-Tenant) 아키텍처**를 완비한 차세대 MDM 생태계를 제공한다.

---

## 1.2 다축 분류체계 (Multi-axis Classification) 모델
관리 대상 마스터 데이터는 **도메인(Domain)** 단위로 최상위 분류되며, 각 도메인 하위에 하나 이상의 **분류축(ClassificationAxis)**을 생성할 수 있다.

```text
[도메인: 임직원 (Employee)]
  │
  ├── [기본 분류축: 조직도 (isDefault = true)]
  │     ├── 영업본부 (Node)
  │     │     └── 영업1팀 (Node)
  │     └── 개발본부 (Node)
  │           ├── 프론트엔드팀 (Node)
  │           └── 백엔드팀 (Node)
  │
  ├── [서브 분류축 A: 고용 형태 (Employment Type)]
  │     ├── 정규직 (Node)
  │     ├── 계약직 (Node)
  │     └── 파견직 (Node)
  │
  └── [서브 분류축 B: 직군 분류 (Job Family)]
        ├── 엔지니어링 (Node)
        ├── 프로덕트/디자인 (Node)
        └── 비즈니스/세일즈 (Node)
```

- **주 분류 노드 (Primary Node)**: 레코드는 기본 분류축의 특정 노드(예: `백엔드팀`)에 주 소속으로 등록되며, 상위 조상 노드들로부터 정의된 필드를 상속받는다.
- **서브 분류 노드 매핑 (`RecordSecondaryNode`)**: 레코드는 주 소속 외에도 타 분류축의 노드(예: `정규직`, `엔지니어링`)에 다대다로 서브 매핑될 수 있어, 다차원 슬라이스 앤 다이스(Slice & Dice) 조회가 가능하다.

---

# 2. 12대 핵심 아키텍처 개념 정의

| 개념 | 영문 표기 | 세부 설명 |
|---|---|---|
| **동적 스키마 & 필드 상속** | Dynamic Schema & Field Inheritance | RDBMS 테이블 구조를 고정하지 않고 `Domain` → `ClassificationAxis` → `ClassificationNode` → `FieldDefinition` 계층을 통해 런타임에 동적으로 스키마를 구성하며, 상위 노드의 필드가 하위 노드로 자동 상속되는 아키텍처. |
| **유효 필드 2중 캐싱** | EffectiveFields Hybrid Caching | 노드 기준 상속된 최종 필드 수집 결과(`getEffectiveFields`)를 Spring Cache를 통해 **Redis(분산 캐시)**에 적재하며, Redis 장애/미연결 시 **Local In-Memory ConcurrentMap**으로 자동 폴백(Fallback)하는 고성능 캐싱 체계. |
| **데이터 품질 자율 치유** | DQ Remediation & Autonomous Cleansing | 10종의 정밀 룰 엔진과 AI 기반 룰 추천(`DqRecommendationService`)을 바탕으로, 데이터 위반 건에 대해 자율 정제 제안을 생성하고 원클릭 자동 교정을 수행하는 지능형 품질 관리 체계. |
| **퍼지 매칭 & 골든 레코드** | Fuzzy Matching & Golden Record Survivorship | Jaro-Winkler 유사도 알고리즘 기반 중복 탐지, 스튜어드 피드백 학습을 통한 임계값 최적화, 그리고 소스 시스템 우선순위(`SourcePriority`) 기반 필드 단위 서바이버십 병합 및 오병합 복원(`Un-merge`) 체계. |
| **지능형 결재 & 거버넌스** | Advanced Approval Workflow | 데이터 생성/수정/삭제 시 다단계 결재선, 결재 위임(`ApprovalDelegation`), SLA 미준수 시 상위자 에스컬레이션(`ApprovalEscalation`), 조건부 동적 라우팅(`ApprovalRoutingTemplate`), 승인 전 영향 시뮬레이션(`ApprovalSandbox`)을 포함하는 전사 결재 거버넌스. |
| **불변 해시체인 감사 원장** | Hash-Chain Immutable Audit Ledger | 레코드의 모든 변경 이력을 이전 블록의 SHA-256 해시값과 체이닝하여 불변 원장(Ledger)으로 관리함으로써, 데이터 위변조를 원천 방지하고 무결성을 수학적으로 입증하는 감사 체계. |
| **제로 트러스트 암호화 & Vault** | Zero-Trust Encryption & Vault Transit | 32바이트 AES 하이브리드 대칭키 암호화와 함께 검색 전용 SHA-256 HMAC Blind Index를 분리 생성하여 평문 노출 없는 일치 검색을 지원하며, HashiCorp Vault Transit HSM 엔진과의 연동을 완비한 암호화 체계. |
| **CDC 스트리밍 & 자가 치유** | CDC Streaming & Self-Healing Pipeline | Kafka 기반 실시간 데이터 변경 캡처(CDC) 스트림을 전파하고, 외부 연계 파이프라인 장애 시 지수 백오프/DLQ 격리 및 대체 경로로 자동 전환하는 자가 치유(Self-Healing) 연계 파이프라인. |
| **레코드 타임머신 & 라이프사이클** | Record Time Machine & Lifecycle | 과거 특정 시점(As-Of)의 레코드 및 스키마 상태를 완벽 복원하는 타임머신 기능, 장기 미사용 데이터의 콜드 스토리지(MinIO/S3) 압축 아카이빙, 보존 기한 만료 데이터 자동 정리 정책. |
| **비즈니스 용어사전 & 온톨로지** | Business Glossary & Semantic Ontology | 전사 표준 비즈니스 용어를 정의하고 물리 메타데이터 필드와 양방향 매핑하며, 도메인 간 관계를 온톨로지 지식 그래프로 모델링하여 자연어 스마트 쿼리 및 비정형 데이터 추출을 지원하는 지능화 체계. |
| **Zero-Fallback i18n & 타임존** | Zero-Fallback i18n & Timezone Governance | 소스코드 내 하드코딩된 라벨이나 임의 폴백 문자열을 원천 배제하고 100% 로케일 사전에 동기화하며, 사용자 개인화 타임존 쿠키와 방어 헬퍼 함수(`parseDate`)를 통해 시차 왜곡을 완벽 방지하는 UI 규약. |
| **TDD & Nuxt 정적 컴파일 검증** | TDD & Static AST Build Pipeline | 백엔드의 고정 암호문 회귀 검증(Golden Sample Validation)을 포함한 JUnit 5 테스트와, 프론트엔드의 유닛 테스트 및 Nuxt 정적 번들 빌드(`npm run build`) 결합 검증을 통해 런타임 오류를 사전에 100% 억제하는 CI/CD 무결성 체계. |

---

# 3. 플랫폼 개발 규모 및 시스템 현황 (Platform Scale & Metrics)

| 구분 | 항목 | 규모 / 스펙 | 세부 설명 |
|---|---|---|---|
| **버전 정보** | 플랫폼 릴리즈 버전 | **v1.1.0** | 차세대 엔터프라이즈 MDM 아키텍처 v1.1.0 프로덕션 릴리즈 |
| **백엔드 도메인 모델** | JPA 엔티티 (Entities) | **67개 (+ 7 enums)** | PostgreSQL 15 기반 마스터 데이터, 동적 스키마, 거버넌스, 연계, 감사 엔티티 및 Enum |
| **백엔드 컨트롤러** | API 컨트롤러 (Controllers) | **94개** | REST API & STOMP WebSocket 전수 컨트롤러 |
| **백엔드 서비스** | 서비스 클래스 (Service classes) | **144개** | 동적 상속, DQ 자율 치유, 매칭, 결재, 해시체인, 암호화 및 연계 서비스 |
| **백엔드 테스트** | JUnit 5 테스트 (Backend tests) | **228개** | 고정 암호문 회귀 검증(Golden Sample) 및 단위/통합 테스트 스위트 |
| **프론트엔드 테스트** | Vitest 테스트 스펙 (Frontend test specs) | **173개** | 단위 및 컴포넌트 유닛 테스트 스펙 |
| **프론트엔드 화면** | Nuxt 3 페이지 (Frontend pages) | **25개** | 마스터 데이터, 결재, 거버넌스, 모니터링 전용 페이지 |
| **프론트엔드 컴포넌트** | Vue 3 컴포넌트 (Frontend components) | **161개** | AG-Grid, Drawer, Dialog, 차트 등 재사용 가능한 고성능 UI 컴포넌트 |
| **인프라 & 배포** | K8s 매니페스트 (K8s manifests) | **19종** | Production 배포를 위한 Ingress, StatefulSet, Deployment, ConfigMap 등 매니페스트 |
