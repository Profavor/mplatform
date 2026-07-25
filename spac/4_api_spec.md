# 6. API 스펙 (실제 구현 기준, Base URL: `/api`)

> 최초 설계(제안) 대비 실제 라우팅 경로가 다르거나(예: 노드 트리는 `/nodes/{id}/tree`가 아닌 `/domains/{domainId}/nodes/tree`), Sector/FieldGroup, DQ, Matching/Merge 등 설계 문서에 없던 엔드포인트가 다수 추가되었다.

### 도메인 / 스키마
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/domains` | 도메인 생성 |
| GET | `/domains` | 도메인 목록 |
| GET | `/domains/{id}` | 도메인 상세 |
| PUT | `/domains/{id}` | 도메인 수정 |
| GET / POST / PUT | `/domains/{domainId}/fields`, `/fields/page`, `/fields/{fieldId}` | 도메인 루트 필드 조회/생성/수정 |
| GET / POST / PUT / DELETE | `/domains/{domainId}/sectors`, `/sectors/{sectorId}` | 화면 탭(Sector) 관리 *(설계 문서에 없던 실제 구현)* |
| GET / POST / PUT / DELETE | `/domains/{domainId}/groups`, `/groups/{groupId}` | 필드 그룹(FieldGroup) 관리 *(설계 문서에 없던 실제 구현)* |
| POST | `/domains/{domainId}/nodes` | 노드 생성 |
| GET | `/domains/{domainId}/nodes/tree` | 도메인 하위 트리 조회 |
| PUT | `/domains/{domainId}/nodes/{nodeId}` | 노드 메타데이터 수정 |
| POST / GET / PUT | `/nodes/{nodeId}/fields`, `/fields/effective`, `/fields/effective/page`, `/fields/{fieldId}` | 노드 필드 정의 CRUD 및 EffectiveFields 조회 |

### 데이터 품질(DQ)
| Method | Endpoint | 설명 |
|---|---|---|
| GET / POST / PUT / DELETE | `/fields/{fieldId}/dq-rules`, `/dq-rules/{ruleId}` | DQ 룰 CRUD |
| POST | `/dq-rules/validate` | DQ 룰 검증(테스트) |
| GET | `/domains/{domainId}/dq-score` | 도메인 DQ 점수 조회 |
| POST | `/domains/{domainId}/dq-scan` | 전체 재검사 수동 트리거 (매일 새벽 자동 스캔도 별도 동작) |
| GET | `/domains/{domainId}/dq-violations` | DQ 위반 목록 (페이징) |

### 레코드 / 결재
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/nodes/{nodeId}/records` | 레코드 생성 요청(기안) — DQ 검증 + 중복 검사 동기 수행 |
| POST | `/nodes/{nodeId}/records/batch` | 배치 생성(Excel 대량 업로드 등) |
| GET | `/nodes/{nodeId}/records` | 노드 기준 레코드 목록 조회 |
| GET | `/records/{id}` | 레코드 단건 조회 |
| GET | `/records/domain/{domainId}` | 도메인 기준 레코드 목록 조회(검색·페이징) |
| POST | `/records/{id}/update-request` | 레코드 수정 요청(기안) |
| POST | `/records/{id}/delete-request` | 레코드 삭제 요청(기안) |
| GET | `/approval-requests/effective-workflow/{nodeId}` | 노드에 적용될 결재 라인 미리보기 |
| GET | `/approval-requests`, `/approval-requests/all`, `/approval-requests/todos`, `/approval-requests/my-requests` | 결재 요청/내 결재함/내 기안함 조회 |
| GET | `/approval-requests/{id}` | 결재 요청 상세 |
| POST | `/approval-requests/steps/{stepId}/approve` | 단계 승인 |
| POST | `/approval-requests/steps/{stepId}/reject` | 단계 반려 |
| POST | `/approval-requests/steps/{stepId}/admin-approve` | (관리자 전용) 대리 승인 |
| POST | `/approval-requests/steps/{stepId}/admin-reject` | (관리자 전용) 대리 반려 |

### 매칭 / Golden Record
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/domains/{domainId}/match-candidates` | 매칭 후보 목록 조회 |
| POST | `/match-candidates/{id}/confirm` | 매칭 후보를 중복으로 확정 |
| POST | `/match-candidates/{id}/reject` | 매칭 후보를 오탐(비중복)으로 반려 |
| POST | `/records/merge` | 지정 레코드들을 수동 병합 |
| POST | `/records/merge/auto` | Survivorship 규칙 기반 자동 병합 |
| GET / PUT | `/records/domains/{domainId}/survivorship-rules` | 서바이버십 규칙 조회/수정 |
| — | *(미구현)* | 병합 취소(Un-merge) — 서비스 로직은 있으나 엔드포인트 없음, 조기 추가 권장 |

### 연계(Integration)
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/integration/inbound/{channelId}` | 외부 시스템 인바운드 수신(채널 시크릿 인증) |
| GET / POST / PUT / DELETE | `/admin/integration/channels`, `/channels/{id}` | 연계 채널 CRUD |
| POST | `/admin/integration/channels/{id}/test` | 연결 테스트 |
