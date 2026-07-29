# 3. 데이터 모델 (PostgreSQL 기준)

### 3.1 domain
도메인 자체의 정보뿐만 아니라, **이 도메인에 속하는 레코드(Record)를 대표하는 식별 필드**를 매핑한다.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 도메인 ID |
| name | JSONB | 도메인명 다국어 맵 (예: `{"ko":"임직원", "en":"Employee"}`) |
| description | JSONB | 도메인 설명 다국어 맵 |
| identifier_field_id | UUID/FK, nullable | 레코드 식별자 필드 |
| display_name_field_id | UUID/FK, nullable | 레코드 표시명 필드 |
| description_field_id | UUID/FK, nullable | 레코드 설명 필드 |
| created_at / updated_at | datetime | |

### 3.1.1 classification_axis *(다축 분류체계 지원)*
도메인 내의 분류축(예: 부서 축, 고용형태 축, 카테고리 축)을 정의하는 테이블.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 분류축 ID |
| domain_id | UUID/FK | 소속 도메인 ID |
| axis_code | string(50) | 분류축 식별 코드 (예: `DEFAULT`, `DEPT`, `EMPLOYMENT_TYPE`) |
| name | JSONB | 분류축 명칭 다국어 맵 |
| description | string(1000) | 분류축 상세 설명 |
| is_default | boolean, default false | 도메인의 기본 분류축 여부 (도메인당 1개) |
| sort_order | int, default 0 | 정렬 순서 |
| created_at / updated_at | datetime | |

### 3.2 classification_node
분류 트리를 구성하는 노드. 특정 `axis_id`에 속할 수 있으며, `axis_id`가 null인 경우 기본 분류축으로 간주한다.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 노드 ID |
| domain_id | UUID/FK | 소속 도메인 |
| axis_id | UUID/FK, nullable | 소속 분류축 ID (`classification_axis`) |
| parent_id | UUID/FK, nullable | 부모 노드 (null이면 분류축의 루트) |
| name | JSONB | 노드명 다국어 맵 |
| path | string | 조상 경로 캐시 (예: `ROOT > 영업본부 > 영업1팀`) |
| depth | int | 루트로부터의 깊이 |
| node_order | int | 형제 노드 간 정렬 순서 |
| is_deleted | boolean, default false | 논리적 삭제 여부 |
| deleted_at | datetime, nullable | 삭제 일시 |
| created_at / updated_at | datetime | |

### 3.2.1 record_secondary_node *(레코드 다축 서브 매핑)*
레코드가 주 분류 노드 외 타 분류축 노드에 속할 수 있도록 지원하는 다대다 매핑 테이블.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 매핑 ID |
| record_id | UUID, index | 레코드 ID |
| node_id | UUID/FK, index | 매핑 대상 서브 분류 노드 ID |
| axis_id | UUID, index | 서브 노드가 속한 분류축 ID |
| created_at | datetime | 매핑 일시 |

### 3.3 field_definition
필드는 특정 `classification_node`에 정의되며, 그 노드와 모든 하위 노드에 자동 상속된다.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 필드 ID |
| defined_at_node_id | UUID/FK | 이 필드를 최초로 정의한 노드 |
| name | JSONB | 필드명 다국어 맵 |
| key | string | 시스템용 식별자 (snake_case) |
| type | enum | 필드 타입 (`TEXT`, `NUMBER`, `DATE`, `TIME`, `I18N`, `RICH_TEXT`, `SELECT`, `MULTI_SELECT`, `TABLE`, `FILE`, `REFERENCE`) |
| options | JSON, nullable | 타입별 세부 설정 |
| required | boolean | 필수 여부 |
| default_value | JSON, nullable | 기본값 |
| order | int | 표시 순서 |
| is_removed | boolean, default false | Soft delete형 오버라이드 |
| is_multi_value | boolean, default false | 다중값 배열 허용 여부 |
| is_table | boolean, default false | TABLE형 데이터 여부 |
| is_encrypted | boolean, default false | 암호화 저장 여부 |
| is_searchable | boolean, default false | 검색 대상 지정 여부 |
| is_highlighted | boolean, default false | 강조 표시 여부 |
| created_at / updated_at | datetime | |

### 3.4 record — 레코드 본체
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 레코드 ID |
| node_id | UUID/FK | 주 분류 노드 ID |
| status | enum | 상태 (`DRAFT`, `PENDING_APPROVAL`, `ACTIVE`, `INACTIVE`, `MISMATCHED`, `REJECTED`, `MERGED`) |
| data | JSONB / String | 전체 필드 데이터 JSON 직렬화 |
| version | int | 레코드 버전 |
| source_system | string | 출처 소스 시스템명 |
| merged_into_record_id | UUID, nullable | 병합 대상(survivor) 레코드 ID |
| approval_request_id | UUID, nullable | 관련 결재 요청 ID |
| created_at / updated_at | datetime | |

### 3.5 dq_score_snapshot *(DQ 시계열 트렌드)*
크론/수동 DQ 스캔 실행 시 도메인의 품질 점수를 이력 저장하는 테이블.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 스냅샷 ID |
| domain_id | UUID, index | 소속 도메인 ID |
| score | double | 품질 점수 (0.0 ~ 100.0) |
| total_records | bigint | 전체 레코드 수 |
| total_violations | bigint | 전체 위반 건수 |
| scan_type | string(20) | 스캔 유형 (`SCHEDULED`, `MANUAL`) |
| recorded_at | datetime, index | 스냅샷 기록 일시 |

### 3.6 integration_channels & integration_logs *(연계 & DLQ)*
#### integration_channels
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 채널 ID |
| name / type / direction | string | 채널명, 방식(`WEB_SERVICE`, `JDBC` 등), 방향(`INBOUND`, `OUTBOUND`) |
| config_json / mapping_config_json | text | 채널 및 SpEL 매핑 규칙 |
| max_retries | int, default 3 | 최대 재시도 횟수 |
| retry_backoff_ms | bigint, default 1000 | 기본 백오프 간격 (ms) |
| use_exponential_backoff | boolean, default true | 지수 백오프 적용 여부 |
| is_active / requires_approval | boolean | 활성화 및 승인 필요 여부 |

#### integration_logs
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 로그 ID |
| channel_id / record_id | UUID | 소속 채널 ID 및 대상 레코드 ID |
| status | string(20) | 상태 (`SUCCESS`, `FAIL`, `DEAD_LETTER`) |
| original_payload / mapped_payload | text | 원본 및 매핑 페이로드 |
| error_message / stack_trace | text | 오류 메시지 및 스택트레이스 |
| retry_count | int, default 0 | 현재까지 재시도 횟수 |
| next_retry_at | datetime, nullable | 지수 백오프 적용 다음 재시도 예정 시각 |
| created_at | datetime | 생성 일시 |
