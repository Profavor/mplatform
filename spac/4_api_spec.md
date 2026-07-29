# 6. API 스펙 (실제 구현 기준, Base URL: `/api`)

### 도메인 / 분류축 / 스키마
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/domains` | 도메인 생성 |
| GET | `/domains` | 도메인 목록 조회 |
| GET | `/domains/{id}` | 도메인 상세 조회 |
| PUT | `/domains/{id}` | 도메인 수정 |
| GET | `/domains/{domainId}/axes` | 도메인 분류축 목록 조회 *(신규)* |
| POST | `/domains/{domainId}/axes` | 도메인 분류축 생성 *(신규)* |
| GET | `/domains/{domainId}/axes/{axisId}` | 분류축 상세 조회 *(신규)* |
| PUT | `/domains/{domainId}/axes/{axisId}` | 분류축 수정 *(신규)* |
| DELETE | `/domains/{domainId}/axes/{axisId}` | 분류축 삭제 *(신규)* |
| GET / POST / PUT | `/domains/{domainId}/fields`, `/fields/page`, `/fields/{fieldId}` | 도메인 루트 필드 관리 |
| GET / POST / PUT / DELETE | `/domains/{domainId}/sectors`, `/sectors/{sectorId}` | 화면 탭(Sector) 관리 |
| GET / POST / PUT / DELETE | `/domains/{domainId}/groups`, `/groups/{groupId}` | 필드 그룹(FieldGroup) 관리 |
| POST | `/domains/{domainId}/nodes` | 노드 생성 |
| GET | `/domains/{domainId}/nodes/tree` | 도메인 하위 트리 조회 |
| PUT | `/domains/{domainId}/nodes/{nodeId}` | 노드 메타데이터 수정 |
| POST / GET / PUT | `/nodes/{nodeId}/fields`, `/fields/effective`, `/fields/effective/page` | 노드 필드 정의 CRUD 및 EffectiveFields 조회 |

### 데이터 품질(DQ)
| Method | Endpoint | 설명 |
|---|---|---|
| GET / POST / PUT / DELETE | `/fields/{fieldId}/dq-rules`, `/dq-rules/{ruleId}` | DQ 룰 CRUD |
| POST | `/dq-rules/validate` | DQ 룰 실시간 테스트 |
| GET | `/domains/{domainId}/dq-score` | 도메인 현재 DQ 점수 조회 |
| GET | `/domains/{domainId}/dq-score/trend` | 도메인 DQ 점수 시계열 트렌드 조회 *(신규)* |
| GET | `/domains/{domainId}/dq-score/recent` | 최근 N건 DQ 스냅샷 조회 *(신규)* |
| POST | `/domains/{domainId}/dq-scan` | 전체 재검사 수동 트리거 및 스냅샷 저장 |
| GET | `/domains/{domainId}/dq-violations` | DQ 위반 목록 조회 (페이징) |

### 레코드 / 사전검증 / 결재
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/nodes/{nodeId}/records` | 레코드 생성 요청(기안) — DQ 검증 + 중복 검사 동기 수행 |
| POST | `/nodes/{nodeId}/records/batch` | 배치 생성(Excel 대량 업로드 등) |
| POST | `/nodes/{nodeId}/records/batch-validate` | Excel 대량 업로드 행 단위 사전 DQ 검증 리포트 *(신규)* |
| GET | `/nodes/{nodeId}/records` | 노드 기준 레코드 목록 조회 |
| GET | `/records/{id}` | 레코드 단건 조회 |
| GET | `/records/domain/{domainId}` | 도메인 기준 레코드 목록 조회(검색·페이징) |
| POST | `/records/{id}/unmerge` | 병합된 레코드 Un-merge (원복) *(신규)* |
| GET | `/records/{id}/secondary-nodes` | 레코드 서브 분류축 노드 매핑 목록 조회 *(신규)* |
| POST | `/records/{id}/secondary-nodes` | 레코드 서브 분류축 노드 매핑 변경 *(신규)* |
| POST | `/records/{id}/update-request` | 레코드 수정 요청(기안) |
| POST | `/records/{id}/delete-request` | 레코드 삭제 요청(기안) |
| GET | `/approval-requests/effective-workflow/{nodeId}` | 유효 결재선 존재 여부 조회 |
| GET | `/approval-requests/available-workflows/{nodeId}` | 적용 가능 서식 목록 조회 |
| GET | `/approval-requests/effective-permission/{nodeId}` | 필드 단위 편집/읽기/숨김 권한 반환 |
| GET | `/approval-requests`, `/todos`, `/my-requests` | 결재 요청/내 결재함/내 기안함 조회 |
| POST | `/approval-requests/steps/{stepId}/approve` | 단계 승인 |
| POST | `/approval-requests/steps/{stepId}/reject` | 단계 반려 |

### 매칭 / Golden Record / 피드백
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/domains/{domainId}/match-candidates` | 매칭 후보 목록 조회 |
| POST | `/match-candidates/{id}/confirm` | 매칭 후보 중복 확정 |
| POST | `/match-candidates/{id}/reject` | 매칭 후보 오탐 반려 |
| GET | `/domains/{domainId}/matching-rules/feedback-summary` | 매칭 피드백 통계 및 권장 임계값 조회 *(신규)* |
| GET | `/domains/{domainId}/matching-rules/{ruleId}/feedback` | 특정 매칭 룰 피드백 상세 조회 *(신규)* |
| POST | `/records/merge` | 수동 레코드 병합 |
| POST | `/records/merge/auto` | Survivorship 규칙 기반 자동 병합 |
| GET / PUT | `/records/domains/{domainId}/survivorship-rules` | 서바이버십 규칙 조회/수정 |

### 연계(Integration) & Dead-Letter Queue (DLQ)
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/integration/inbound/{channelId}` | 외부 시스템 인바운드 수신(채널 시크릿 인증) |
| GET / POST / PUT / DELETE | `/admin/integration/channels`, `/channels/{id}` | 연계 채널 CRUD |
| POST | `/admin/integration/channels/{id}/test` | 연계 테스트 |
| GET | `/admin/integration/logs/dead-letter` | Dead-Letter Queue 연계 실패 로그 조회 *(신규)* |
| POST | `/admin/integration/logs/dead-letter/retry-all` | DLQ전건 일괄 수동 재시도 *(신규)* |
| POST | `/admin/integration/logs/{logId}/retry` | 연계 실패 1건 수동 재시도 *(신규)* |
