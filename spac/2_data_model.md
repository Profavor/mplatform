# 2. 데이터 모델 (PostgreSQL 스키마 명세)

본 문서는 PostgreSQL 15 및 Spring Data JPA 기반으로 구축된 MDM 플랫폼의 전체 67개 엔티티 및 7개 enum 및 테이블 스키마에 대한 상세 명세서이다.

---

## 2.1 도메인 및 다축 동적 스키마 (Domain & Dynamic Schema)

### 2.1.1 `domain` (도메인 마스터)
마스터 데이터의 최상위 도메인 정의 테이블.
| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
|---|---|---|---|
| `id` | UUID | PK | 도메인 고유 식별자 |
| `name` | JSONB | NOT NULL | 도메인명 다국어 맵 (예: `{"ko":"임직원", "en":"Employee"}`) |
| `description` | JSONB | NULLABLE | 도메인 설명 다국어 맵 |
| `identifier_field_id` | UUID | FK (`field_definition`), NULL | 레코드 고유 식별자 필드 |
| `display_name_field_id` | UUID | FK (`field_definition`), NULL | 레코드 대표 표시명 필드 |
| `description_field_id` | UUID | FK (`field_definition`), NULL | 레코드 설명 필드 |
| `created_at` / `updated_at` | TIMESTAMP | NOT NULL | 생성 및 최종 수정 일시 |

### 2.1.2 `classification_axis` (다축 분류 기준)
도메인 하위의 독립적인 분류축(예: 조직도 축, 고용형태 축, 직군 축) 정의 테이블.
| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
|---|---|---|---|
| `id` | UUID | PK | 분류축 고유 식별자 |
| `domain_id` | UUID | FK (`domain`), INDEX | 소속 도메인 ID |
| `axis_code` | VARCHAR(50) | NOT NULL | 분류축 식별 코드 (예: `DEFAULT`, `ORG`, `EMPLOYMENT_TYPE`) |
| `name` | JSONB | NOT NULL | 분류축 명칭 다국어 맵 |
| `description` | VARCHAR(1000) | NULLABLE | 분류축 상세 설명 |
| `is_default` | BOOLEAN | NOT NULL, DEFAULT FALSE | 도메인의 기본 분류축 여부 (도메인당 1개만 true) |
| `sort_order` | INT | NOT NULL, DEFAULT 0 | 분류축 노출 정렬 순서 |
| `created_at` / `updated_at` | TIMESTAMP | NOT NULL | 생성 및 최종 수정 일시 |

### 2.1.3 `classification_node` (분류 트리 노드)
분류축 내의 계층적 트리 노드. 상위 노드의 필드를 하위 노드가 자동 상속한다.
| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
|---|---|---|---|
| `id` | UUID | PK | 노드 고유 식별자 |
| `domain_id` | UUID | FK (`domain`), INDEX | 소속 도메인 ID |
| `axis_id` | UUID | FK (`classification_axis`), NULL, INDEX | 소속 분류축 ID (null이면 기본 축 간주) |
| `parent_id` | UUID | FK (`classification_node`), NULL | 상위 부모 노드 ID (null이면 축의 루트 노드) |
| `name` | JSONB | NOT NULL | 노드명 다국어 맵 |
| `path` | VARCHAR(1000) | NOT NULL | 조상 노드 경로 캐시 (예: `ROOT > 개발본부 > 백엔드팀`) |
| `depth` | INT | NOT NULL, DEFAULT 0 | 루트로부터의 트리 깊이 |
| `node_order` | INT | NOT NULL, DEFAULT 0 | 형제 노드 간 정렬 순서 |
| `is_deleted` | BOOLEAN | NOT NULL, DEFAULT FALSE | 논리적 삭제(Soft Delete) 플래그 |
| `deleted_at` | TIMESTAMP | NULLABLE | 논리적 삭제 일시 |
| `created_at` / `updated_at` | TIMESTAMP | NOT NULL | 생성 및 최종 수정 일시 |

### 2.1.4 `record_secondary_node` (레코드 다축 서브 매핑)
레코드가 주 소속 노드 외에 타 분류축의 노드에 동시 등록될 수 있도록 지원하는 다대다 매핑 테이블.
| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
|---|---|---|---|
| `id` | UUID | PK | 매핑 고유 식별자 |
| `record_id` | UUID | NOT NULL, INDEX | 레코드 ID |
| `node_id` | UUID | FK (`classification_node`), INDEX | 매핑 대상 서브 분류 노드 ID |
| `axis_id` | UUID | NOT NULL, INDEX | 서브 노드가 속한 분류축 ID |
| `created_at` | TIMESTAMP | NOT NULL | 매핑 생성 일시 |

### 2.1.5 `sector` & `field_group` (화면 레이아웃 구획)
- `sector`: 입력 폼의 대분류 탭(Tab) 구획. (`id`, `domain_id`, `name`, `sort_order`)
- `field_group`: Sector 하위의 시각적 필드 그룹 박스. (`id`, `sector_id`, `name`, `sort_order`)

### 2.1.6 `field_definition` (동적 필드 정의)
특정 `classification_node`에 정의되어 하위 노드로 자동 상속되는 동적 속성 메타데이터.
| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
|---|---|---|---|
| `id` | UUID | PK | 필드 고유 식별자 |
| `defined_at_node_id` | UUID | FK (`classification_node`), INDEX | 필드가 최초 선언된 노드 ID |
| `name` | JSONB | NOT NULL | 필드명 다국어 맵 |
| `hint` | JSONB | NULLABLE | 필드 툴팁/도움말 다국어 맵 |
| `key` | VARCHAR(100) | NOT NULL | 시스템용 고유 필드 키 (snake_case) |
| `type` | VARCHAR(30) | NOT NULL | 데이터 타입 (`TEXT`, `NUMBER`, `DATE`, `TIME`, `I18N`, `RICH_TEXT`, `SELECT`, `MULTI_SELECT`, `TABLE`, `FILE`, `REFERENCE`) |
| `options` | JSONB | NULLABLE | 선택지, 참조 대상 도메인, 수식(SpEL), 테이블 컬럼 등 세부 설정 |
| `required` | BOOLEAN | NOT NULL, DEFAULT FALSE | 필수 입력 여부 |
| `default_value` | JSONB | NULLABLE | 필드 기본값 |
| `order` | INT | NOT NULL, DEFAULT 0 | 그룹 내 표시 순서 |
| `group_id` | UUID | FK (`field_group`), NULL | 소속 필드 그룹 ID |
| `is_removed` | BOOLEAN | NOT NULL, DEFAULT FALSE | 상속 필드 제외(Soft-override) 여부 |
| `is_multi_value` | BOOLEAN | NOT NULL, DEFAULT FALSE | 배열 다중값 허용 여부 |
| `is_table` | BOOLEAN | NOT NULL, DEFAULT FALSE | 복합 테이블 데이터 구조 여부 |
| `is_encrypted` | BOOLEAN | NOT NULL, DEFAULT FALSE | 32바이트 AES / Vault 암호화 저장 여부 |
| `is_searchable` | BOOLEAN | NOT NULL, DEFAULT FALSE | OpenSearch 검색 인덱싱 대상 여부 |
| `is_highlighted` | BOOLEAN | NOT NULL, DEFAULT FALSE | 메인 그리드 강조 컬럼 표출 여부 |
| `masking_pattern` | VARCHAR(50) | NULLABLE | 개인정보 마스킹 패턴 (`RRN`, `PHONE`, `CARD`, `EMAIL`, `CUSTOM`) |
| `created_at` / `updated_at` | TIMESTAMP | NOT NULL | 생성 및 최종 수정 일시 |

---

## 2.2 레코드 본체 및 이력 (Record & History)

### 2.2.1 `record` (마스터 데이터 본체)
| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
|---|---|---|---|
| `id` | UUID | PK | 레코드 고유 식별자 |
| `node_id` | UUID | FK (`classification_node`), INDEX | 주 소속 분류 노드 ID |
| `status` | VARCHAR(30) | NOT NULL, INDEX | 상태 (`DRAFT`, `PENDING_APPROVAL`, `ACTIVE`, `INACTIVE`, `MISMATCHED`, `REJECTED`, `MERGED`) |
| `data` | JSONB | NOT NULL | 전체 필드 데이터 JSON 맵 (암호화 필드는 암호문 Base64 저장) |
| `version` | INT | NOT NULL, DEFAULT 1 | 레코드 버전 |
| `source_system` | VARCHAR(100) | NOT NULL, DEFAULT 'MANUAL' | 데이터 출처 소스 시스템명 |
| `merged_into_record_id` | UUID | NULLABLE, INDEX | 병합된 경우 대상(Survivor) 골든 레코드 ID |
| `approval_request_id` | UUID | NULLABLE, INDEX | 관련 결재 요청 ID |
| `created_at` / `updated_at` | TIMESTAMP | NOT NULL, INDEX | 생성 및 최종 수정 일시 |

### 2.2.2 `record_history` (버전별 스냅샷 이력)
레코드의 변경(생성, 수정, 삭제, 병합, Un-merge) 시점의 전체 스냅샷 이력.
| 컬럼명 | 데이터 타입 | 설명 |
|---|---|---|
| `id` | UUID/PK | 이력 고유 식별자 |
| `record_id` | UUID/INDEX | 대상 레코드 ID |
| `version` | INT | 버전 번호 |
| `action` | VARCHAR(50) | 변경 액션 (`CREATE`, `UPDATE`, `DELETE`, `MERGE`, `UNMERGE`, `ROLLBACK`) |
| `data_snapshot` | JSONB | 당시의 `record.data` 전체 스냅샷 |
| `changed_by` | VARCHAR(100) | 변경자 사용자 ID |
| `reason` | VARCHAR(500) | 변경 사유 |
| `created_at` | TIMESTAMP | 이력 기록 일시 |

### 2.2.3 `record_field_source` (필드 수준 출처 계보 - Lineage)
각 필드값이 어느 소스 시스템에서 언제 반영되었는지 추적하는 계보 테이블.
| 컬럼명 | 데이터 타입 | 설명 |
|---|---|---|
| `id` | UUID/PK | 계보 식별자 |
| `record_id` | UUID/INDEX | 레코드 ID |
| `field_key` | VARCHAR(100) | 필드 키 식별자 |
| `source_system` | VARCHAR(100) | 데이터 제공 소스 시스템 |
| `updated_at` | TIMESTAMP | 소스 시스템 반영 일시 |

---

## 2.3 결재, 위임, 동적 라우팅 및 샌드박스 (Approval Governance)

### 2.3.1 `approval_request` (결재 기안)
| 컬럼명 | 데이터 타입 | 설명 |
|---|---|---|
| `id` | UUID/PK | 결재 요청 고유 식별자 |
| `domain_id` / `node_id` | UUID/FK | 대상 도메인 및 노드 ID |
| `record_id` | UUID/FK, NULL | 대상 레코드 ID (신규 기안 시 임시 DRAFT 레코드) |
| `request_type` | VARCHAR(50) | 기안 유형 (`RECORD_CREATE`, `RECORD_UPDATE`, `RECORD_DELETE`, `SCHEMA_CHANGE`) |
| `status` | VARCHAR(30) | 결재 상태 (`PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`) |
| `requester_id` | VARCHAR(100) | 기안자 ID |
| `payload` | JSONB | 기안 변경 데이터 JSON 스냅샷 |
| `created_at` / `updated_at` | TIMESTAMP | 기안 및 완료 일시 |

### 2.3.2 `approval_step` (단계별 결재선)
| 컬럼명 | 데이터 타입 | 설명 |
|---|---|---|
| `id` | UUID/PK | 결재 단계 식별자 |
| `request_id` | UUID/FK | 결재 요청 ID |
| `step_order` | INT | 결재 순서 (1, 2, 3...) |
| `approver_type` | VARCHAR(20) | 승인권자 유형 (`USER`, `ROLE`, `DEPARTMENT_HEAD`) |
| `approver_id` | VARCHAR(100) | 지정 승인 대상 식별자 |
| `status` | VARCHAR(30) | 단계 상태 (`WAITING`, `PENDING`, `APPROVED`, `REJECTED`, `ESCALATED`, `DELEGATED`) |
| `action_by` | VARCHAR(100) | 실제 승인/반려 처리자 ID |
| `action_comment` | VARCHAR(1000) | 결재 의견 / 반려 사유 |
| `action_at` | TIMESTAMP | 결재 처리 일시 |

### 2.3.3 `approval_delegation` (결재 위임)
결재자의 부재/출장 시 결재 권한을 타 사용자에게 위임하는 설정 테이블.
| 컬럼명 | 데이터 타입 | 설명 |
|---|---|---|
| `id` | UUID/PK | 위임 식별자 |
| `delegator_id` | VARCHAR(100)/INDEX | 위임자(원 결재자) ID |
| `delegatee_id` | VARCHAR(100)/INDEX | 수임자(대결자) ID |
| `domain_id` | UUID, NULL | 특정 도메인 한정 위임 (null이면 전체) |
| `start_date` / `end_date` | TIMESTAMP | 위임 유효 기간 |
| `reason` | VARCHAR(500) | 위임 사유 |
| `is_active` | BOOLEAN | 활성화 여부 |

### 2.3.4 `approval_routing_template` (조건부 동적 결재선 템플릿)
데이터 금액/중요도 조건에 따라 결재선을 자동 동적 분기하는 템플릿 테이블.
| 컬럼명 | 데이터 타입 | 설명 |
|---|---|---|
| `id` | UUID/PK | 템플릿 식별자 |
| `domain_id` | UUID/FK | 소속 도메인 ID |
| `name` | VARCHAR(100) | 템플릿 명칭 |
| `condition_expression` | VARCHAR(1000) | SpEL 기반 조건식 (예: `#amount > 10000000`) |
| `steps_json` | JSONB | 생성할 결재 단계 배열 정의 |
| `priority` | INT | 조건 평가 우선순위 |

### 2.3.5 `workflow_config` (도메인/노드별 워크플로우 설정)
| 컬럼명 | 데이터 타입 | 설명 |
|---|---|---|
| `id` | UUID/PK | 설정 식별자 |
| `domain_id` / `node_id` | UUID/FK | 적용 대상 도메인 또는 노드 ID |
| `config_json` | JSONB | 기본 결재선 단계 및 필수 승인자 규칙 |

---

## 2.4 데이터 품질, 시계열 트렌드 & 자율 치유 (DQ & Cleansing)

### 2.4.1 `dq_rule` (품질 검증 규칙)
| 컬럼명 | 데이터 타입 | 설명 |
|---|---|---|
| `id` | UUID/PK | 규칙 식별자 |
| `field_id` | UUID/FK | 대상 필드 ID |
| `rule_type` | VARCHAR(50) | 룰 유형 (`NOT_NULL`, `REGEX`, `RANGE`, `LENGTH`, `ENUM`, `DATE_RANGE`, `CROSS_FIELD`, `UNIQUE`, `SPEL_EXPRESSION`) |
| `rule_value` | VARCHAR(2000) | 정규식, 범위 JSON, SpEL 수식 등 검증 파라미터 |
| `error_message` | JSONB | 위반 시 노출할 다국어 오류 메시지 맵 |
| `severity` | VARCHAR(20) | 심각도 (`ERROR` - 차단, `WARNING` - 경고) |
| `is_active` | BOOLEAN | 활성화 여부 |

### 2.4.2 `dq_violation` (품질 위반 기록)
스캔 또는 배치 시 발견된 위반 내역. (`id`, `domain_id`, `record_id`, `rule_id`, `field_key`, `invalid_value`, `status`, `created_at`)

### 2.4.3 `dq_score_snapshot` (품질 점수 시계열 스냅샷)
크론 스캔 또는 수동 스캔 시 도메인의 종합 품질 지수를 시계열 보관.
| 컬럼명 | 데이터 타입 | 설명 |
|---|---|---|
| `id` | UUID/PK | 스냅샷 식별자 |
| `domain_id` | UUID/INDEX | 도메인 ID |
| `score` | DOUBLE PRECISION | 품질 점수 (0.0 ~ 100.0) |
| `total_records` | BIGINT | 전체 검사 레코드 수 |
| `total_violations` | BIGINT | 전체 위반 건수 |
| `scan_type` | VARCHAR(20) | 스캔 유형 (`SCHEDULED`, `MANUAL`) |
| `recorded_at` | TIMESTAMP/INDEX | 스냅샷 기록 일시 |

### 2.4.4 `dq_scan_schedule` (DQ 자동 스캔 스케줄)
도메인별 정기 스캔 크론 표현식 설정. (`id`, `domain_id`, `cron_expression`, `is_active`, `last_run_at`)

---

## 2.5 매칭, 골든 레코드 & 서바이버십 (Matching & Golden Record)

### 2.5.1 `matching_rule` (중복 검사 룰)
| 컬럼명 | 데이터 타입 | 설명 |
|---|---|---|
| `id` | UUID/PK | 매칭 룰 식별자 |
| `domain_id` | UUID/FK | 도메인 ID |
| `rule_name` | VARCHAR(100) | 룰 명칭 |
| `match_type` | VARCHAR(30) | 매칭 방식 (`EXACT`, `FUZZY`) |
| `field_keys` | JSONB | 비교 대상 필드 키 목록 |
| `threshold` | DOUBLE PRECISION | 유사도 임계값 (예: 0.85) |
| `is_active` | BOOLEAN | 활성화 여부 |

### 2.5.2 `match_candidate` (중복 의심 후보군)
| 컬럼명 | 데이터 타입 | 설명 |
|---|---|---|
| `id` | UUID/PK | 매칭 후보 식별자 |
| `rule_id` | UUID/FK | 트리거된 매칭 룰 ID |
| `primary_record_id` | UUID/INDEX | 기준 레코드 ID |
| `candidate_record_id` | UUID/INDEX | 비교 대상 레코드 ID |
| `similarity_score` | DOUBLE PRECISION | 계산된 유사도 점수 (0.0 ~ 1.0) |
| `status` | VARCHAR(30) | 검토 상태 (`PENDING`, `CONFIRMED_MERGE`, `REJECTED`) |
| `reviewed_by` | VARCHAR(100) | 검토 스튜어드 ID |
| `reviewed_at` | TIMESTAMP | 검토 일시 |

### 2.5.3 `survivorship_rule` & `source_priority` (서바이버십 병합 규칙)
- `survivorship_rule`: 도메인별 필드 단위 생존 규칙 (`LATEST`, `SOURCE_PRIORITY`, `MOST_FREQUENT`, `MANUAL`).
- `source_priority`: 소스 시스템별 신뢰도 우선순위 점수 (1위: SAP ERP, 2위: Salesforce CRM, 3위: Manual 등).

---

## 2.6 외부 연계, DLQ & 스트리밍 (Integration, DLQ & Streaming)

### 2.6.1 `integration_channel` (연계 채널 정의)
| 컬럼명 | 데이터 타입 | 설명 |
|---|---|---|
| `id` | UUID/PK | 채널 고유 식별자 |
| `name` | VARCHAR(100) | 채널명 |
| `type` | VARCHAR(30) | 프로토콜 (`REST_WEBHOOK`, `JDBC`, `KAFKA`, `RABBITMQ`) |
| `direction` | VARCHAR(20) | 방향 (`INBOUND`, `OUTBOUND`) |
| `config_json` | TEXT | 접속 URL, 토큰, SQL 쿼리 등 연결 설정 (암호화 필드 포함) |
| `mapping_config_json` | TEXT | SpEL 기반 페이로드 변환 매핑 규칙 |
| `max_retries` | INT | 최대 재시도 횟수 (기본 3) |
| `retry_backoff_ms` | BIGINT | 기본 백오프 간격 (ms, 기본 1000) |
| `use_exponential_backoff` | BOOLEAN | 지수 백오프 적용 여부 (기본 true) |
| `is_active` | BOOLEAN | 채널 활성화 여부 |

### 2.6.2 `integration_log` (연계 실행 및 DLQ 로그)
| 컬럼명 | 데이터 타입 | 설명 |
|---|---|---|
| `id` | UUID/PK | 로그 고유 식별자 |
| `channel_id` | UUID/FK | 연계 채널 ID |
| `record_id` | UUID/NULL | 대상 레코드 ID |
| `status` | VARCHAR(20) | 상태 (`SUCCESS`, `FAIL`, `DEAD_LETTER`) |
| `original_payload` | TEXT | 원본 수/발신 페이로드 |
| `mapped_payload` | TEXT | 변환 매핑된 페이로드 |
| `error_message` | TEXT | 오류 메시지 및 예외 내용 |
| `retry_count` | INT | 현재까지 재시도 횟수 |
| `next_retry_at` | TIMESTAMP/NULL | 지수 백오프 다음 재시도 예정 시각 |
| `created_at` | TIMESTAMP | 발생 일시 |

### 2.6.3 `webhook_subscription` (웹훅 이벤트 구독)
외부 시스템의 이벤트 구독 등록 테이블. (`id`, `event_type`, `target_url`, `secret_token`, `is_active`)

### 2.6.4 `bulk_import_job` & `batch_job` & `staging_record` (대량 배치 및 스테이징)
대용량 데이터 임포트/익스포트 처리 및 임시 검증 스테이징 테이블.

---

## 2.7 보안, 감사, 컴플라이언스 & 라이프사이클 (Security & Governance)

### 2.7.1 `sensitive_data_access_log` (개인정보 원본 열람 감사 로그)
| 컬럼명 | 데이터 타입 | 설명 |
|---|---|---|
| `id` | UUID/PK | 감사 로그 식별자 |
| `user_id` | VARCHAR(100) | 열람자 ID |
| `username` | VARCHAR(100) | 열람자 성명 |
| `target_type` | VARCHAR(50) | 대상 유형 (`RECORD`) |
| `target_id` | UUID | 대상 레코드 ID |
| `field_keys` | VARCHAR(500) | 열람한 민감 데이터 필드 키 목록 |
| `access_reason` | VARCHAR(500) | 필수 입력된 열람 사유 |
| `ip_address` | VARCHAR(50) | 열람자 접속 IP |
| `accessed_at` | TIMESTAMP | 열람 일시 |

### 2.7.2 `business_term` (비즈니스 용어사전)
전사 표준 비즈니스 용어 및 도메인 필드 양방향 매핑 테이블. (`id`, `term_name`, `term_code`, `definition`, `domain_id`, `field_key`, `steward_id`)

### 2.7.3 `schema_history` & `taxonomy_version` & `domain_snapshot` (스키마 버전 및 스냅샷)
- `schema_history`: 스키마 필드/노드 변경 이력 및 Before/After JSON 스냅샷.
- `taxonomy_version`: 도메인 전체 분류체계 스냅샷 버전 관리.
- `domain_snapshot`: 특정 시점 도메인 전체 백업 스냅샷.

---

## 2.8 사용자, 조직, 메뉴, 공통코드 & 협업 (Platform Foundations)

- `user`, `role`, `user_role`: RBAC 사용자 및 역할 매핑.
- `organization`, `department`, `team`, `user_org_history`: 조직 계층 및 인사 발령 이력.
- `permission_group`, `permission_item`, `domain_permission`, `domain_access_request`: 메뉴 및 도메인 데이터 접근 권한 매트릭스.
- `menu`, `menu_access_log`: 동적 메뉴 트리 및 접근 감사.
- `code_group`, `code_detail`: 다국어 공통 코드 체계.
- `chat_message_room`, `chat_message_room_member`, `chat_message`: STOMP 실시간 협업 메신저.
- `notification`: SSE 및 웹소켓 실시간 알림.
- `system_config`, `system_feature`, `user_youtube_config`, `login_log`, `error_log`: 플랫폼 시스템 설정 및 진단 로그.
