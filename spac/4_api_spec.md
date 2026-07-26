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
| GET | `/approval-requests/effective-workflow/{nodeId}?actionType=` | 해당 노드+행위유형에 유효(비어있지 않은) 결재선이 존재하는지 여부(boolean)만 반환 |
| GET | `/approval-requests/available-workflows/{nodeId}?actionType=` | 해당 노드+행위유형에 적용 가능한 `workflow_config` 후보 전체 목록 조회 *(설계 문서에 없던 실제 구현, 다중 서식 선택용)* |
| GET | `/approval-requests/effective-permission/{nodeId}?actionType=&workflowId=` | 로그인 사용자 기준으로 적용될 규칙명(`ruleName`), 편집가능/읽기전용/숨김 필드 목록을 반환 *(설계 문서에 없던 실제 구현)*. `workflowId` 생략 시 7.3절 해석 순서로 자동 결정 |
| GET | `/approval-requests`, `/approval-requests/all`, `/approval-requests/todos`, `/approval-requests/my-requests` | 결재 요청/내 결재함/내 기안함 조회 |
| GET | `/approval-requests/{id}` | 결재 요청 상세 |
| POST | `/approval-requests/steps/{stepId}/approve` | 단계 승인 |
| POST | `/approval-requests/steps/{stepId}/reject` | 단계 반려 |
| POST | `/approval-requests/steps/{stepId}/admin-approve` | (관리자 전용) 대리 승인 |
| POST | `/approval-requests/steps/{stepId}/admin-reject` | (관리자 전용) 대리 반려 |

### 워크플로우 설정 (Workflow Config) *(설계 문서에 없던 실제 구현)*
결재선/필드 권한 규칙(`workflow_config`) 자체를 관리하는 어드민 전용 API. 필요 권한: `admin:read`/`admin:write` 또는 `workflow:read`/`workflow:write`.

| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/workflow-configs/page?page=&size=&actionType=&domainId=&nodeId=&query=` | 전체 규칙 페이징 조회(관리 화면 목록용). `actionType=ALL` 또는 생략 시 전체 |
| GET | `/workflow-configs/domain/{domainId}`, `/domain/{domainId}/all` | 해당 도메인의 도메인 레벨(노드 미지정) 규칙 목록 |
| GET | `/workflow-configs/node/{nodeId}` | 해당 노드에 직접 지정된 규칙 목록 |
| POST | `/workflow-configs/domain/{domainId}` | 도메인 레벨 규칙 **전체 교체**(기존 규칙 삭제 후 요청 본문의 목록으로 재생성) |
| POST | `/workflow-configs/node/{nodeId}` | 노드 레벨 규칙 **전체 교체**(기존 규칙 삭제 후 요청 본문의 목록으로 재생성) |
| POST | `/workflow-configs` | 단건 저장(신규 생성 또는 `id` 지정 시 부분 수정) |
| DELETE | `/workflow-configs/{id}` | 규칙 단건 삭제 |

> `steps_config`의 `steps`/`approvalLine` 배열에 `stepOrder`가 있을 경우, 저장 시 1부터 시작하는 연속된 값인지 검증하며 위반 시 `400 INVALID_WORKFLOW_CONFIG`를 반환한다(3_business_logic.md 7.4절 참고).

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
