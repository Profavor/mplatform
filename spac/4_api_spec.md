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

### 인증 및 권한 (Auth & Permissions)
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/auth/login` | 로그인 |
| POST | `/auth/refresh` | 토큰 갱신 |
| POST | `/auth/change-password` | 비밀번호 변경 |
| GET / PUT | `/admin/permissions` | 권한 매트릭스 조회 및 수정 |

### 사용자 및 조직 (Users & Organizations)
| Method | Endpoint | 설명 |
|---|---|---|
| GET / POST / PUT | `/admin/users` | 사용자 관리 및 역할 할당 |
| GET / POST / PUT / DELETE | `/admin/organizations` | 조직 관리 |
| GET / POST / PUT / DELETE | `/admin/departments` | 부서 관리 |
| GET / POST / PUT / DELETE | `/admin/teams` | 팀 관리 |

### 시스템 및 공통 관리
| Method | Endpoint | 설명 |
|---|---|---|
| GET / POST / PUT / DELETE | `/admin/menus/tree` | 트리 메뉴 관리 |
| GET / POST / PUT / DELETE | `/admin/codes/groups` | 공통 코드 그룹 관리 |
| GET / POST / PUT / DELETE | `/admin/codes/details` | 공통 코드 상세 관리 |
| GET | `/dashboard/summary` | 대시보드 요약 데이터 조회 |
| POST | `/system/install` | 시스템 초기 설치 |
| GET | `/system/install-status` | 초기 설치 상태 확인 |

### 실시간 협업 및 알림
| Method | Endpoint | 설명 |
|---|---|---|
| GET / PUT | `/notifications` | 알림 목록 및 읽음 처리 |
| GET | `/notifications/stream` | SSE 기반 실시간 알림 스트림 |
| WS | `/ws-stomp` | STOMP 웹소켓 엔드포인트 |
| GET / POST | `/chat/messages` | 채팅 메시지 조회 및 전송 |
| GET / POST | `/chat/rooms` | 채팅방 조회 및 생성 |
| POST / GET | `/music/broadcast` | 유튜브 음악 방송 제어 |

### 파일 및 검색 공통
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/files/upload` | 파일 업로드 |
| GET | `/files/download/{id}` | 파일 다운로드 |
| GET | `/search` | 글로벌 통합 검색 |
| POST | `/translate` | 다국어 번역 요청 |

### 도메인 특화 부가기능
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/domains/{domainId}/data-profiling` | 데이터 프로파일링 실행 |
| GET | `/domains/{domainId}/dq-recommendations` | DQ 룰 추천 목록 조회 |
| GET | `/nodes/{nodeId}/schema-impact` | 스키마 변경 영향도 분석 |
| GET | `/records/{id}/lineage` | 레코드 필드 수준 계보(Lineage) 조회 |
| GET | `/records/{id}/history` | 레코드 변경 이력 조회 |
| GET / POST | `/domains/{domainId}/taxonomy-versions` | 분류체계 버전 스냅샷 관리 |
| GET / POST / DELETE | `/domains/{domainId}/relations` | 도메인 간 마스터 관계 관리 |
| GET / POST / PUT / DELETE | `/domains/{domainId}/workflows` | 워크플로우 결재선 설정 |

### 배치 및 모니터링
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/records/export/async` | 대용량 레코드 비동기 내보내기 |
| GET | `/batch-jobs/{jobId}/status` | 배치 작업 상태 폴링 |
| POST | `/records/{id}/unmask` | 민감 데이터 마스킹 해제 요청 |
| GET | `/admin/sensitive-data/access-logs` | 민감 데이터 접근 감사 로그 조회 |
| GET | `/health` | 헬스 체크 |
| GET | `/actuator/prometheus` | Prometheus 메트릭 수집 엔드포인트 |
| GET | `/admin/error-logs` | 시스템 에러 로그 조회 |
