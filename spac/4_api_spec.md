# 4. REST API & WebSocket 명세서 (API Specifications)

본 문서는 백엔드(Spring Boot 4.1.0)에 구현된 **94개 컨트롤러의 전체 REST API 및 STOMP WebSocket 엔드포인트**에 대한 종합 명세서이다. (기본 Base URL: `/api`)

---

## 4.1 도메인, 다축 분류 & 스키마 관리

| 구분 | HTTP Method | Endpoint | 설명 |
|---|---|---|---|
| **도메인** | `GET` | `/api/domains` | 도메인 전체 목록 조회 |
| **도메인** | `POST` | `/api/domains` | 신규 도메인 생성 |
| **도메인** | `GET` | `/api/domains/{id}` | 도메인 상세 메타데이터 조회 |
| **도메인** | `PUT` | `/api/domains/{id}` | 도메인 메타데이터 수정 |
| **도메인** | `DELETE` | `/api/domains/{id}` | 도메인 삭제 |
| **도메인 패키지** | `GET` | `/api/domains/{id}/export-package` | 도메인 전체 스키마/룰 패키지 내보내기 |
| **도메인 패키지** | `POST` | `/api/domains/import-package` | 도메인 패키지 가져오기 |
| **도메인 스냅샷** | `GET, POST` | `/api/domains/{id}/snapshots` | 도메인 스냅샷 목록 조회 및 수동 생성 |
| **분류축** | `GET` | `/api/domains/{domainId}/axes` | 도메인의 전체 분류축 목록 조회 |
| **분류축** | `POST` | `/api/domains/{domainId}/axes` | 신규 분류축 생성 |
| **분류축** | `GET, PUT, DELETE`| `/api/domains/{domainId}/axes/{axisId}` | 분류축 상세 조회, 수정, 삭제 |
| **분류 노드** | `GET` | `/api/domains/{domainId}/nodes/tree` | 분류 노드 계층 트리 조회 |
| **분류 노드** | `POST` | `/api/domains/{domainId}/nodes` | 특정 부모 노드 하위에 신규 노드 추가 |
| **분류 노드** | `PUT, DELETE` | `/api/domains/{domainId}/nodes/{nodeId}` | 노드 메타데이터 수정 및 논리적 삭제 |
| **필드 정의** | `GET, POST` | `/api/nodes/{nodeId}/fields` | 노드 직접 선언 필드 목록 조회 및 추가 |
| **유효 필드** | `GET` | `/api/nodes/{nodeId}/fields/effective` | 상속 포함 최종 유효 필드(EffectiveFields) 조회 |
| **과거 필드** | `GET` | `/api/nodes/{nodeId}/fields/effective/as-of` | 과거 특정 시점(As-Of) 유효 필드 재구성 조회 |
| **구획(Sector)**| `GET, POST, PUT, DELETE` | `/api/domains/{domainId}/sectors` | 화면 탭(Sector) 및 필드 그룹(FieldGroup) 관리 |
| **스키마 영향도**| `GET` | `/api/domains/{domainId}/nodes/{nodeId}/schema-impact` | 필드 변경/삭제 시 레코드/연계 영향도 분석 |
| **스키마 시뮬** | `POST` | `/api/domains/{domainId}/schema/simulate` | 스키마 구조 변경 가상 시뮬레이션 |
| **스키마 호환** | `POST` | `/api/domains/{domainId}/schema/compatibility-check` | 이전 버전과의 역방향 호환성 검사 |
| **스키마 이력** | `GET` | `/api/domains/{domainId}/schema-history` | 스키마 변경 Before/After 이력 조회 |

---

## 4.2 레코드, 대량 처리, 타임머신 & 해시체인 원장

| 구분 | HTTP Method | Endpoint | 설명 |
|---|---|---|---|
| **레코드 기안** | `POST` | `/api/nodes/{nodeId}/records` | 레코드 생성 기안 (DQ 검증 & 중복 검사 동기 수행) |
| **레코드 조회** | `GET` | `/api/nodes/{nodeId}/records` | 특정 노드 소속 레코드 목록 조회 (페이징/정렬) |
| **레코드 단건** | `GET` | `/api/records/{id}` | 레코드 단건 상세 조회 |
| **레코드 수정** | `POST` | `/api/records/{id}/update-request` | 레코드 수정 결재 기안 |
| **레코드 삭제** | `POST` | `/api/records/{id}/delete-request` | 레코드 삭제 결재 기안 |
| **전역 레코드** | `GET` | `/api/records/domain/{domainId}` | 도메인 전체 레코드 다차원 검색 및 페이징 |
| **다축 서브매핑**| `GET, POST` | `/api/records/{id}/secondary-nodes` | 레코드의 타 분류축 서브 노드 매핑 조회 및 변경 |
| **Excel 사전검증**| `POST` | `/api/nodes/{nodeId}/records/batch-validate`| 대량 업로드 엑셀 행 단위 DQ 사전 검증 리포트 |
| **대량 임포트** | `POST` | `/api/records/bulk-import/upload` | 대량 레코드 비동기 임포트 작업 등록 |
| **대량 익스포트** | `POST` | `/api/export/domains/{domainId}/async` | 대용량 레코드 비동기 엑셀 내보내기 |
| **배치 작업상태**| `GET` | `/api/batch/{jobId}/status` | 비동기 배치 작업 진행률 및 상태 폴링 |
| **레코드 이력** | `GET` | `/api/records/{id}/history` | 레코드 버전별 전체 변경 이력 조회 |
| **레코드 계보** | `GET` | `/api/records/{id}/lineage` | 필드 수준 소스 시스템 출처 계보 조회 |
| **레코드 타임머신**| `GET` | `/api/records/{recordId}/timemachine/as-of` | 과거 특정 시점의 레코드 스냅샷 조회 |
| **타임머신 롤백**| `POST` | `/api/records/{recordId}/timemachine/rollback` | 레코드를 과거 특정 버전으로 롤백 |
| **해시체인 원장**| `GET` | `/api/records/{recordId}/ledger` | 블록체인형 해시체인 변경 원장 목록 조회 |
| **원장 무결성** | `GET` | `/api/records/{recordId}/ledger/verify` | 해시체인 연결 무결성 수학적 검증 |
| **골든 레코드** | `GET` | `/api/records/golden-record/{id}` | 마스터 골든 레코드 통합 뷰 조회 |

---

## 4.3 결재(Approval) 워크플로우, 위임, 동적 라우팅 & 샌드박스

| 구분 | HTTP Method | Endpoint | 설명 |
|---|---|---|---|
| **결재 목록** | `GET` | `/api/approval-requests` | 전체 결재 요청 목록 조회 (필터링) |
| **내 결재함** | `GET` | `/api/approval-requests/todos` | 내가 승인해야 할 대기 목록 조회 |
| **내 기안함** | `GET` | `/api/approval-requests/my-requests` | 내가 기안한 요청 목록 조회 |
| **결재 상세** | `GET` | `/api/approval-requests/{id}` | 결재 기안 상세 데이터 및 단계별 진행 상황 |
| **단계 승인** | `POST` | `/api/approval-requests/steps/{stepId}/approve` | 해당 결재 단계 승인 처리 |
| **단계 반려** | `POST` | `/api/approval-requests/steps/{stepId}/reject` | 해당 결재 단계 반려 처리 |
| **관리자 개입** | `POST` | `/api/approval-requests/{id}/admin-approve` | 최고 관리자 직권 강제 승인 |
| **관리자 반려** | `POST` | `/api/approval-requests/{id}/admin-reject` | 최고 관리자 직권 강제 반려 |
| **결재 위임** | `GET, POST, DELETE` | `/api/approvals/delegations` | 결재 권한 위임 설정 조회, 등록, 해제 |
| **에스컬레이션** | `POST` | `/api/approvals/escalate/{stepId}` | 지연된 결재 단계 상위자 에스컬레이션 |
| **동적 라우팅** | `GET, POST, PUT` | `/api/approvals/routing-templates` | 조건부 동적 결재선 템플릿 관리 |
| **결재 샌드박스**| `POST` | `/api/approvals/{requestId}/sandbox-preview`| 결재 승인 전 사전 영향 가상 시뮬레이션 |
| **반려 원인분석**| `GET` | `/api/approvals/analytics/rejections` | 결재 반려 통계 및 주요 사유 분석 |
| **워크플로우설정**| `GET, POST, PUT` | `/api/workflow-configs` | 도메인/노드별 워크플로우 결재선 설정 |

---

## 4.4 데이터 품질(DQ), AI 추천 & 자율 치료

| 구분 | HTTP Method | Endpoint | 설명 |
|---|---|---|---|
| **DQ 룰 관리** | `GET, POST, PUT, DELETE`| `/api/fields/{fieldId}/dq-rules`, `/api/dq-rules/{ruleId}` | 필드별 DQ 검증 룰 CRUD |
| **DQ 룰 테스트** | `POST` | `/api/dq-rules/validate` | DQ 룰 실시간 정규식/수식 테스트 |
| **AI 룰 추천** | `GET` | `/api/v1/dq/{domainId}/recommendations` | 프로파일링 기반 AI DQ 룰 추천 목록 |
| **DQ 점수 조회** | `GET` | `/api/domains/{domainId}/dq-score` | 도메인 현재 품질 점수 및 위반 통계 |
| **시계열 트렌드**| `GET` | `/api/domains/{domainId}/dq-score/trend` | 도메인 DQ 점수 시계열 변화 추이 |
| **최근 스냅샷** | `GET` | `/api/domains/{domainId}/dq-score/recent` | 최근 N건 DQ 스냅샷 데이터 조회 |
| **DQ 전체 스캔** | `POST` | `/api/domains/{domainId}/dq-scan` | 도메인 전체 레코드 수동 DQ 스캔 트리거 |
| **DQ 위반 목록** | `GET` | `/api/domains/{domainId}/dq-violations` | 도메인 내 위반 레코드 목록 페이징 조회 |
| **DQ 치료 제안** | `GET` | `/api/domains/{domainId}/dq/remediation/proposals` | 위반 데이터에 대한 자동 치료 제안 조회 |
| **DQ 치료 실행** | `POST` | `/api/domains/{domainId}/dq/remediation/apply` | 선택된 치료 제안 일괄 반영 |
| **자율 정제 제안**| `GET, POST` | `/api/domains/{domainId}/dq/cleansing-proposals` | AI 기반 자율 데이터 정제 제안 및 적용 |
| **데이터 프로파일**| `POST` | `/api/domains/{domainId}/profiling` | 도메인 전체 필드 데이터 프로파일링 실행 |
| **비즈니스 룰** | `GET, POST` | `/api/domains/{domainId}/business-rules` | 도메인 복합 비즈니스 룰 관리 |

---

## 4.5 매칭, 골든 레코드, 서바이버십 & Un-merge

| 구분 | HTTP Method | Endpoint | 설명 |
|---|---|---|---|
| **매칭 룰 관리** | `GET, POST, PUT, DELETE`| `/api/domains/{domainId}/matching-rules` | 중복 검사 매칭 룰 CRUD |
| **매칭 피드백** | `GET` | `/api/domains/{domainId}/matching-rules/feedback-summary` | 스튜어드 피드백 통계 및 권장 임계값 |
| **매칭 후보 조회**| `GET` | `/api/domains/{domainId}/match-candidates` | 중복 의심 후보군 목록 조회 |
| **중복 확정** | `POST` | `/api/match-candidates/{id}/confirm` | 매칭 후보 중복 확정 |
| **오탐 반려** | `POST` | `/api/match-candidates/{id}/reject` | 매칭 후보 오탐 반려 |
| **수동 레코드 병합**| `POST` | `/api/records/merge` | 2개 이상의 레코드 수동 병합 |
| **자동 병합** | `POST` | `/api/records/merge/auto` | Survivorship 룰 기반 자동 병합 |
| **Un-merge (복원)**| `POST` | `/api/records/{id}/unmerge` | 병합된 레코드 원상 복구 및 활성화 |
| **서바이버십 룰** | `GET, PUT` | `/api/records/domains/{domainId}/survivorship-rules` | 도메인 필드별 서바이버십 규칙 관리 |

---

## 4.6 외부 연계, DLQ & 실시간 스트리밍

| 구분 | HTTP Method | Endpoint | 설명 |
|---|---|---|---|
| **인바운드 수신**| `POST` | `/api/integration/inbound/{channelId}` | 외부 시스템 인바운드 수신 (시크릿 토큰 인증) |
| **연계 채널 관리**| `GET, POST, PUT, DELETE`| `/api/admin/integration/channels` | 연계 채널 CRUD 및 접속 설정 |
| **연계 테스트** | `POST` | `/api/admin/integration/channels/{id}/test` | 연계 채널 송수신 즉시 테스트 |
| **연계 로그 조회**| `GET` | `/api/admin/integration/logs` | 전체 연계 송수신 실행 로그 |
| **DLQ 로그 조회** | `GET` | `/api/admin/integration/logs/dead-letter` | Dead-Letter Queue 격리 로그 목록 |
| **DLQ 단건 재시도**| `POST` | `/api/admin/integration/logs/{logId}/retry` | 연계 실패 1건 수동 재시도 |
| **DLQ 전체 재시도**| `POST` | `/api/admin/integration/logs/dead-letter/retry-all` | DLQ 전건 일괄 재시도 |
| **API 키 관리** | `GET, POST, DELETE` | `/api/integration/api-keys` | 외부 연계용 API Key 발급 및 ACL 제어 |
| **웹훅 구독 관리**| `GET, POST, DELETE` | `/api/integration/webhooks` | 이벤트 웹훅 구독 등록 및 상태 관리 |
| **CDC 스트리밍** | `GET, POST` | `/api/domains/{domainId}/cdc` | CDC 실시간 변경 이벤트 스트림 제어 |
| **크로스 파이프라인**| `GET, POST` | `/api/integration/pipelines` | 도메인 간 동기화 파이프라인 관리 |
| **자가치유 모니터**| `GET, POST` | `/api/system/pipeline-healing` | 파이프라인 자가 치유 상태 및 복구 제어 |

---

## 4.7 보안, 감사, 컴플라이언스 & 라이프사이클

| 구분 | HTTP Method | Endpoint | 설명 |
|---|---|---|---|
| **마스킹 해제** | `POST` | `/api/sensitive-data/unmask` | 마스킹 필드 원본 열람 요청 (사유 필수) |
| **열람 감사 로그**| `GET` | `/api/admin/sensitive-data/access-logs` | 민감 데이터 원본 열람 감사 로그 조회 |
| **마스킹 정책** | `GET, PUT` | `/api/records/{recordId}/masked` | 마스킹 규칙 및 권한별 마스킹 정책 |
| **컴플라이언스** | `GET` | `/api/compliance/report` | GDPR / 개인정보보호 규제 준수율 리포트 |
| **규제 감사 로그**| `GET` | `/api/compliance/regulatory-audit` | 규제 대응용 시스템 감사 로그 |
| **이상 접근 탐지**| `GET` | `/api/security/anomaly-detection` | 비정상 대량 다운로드 및 이상 접근 탐지 |
| **대량 변동 레이더**| `GET` | `/api/system/volume-radar` | 급격한 데이터 변동량 AI 레이더 모니터링 |
| **콜드 아카이빙**| `GET, POST` | `/api/system/archives` | 콜드 스토리지 아카이빙 목록 및 수동 실행 |
| **데이터 보존정책**| `GET, POST, PUT` | `/api/domains/{domainId}/retention` | 데이터 보존 주기 정책 설정 및 만료 처리 |
| **참조 무결성** | `GET, POST` | `/api/domains/{domainId}/integrity` | 도메인 간 참조 무결성 검증 및 리포트 |
| **신선도 히트맵**| `GET` | `/api/system/freshness-heatmap` | 도메인/노드별 데이터 갱신 신선도 히트맵 |
| **데이터 SLA** | `GET, POST` | `/api/system/sla-contracts` | 데이터 SLA 계약 관리 및 위반 현황 |
| **자산 가치평가**| `GET` | `/api/catalog/valuation` | 마스터 데이터 자산의 금전적 가치 평가 |

---

## 4.8 지능형 AI, 검색 & 비즈니스 용어사전

| 구분 | HTTP Method | Endpoint | 설명 |
|---|---|---|---|
| **전역 통합 검색**| `GET` | `/api/v1/search` | OpenSearch 기반 도메인/레코드 전문 검색 |
| **자연어 스마트쿼리**| `POST` | `/api/domains/{domainId}/smart-query` | 자연어 질의를 파싱한 스마트 레코드 검색 |
| **비정형 추출** | `POST` | `/api/domains/{domainId}/ai/extract-unstructured` | 비정형 문서/텍스트에서 필드 자동 추출 |
| **비즈니스 용어사전**| `GET, POST, PUT, DELETE`| `/api/business-terms` | 표준 비즈니스 용어사전 CRUD 및 필드 매핑 |
| **시맨틱 온톨로지**| `GET, POST` | `/api/ontology` | 도메인 간 관계 온톨로지 지식 그래프 관리 |
| **거버넌스 코파일럿**| `POST` | `/api/governance/copilot` | 거버넌스 규정 및 메타데이터 AI 질의응답 |
| **성숙도 평가** | `GET` | `/api/governance/maturity-evaluation` | 전사 MDM 거버넌스 성숙도 평가 점수 |
| **다국어 동기화**| `POST` | `/api/domains/{domainId}/multilingual/sync` | 메타데이터 다국어 일괄 동기화/번역 |

---

## 4.9 플랫폼 기초, 사용자, 조직 & 실시간 협업

| 구분 | HTTP Method | Endpoint | 설명 |
|---|---|---|---|
| **로그인 / 토큰**| `POST` | `/api/auth/login`, `/api/auth/refresh` | 자체 JWT 로그인 및 토큰 재발급 |
| **사용자 관리** | `GET, POST, PUT` | `/api/users` | 사용자 CRUD 및 역할(Role) 할당 |
| **조직/부서 관리**| `GET, POST, PUT, DELETE`| `/api/organizations` | 조직, 부서, 팀 계층 관리 |
| **메뉴 관리** | `GET, POST, PUT, DELETE`| `/api/menus` | 동적 사이드바 메뉴 트리 관리 |
| **공통 코드 관리**| `GET, POST, PUT, DELETE`| `/api/code-groups`, `/api/code-groups/{code}/details` | 공통 코드 그룹 및 상세 코드 관리 |
| **권한 매트릭스**| `GET, PUT` | `/api/permissions` | 메뉴 및 도메인별 권한 매트릭스 관리 |
| **시스템 초기설치**| `POST, GET` | `/api/system/install`, `/api/system/install-status` | 시스템 초기 구성 위저드 |
| **시스템 진단** | `GET` | `/api/system/diagnostics` | 글로벌 인프라/JVM/DB 리소스 통합 진단 |
| **마스터 오케스트레이터**| `GET, POST` | `/api/system/master-orchestrator` | 전체 스케줄러/배치 파이프라인 통합 제어 |
| **채팅 메시지** | `GET, POST` | `/api/chat/messages`, `/api/chat/rooms` | 채팅방 조회 및 메시지 송수신 |
| **실시간 번역** | `POST` | `/api/chat/translate` | 채팅 메시지 다국어 실시간 번역 |
| **시스템 라디오**| `POST, GET` | `/api/music/broadcast` | 유튜브 음악 방송 제어 및 재생 상태 |
| **알림 조회** | `GET, PUT` | `/api/notifications` | 시스템 알림 목록 조회 및 읽음 처리 |
| **SSE 알림 스트림**| `GET` | `/api/notifications/stream` | Server-Sent Events 기반 실시간 알림 |
| **STOMP 웹소켓** | `WS` | `/ws-stomp` | STOMP 웹소켓 연결 (채팅, 알림) |
| **파일 업/다운로드**| `POST, GET` | `/api/files/upload`, `/api/files/download/{id}` | MinIO 파일 업로드 및 다운로드 |
| **모니터링 메트릭**| `GET` | `/actuator/prometheus` | Prometheus 메트릭 수집 엔드포인트 |
