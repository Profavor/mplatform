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
| hint | JSONB | 필드 툴팁/도움말 다국어 맵 |
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
| masking_pattern | string(50), nullable | 개인정보 마스킹 정규식 패턴 (예: `RRN`, `PHONE`, `CARD`) |
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

### 3.7 sensitive_data_access_log *(개인정보 열람 감사 로그)*
마스킹된 개인정보 필드의 원본 데이터를 조회/열람할 때마다 기록되는 시스템 감사(Audit) 로그.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 로그 ID |
| user_id | string(100) | 열람자 ID |
| username | string(100) | 열람자 이름 |
| target_type | string(50) | 열람 대상 유형 (예: `RECORD`) |
| target_id | UUID | 열람 대상 레코드 ID |
| field_keys | string(500) | 열람한 민감 데이터 필드 키 목록 |
| access_reason | string(500) | 필수 열람 사유 |
| ip_address | string(50) | 열람자 IP 주소 |
| accessed_at | datetime | 열람 일시 |

### 3.8 permission_master, menu_node, common_code *(멱등성 기반 기초 시드 데이터)*
시스템 구동 및 운영에 필요한 핵심 권한 매트릭스, 전체 트리 메뉴 노드, 공통 코드 속성 테이블.
이 테이블들은 프로덕션(`prod`) 배포 및 DB `ddl-auto: validate` 환경에서 불필요한 DDL 변경이나 데이터 훼손 없이 안전하게 운영되도록 **멱등성(Idempotency) Guard(`count() > 0`)** 룰을 적용하여 초기화된다. 

> **💡 암호화 필드(`is_encrypted=true`)의 데이터 저장은 어떻게 되나요?**
> `field_definition`에서 `is_encrypted=true`로 선언된 항목은 `record.data` JSONB 내에 평문이 아닌 **32바이트 AES 대칭키로 암호화된 Base64 문자열**로 반영된다. 일치 검색이 필요한 경우 원본 복원 키와 분리된 HMAC SHA-256 전용 키를 통해 생성되는 **Blind Index 해시값**을 대조함으로써, 관리자 DB 조회 시에도 원본 평문이 노출되지 않는 제로 트러스트(Zero Trust) 구조를 달성한다.

### 3.9 사용자/조직 관리 (User & Organization)
사용자 및 조직도 구성을 위한 테이블.

#### user
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | string/PK | 사용자 ID |
| username | string | 사용자 로그인 아이디 |
| name | string | 사용자 이름 |
| email | string | 이메일 |
| department_id | UUID/FK | 주 소속 부서 |
| created_at / updated_at | datetime | |

#### role
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 역할 ID |
| code | string | 역할 코드 (예: ADMIN, USER) |
| name | JSONB | 역할명 다국어 |

#### user_role
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 매핑 ID |
| user_id | string/FK | 사용자 ID |
| role_id | UUID/FK | 역할 ID |

#### organization, department, team
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 식별자 ID |
| parent_id | UUID/FK, nullable | 상위 조직 ID |
| name | JSONB | 조직명 다국어 |

#### user_org_history
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 이력 ID |
| user_id | string/FK | 사용자 ID |
| department_id | UUID/FK | 발령 부서 ID |
| effective_date | date | 발령 일자 |

### 3.10 메뉴 시스템 (Menu)
#### menu
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 메뉴 ID |
| parent_id | UUID/FK | 부모 메뉴 ID |
| name | JSONB | 메뉴명 다국어 |
| path | string | 라우팅 경로 |

#### menu_access_log
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 로그 ID |
| user_id | string/FK | 사용자 ID |
| menu_id | UUID/FK | 접근 메뉴 ID |
| accessed_at | datetime | 접근 일시 |

### 3.11 권한 관리 (Permission)
#### permission_group, permission_item
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 권한 식별자 |
| code | string | 권한 코드 |
| description | string | 권한 설명 |

### 3.12 STOMP 채팅 (Chat)
#### chat_message, chat_message_room, chat_message_room_member
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 식별자 |
| room_id | UUID/FK | 채팅방 ID |
| sender_id | string/FK | 송신자 ID |
| content | text | 메시지 내용 |

### 3.13 알림 (Notification)
#### notification
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 알림 ID |
| user_id | string/FK | 수신자 ID |
| message | string | 알림 내용 |
| is_read | boolean | 읽음 여부 |
| created_at | datetime | 생성 일시 |

### 3.14 배치 처리 (BatchJob & Staging)
#### batch_job
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 배치 ID |
| job_type | string | 작업 유형 (IMPORT, EXPORT) |
| status | string | 상태 (RUNNING, COMPLETED, FAILED) |

#### staging_record
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 임시 레코드 ID |
| batch_job_id | UUID/FK | 배치 작업 ID |
| raw_data | text | 원본 데이터 |

### 3.15 검색 인덱스 (RecordDocument)
#### record_document
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | string/PK | 오픈서치 문서 ID (Record ID와 동일) |
| domain_id | UUID/FK | 도메인 ID |
| indexed_data | JSON | 검색용 인덱싱 데이터 |

### 3.16 도메인 기능 확장
#### master_relation (마스터 데이터 관계)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 관계 ID |
| source_domain_id | UUID/FK | 소스 도메인 |
| target_domain_id | UUID/FK | 타겟 도메인 |

#### taxonomy_version (분류체계 버전)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 버전 ID |
| domain_id | UUID/FK | 도메인 ID |
| version_tag | string | 버전 태그 |
| snapshot_data | JSONB | 체계 스냅샷 |

#### domain_access_request, domain_permission (도메인 권한)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 식별자 |
| domain_id | UUID/FK | 도메인 |
| user_id | string/FK | 사용자 |
| access_level | string | 접근 수준 (READ, WRITE) |

#### dq_scan_schedule (DQ 스케줄)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 스케줄 ID |
| domain_id | UUID/FK | 도메인 ID |
| cron_expression | string | 실행 주기 |

#### workflow_config (워크플로우 설정)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 워크플로우 ID |
| domain_id | UUID/FK | 도메인 ID |
| config_json | JSONB | 결재선 규칙 |

### 3.17 공통 및 시스템 (System & Logs)
#### login_log, error_log
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 로그 ID |
| user_id | string | 사용자 |
| error_message | text | 오류 메시지 |
| created_at | datetime | 발생 일시 |

#### system_config
| 컬럼 | 타입 | 설명 |
|---|---|---|
| config_key | string/PK | 설정 키 |
| config_value | text | 설정 값 |

#### user_youtube_config
| 컬럼 | 타입 | 설명 |
|---|---|---|
| user_id | string/PK | 사용자 ID |
| api_key | string | 유튜브 연동 키 |

#### code_group, code_detail
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 코드 식별자 |
| group_code | string | 그룹 코드 |
| detail_code | string | 상세 코드 |
| name | JSONB | 코드명 다국어 |
