# 3. 데이터 모델 (PostgreSQL 기준)

### 3.1 domain
도메인 자체의 정보뿐만 아니라, **이 도메인에 속하는 레코드(Record)를 대표하는 식별 필드**를 매핑한다. (도메인 참조 시 UI 렌더링 및 검색 키로 사용됨)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 도메인 ID |
| name | JSONB | 도메인명 다국어 맵 (예: `{"ko":"임직원", "en":"Employee"}`) |
| description | JSONB | 도메인 설명 다국어 맵 |
| identifier_field_id | UUID/FK, nullable | 레코드 식별자 필드 (예: 사번 필드의 ID). 참조 시 검색용 키로 사용됨. |
| display_name_field_id | UUID/FK, nullable | 레코드 표시명 필드 (예: 성명 필드의 ID). 참조 시 드롭다운에 표시됨. |
| description_field_id | UUID/FK, nullable | 레코드 설명 필드 (예: 직급 필드의 ID). 참조 시 부가 정보로 표시됨. |
| created_at / updated_at | datetime | |

> **무결성 규칙:** 위 3개의 FK가 가리키는 필드 정의는 반드시 해당 도메인의 루트 노드(모든 레코드가 상속받는)에 정의되어야 한다.

### 3.2 classification_node
도메인을 루트로 하는 트리. **본 스펙에서는 도메인=root node(parent_id = null)** 로 통합 관리한다.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 노드 ID |
| domain_id | UUID/FK | 소속 도메인 |
| parent_id | UUID/FK, nullable | 부모 노드 (null이면 도메인 루트) |
| name | JSONB | 노드명 다국어 맵 (예: `{"ko":"정규직", "en":"Regular"}`) |
| path | string | 조상 경로 캐시 (예: `/임직원/정규직/수습`), 조회 및 연쇄 작업 최적화용 |
| depth | int | 루트로부터의 깊이 |
| order | int | 형제 노드 간 정렬 순서 |
| is_deleted | boolean, default false | 논리적 삭제 여부 |
| deleted_at | datetime, nullable | 삭제 일시 |
| created_at / updated_at | datetime | |

> **무결성 규칙 (PostgreSQL 구현체):**
> 1. 순환 참조 금지(자기 자신 또는 자손을 parent로 지정 불가)
> 2. 동일 부모 하위 노드명 중복 금지 → **Partial Unique Index** 사용:
>    `CREATE UNIQUE INDEX idx_node_unique_name_active ON classification_node (domain_id, parent_id, name) WHERE is_deleted = false;`

### 3.3 field_definition
필드는 **특정 classification_node에 정의**되며, 그 노드와 모든 하위 노드에서 유효하다.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 필드 ID |
| defined_at_node_id | UUID/FK | 이 필드를 최초로 정의한 노드 |
| name | JSONB | 필드명 다국어 맵 (예: `{"ko":"입사일", "en":"Hire Date"}`) |
| key | string | 시스템용 식별자 (snake_case, 노드 내 유일) |
| type | enum | 필드 타입 (아래 3.3.1 참조) |
| options | JSON, nullable | 타입별 세부 설정 및 UI/Validation 힌트 (아래 3.3.2 참조) |
| required | boolean | 필수 여부 |
| default_value | JSON, nullable | 기본값 |
| order | int | 표시 순서 |
| is_removed | boolean, default false | 상속된 필드를 이 노드부터 제외시킬지 여부 (Soft delete형 오버라이드) |
| is_multi_value | boolean, default false | 하나의 필드에 여러 값(배열)을 허용할지 여부 (FILE, REFERENCE 타입은 별도 테이블로 관리하므로 불필요) |
| is_table | boolean, default false | 필드 값이 테이블형(행x열) 데이터인지 여부 |
| is_encrypted | boolean, default false | 값을 암호화하여 저장해야 하는지 여부 (FILE, REFERENCE 타입에는 적용 불가) |
| is_searchable | boolean, default false | 검색/필터링 대상 필드로 지정할지 여부 (인덱싱 대상) |
| is_highlighted | boolean, default false | 필드 표시 시 강조(Highlight) 여부 |
| created_at / updated_at | datetime | |

#### 3.3.1 필드 타입 (type Enum)
`TEXT`, `NUMBER`, `DATE`, `TIME`, `I18N`, `RICH_TEXT`, `SELECT`, `MULTI_SELECT`, `TABLE`, `FILE`, `REFERENCE`

#### 3.3.2 필드 옵션 (options JSON) 권장 스펙
프론트엔드 UI 렌더링 및 백엔드 Validation을 위해 타입별로 아래 구조를 준수한다.

| type | options JSON 스펙 예시 |
| :--- | :--- |
| **TEXT** | `{ "max_length": 50, "placeholder": "이름 입력" }` |
| **NUMBER** | `{ "min": 0, "max": 100, "step": 0.1, "unit": "kg" }` |
| **DATE** | `{ "format": "YYYY-MM-DD" }` |
| **TIME** | `{ "format": "HH:mm" }` |
| **I18N** | `{ "supported_locales": ["ko", "en", "ja"] }` |
| **RICH_TEXT**| `{ "max_length": 5000, "toolbar": ["bold", "image"] }` |
| **SELECT** | `{ "choices": [ {"value": "male", "label": "남성"}, {"value": "female", "label": "여성"} ] }` |
| **MULTI_SELECT**| `{ "choices": [ {"value": "reading", "label": "독서"} ], "max_count": 3 }` |
| **TABLE** | `{ "columns": [ {"key": "company", "type": "TEXT", "required": true}, {"key": "start_date", "type": "DATE"} ] }` *(행 내부 스키마 정의)* |
| **FILE** | `{ "allowed_extensions": ["jpg", "pdf"], "max_size_mb": 10, "max_count": 5 }` |
| **REFERENCE**| `{ "target_domain_ids": ["참조가능도메인_UUID"], "is_multi": false }` |

### 3.4 실제 데이터 저장 구조

> **구현 현황 안내:** 아래는 당초 설계한 "5-way 라우팅" 구조이며, 검색/타입 안전성 관점의 이상적인 모델이다. **실제 구현은 이보다 단순화되어 있다** — `record.data` 단일 JSONB 컬럼에 TABLE형 필드의 행 데이터, 파일 필드의 다운로드 경로, REFERENCE 필드의 대상 레코드 ID, 심지어 암호화된 값(`is_encrypted=true`)까지 모두 함께 저장된다. 별도의 `record_table_field`, `record_encrypted_value`, `record_file`, `record_relation` 테이블/엔티티는 존재하지 않는다. 이 방식은 스키마가 단순해지는 대신, TABLE 필드의 행 단위 검색이나 REFERENCE의 역참조(어떤 레코드들이 나를 참조하는지) 조회가 어렵다는 트레이드오프가 있다.

#### (1) record — 기본 레코드 + 전체 필드 (JSONB, 실제 구현)
스칼라 값, 다중값(배열), 다국어, 선택형뿐 아니라 TABLE 행 데이터, FILE 경로, REFERENCE 대상 ID, 암호문(encrypted)까지 전부 이 컬럼에 저장한다.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 레코드 ID |
| node_id | UUID/FK | 어떤 분류 노드에 속하는 데이터인지 |
| status | enum | 레코드 상태 (`DRAFT`, `PENDING_APPROVAL`, `ACTIVE`, `INACTIVE`, `MISMATCHED`, `REJECTED`, `MERGED`) — 실제 구현에는 병합된 레코드를 나타내는 `MERGED` 상태가 추가됨 |
| data | JSONB (실제로는 String 컬럼에 JSON 직렬화) | 전체 필드 값 모음. 예: `{"이름":"홍길동", "보유자격증":["정보처리기사"]}` |
| version | int | 레코드 버전(수정마다 증가) |
| source_system | string | 이 레코드(또는 최근 갱신)를 생성한 소스 시스템명 |
| merged_into_record_id | UUID, nullable | 병합되어 사라진 경우 병합 대상(survivor) 레코드 ID |
| approval_request_id | UUID, nullable | 현재 레코드를 생성/변경한 결재 요청 ID |
| created_at / updated_at | datetime | |

> **필드별 소스 계보:** `record_field_source` 테이블이 실제로 존재하며, `(record_id, field_key)` 별로 최근 값을 기록한 소스 시스템명과 갱신 시각을 저장한다. 인바운드 연계 시 `source_priority`(소스 시스템 우선순위) 규칙에 따라 필드 단위로 병합(Survivorship)할 때 사용된다.

> **검색 최적화:** `is_searchable=true`인 필드는 표현식 인덱스 생성. (예: `CREATE INDEX idx_record_name ON record ((data->>'이름'));`)

#### (1-2) approval_request — 거버넌스 승인 워크플로우 관리 테이블
데이터 및 스키마 변경 사항에 대한 결재/승인 상태를 관리한다.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | UUID/PK | 승인 요청 ID |
| target_type | enum | 변경 대상 유형 (`SCHEMA`, `RECORD`) |
| target_id | UUID | 변경 대상 ID (노드 ID 또는 레코드 ID) |
| requester_id | UUID | 요청자 사용자 ID |
| approver_id | UUID, nullable | 승인자 사용자 ID |
| status | enum | 승인 상태 (`PENDING`, `APPROVED`, `REJECTED`) |
| changes | JSONB | 변경될 내용 (예: 레코드 수정 시 새로운 data JSON, 스키마 변경 시 필드 정의) |
| created_at / updated_at | datetime | |

#### (2) ~ (5) 원 설계상의 분리 테이블 (미구현, 참고용)
아래 4개는 최초 설계 문서에만 존재하며 실제 코드베이스에는 구현되어 있지 않다. 위 (1)의 `data` 컬럼이 이 역할을 대신한다.
- **record_table_field**: 한 필드가 여러 행 x 여러 컬럼 구조를 가질 때(`type='TABLE'`) 사용할 예정이었던 테이블.
- **record_encrypted_value**: 암호화 대상 값(`is_encrypted=true`)과 동등 비교용 blind index를 저장할 예정이었던 테이블.
- **record_file**: FILE 타입 필드의 첨부파일 메타데이터를 저장할 예정이었던 테이블. 실제로는 `/api/files/upload`로 선업로드 후 반환된 다운로드 경로 문자열이 `record.data`에 그대로 저장된다.
- **record_relation**: REFERENCE 타입 필드의 대상 레코드 ID를 저장할 예정이었던 테이블. 실제로는 대상 레코드의 UUID가 `record.data`에 그대로 저장된다(역참조 조회를 위한 별도 인덱스 테이블 없음).
