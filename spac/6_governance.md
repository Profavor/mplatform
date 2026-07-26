# 10. 데이터 거버넌스 (Data Governance)

분류체계 및 데이터의 신뢰성, 보안, 추적성을 보장하기 위해 다음과 같은 거버넌스 기능을 포함한다.

### 10.1 권한 및 역할 관리 (RBAC) - 구현 완료 (스펙 대비 확장 구현)
데이터와 스키마에 대한 접근을 제어하기 위한 역할 기반 접근 제어. 실제 구현은 아래와 같이 5개 기본 역할 + 세분화된 권한 아이템(Permission Item) 체계로 구성되어 있다(당초 스펙의 4역할·`can_unmask_pii` 단일 속성 모델보다 확장됨).

| 역할 (`Role.name`) | 설명 |
|---|---|
| **ROLE_ADMIN** (시스템 관리자) | 시스템 전체 관리 권한 |
| **ORG_ADMIN** (조직 관리자) | 조직 내 모든 리소스 제어 권한 |
| **DATA_STEWARD** (데이터 스튜어드) | 데이터 품질 및 모델 관리 권한 |
| **DOMAIN_EDITOR** (도메인 편집자) | 도메인 및 필드 생성/편집 권한 |
| **VIEWER** (조회자) | 도메인/노드/필드/레코드/DQ 정보 읽기 전용 |

- **세분화된 권한(`PermissionMaster`/`PermissionGroup`/`PermissionItem`)**: 역할과 별개로 `record:write`, `record:unmask`, `domain:read`, `domain:write`, `workflow:read`, `workflow:write` 등 기능 단위 권한 아이템을 두어, 마스킹 해제(`record:unmask`)나 워크플로우 규칙 관리(`workflow:*`) 같은 민감 동작을 역할이 아닌 권한 아이템 단위로 제어한다. `workflow:*` 권한은 `admin:*` 권한과 OR 조건으로 평가되어, 시스템 관리자가 아니어도 워크플로우 관리 권한만 별도로 부여받은 사용자가 `/admin/workflow` 화면과 `/api/workflow-configs` API를 사용할 수 있다.
- **도메인 단위 접근 제어(`DomainPermission`)**: 사용자-도메인 매핑(`DomainAccessRequest`로 신청 → 승인 시 `DomainPermission` 생성) 방식이며, 현재는 **도메인 단위**로만 부여된다. 스펙에서 언급한 "노드 단위" 권한 부여는 아직 없음.

### 10.2 변경 이력 추적 (Audit Trail)
누가, 언제, 어떤 데이터를 변경했는지 완벽하게 추적할 수 있도록 데이터 및 스키마 수준의 감사 로그를 기록한다.

1. **스키마 감사 로그 (Schema Audit Log) - 구현 완료**
   - **백엔드 (`SchemaHistory` / `SchemaHistoryService`)**: 필드 추가/수정/논리삭제, 노드 생성/이동/삭제 이벤트를 변경자 ID, 변경 일시, 대상 엔티티, 이전 상태(`beforeData`)/변경 후 상태(`afterData`) JSON 스냅샷으로 기록한다.
   - **조회**: `SchemaHistoryController`를 통해 이력 목록을 조회할 수 있다.
2. **데이터 감사 로그 (Record Audit Log) - 구현 완료**
   - **백엔드 (`RecordHistory`)**: 데이터 생성(`CREATE`), 수정(`UPDATE`), 삭제(`DELETE`) 시 변경 전후 데이터를 JSONB 스냅샷으로 저장한다. 이력에는 변경자 UUID, 반영 일시(`changedAt`), 결재 문서 식별자(`approvalRequestId`)가 포함되어 승인 내역과 데이터 변경을 연결한다.
   - **프론트엔드 (이력 테이블)**: 과거 변경 내역을 내부 시스템 키 대신 번역된 필드명을 사용하여 가독성 높은 표 형태로 렌더링한다.
   - **스냅샷 보기**: 과거 특정 이력을 클릭하면, 해당 시점의 원본 데이터를 읽기 전용 팝업으로 복원하여 확인할 수 있다.
   - **결재 내역 역추적**: 이력 행 내의 [결재 내역] 버튼을 통해, 데이터를 변경하게 된 워크플로우 기안자, 결재 진행 과정, 코멘트 등을 즉시 확인할 수 있다.

> **남은 갭:** 스키마 감사 로그는 이력 저장/조회는 구현되었으나, 과거 특정 시점의 EffectiveFields를 재구성해 보여주는 "시점 조회(as-of)" API는 아직 없다.

### 10.3 스키마 및 데이터 변경 승인 워크플로우 (Approval Process) - 구현 완료
운영 환경에서 스키마(노드/필드)뿐만 아니라 **실제 데이터(레코드)**가 무분별하게 변경되는 것을 방지하기 위해 통일된 승인 워크플로우를 거친다. 두 워크플로우 모두 구현되어 있다.

**[데이터(Record) 워크플로우]**
0. **라우팅 룰 정의 (`workflow_config`):** 어드민 화면(`/admin/workflow`)에서 노드 또는 도메인 단위로, 행위 유형(`CREATE`/`UPDATE`/`DELETE`/`SCHEMA_CHANGE`)별 결재선과 사용자/역할 단위 필드 권한(편집가능/읽기전용/숨김)을 사전에 정의한다. 동일 대상+행위 유형에 여러 서식을 등록해 두고 기본 서식(`is_default`)을 지정할 수 있으며, 기안자가 기안 화면에서 서식을 직접 선택할 수도 있다. 상세 해석 순서는 `3_business_logic.md` 7.3절, JSON 스키마는 7.4절 참고.
1. **생성/수정/삭제 요청 (Pending):** Domain Editor 등 편집 권한을 가진 사용자가 레코드를 조작하면 즉시 데이터가 반영되지 않고, 이전/이후 변경 사항(Changes)을 담은 `ApprovalRequest`(상태: `PENDING`) 데이터가 생성된다. 이 시점에 워크플로우 규칙에 정의된 행위 권한 검증(허용되지 않은 행위면 `ACCESS_DENIED`로 즉시 차단), DQ 룰 검증(동기, hard-block, 편집 가능 필드로 범위 제한), 중복 검사(정확/퍼지)가 함께 수행된다.
2. **결재 단계 진행 (Steps):** 워크플로우 규칙의 결재선에 따라 순차적인 `ApprovalStep`이 생성되며, 할당된 결재권자들은 전용 "Approvals" 페이지에서 결재를 진행한다. 결재자는 화면상에서 Before/After 차이를 확인하고 **의견(Comment)** 을 기재한 후 승인 또는 반려를 선택한다. 같은 차수에 결재자가 여럿이면 전원이 승인해야 다음 차수로 진행된다(규칙 상 `approvalMode` ANY/ALL 옵션은 정의만 되어 있을 뿐 실제로는 적용되지 않는 갭이 있음).
3. **최종 승인(Approve) 및 반영:** 모든 결재 단계가 승인 처리되면, `RecordHistory`에 이력을 남김과 동시에 실제 원본 `Record` 테이블에 데이터가 최종 반영(`ACTIVE` 처리 또는 `DELETE`)된다.
4. **반려(Reject):** 결재 단계 중 하나라도 반려되면, 해당 상신 건 전체가 `REJECTED` 처리되며 원본 데이터는 수정 전 상태를 유지한다.

**[스키마(Schema) 워크플로우]**
1. **Draft 상태:** 도메인 관리자가 필드 추가/노드 이동을 수행하면(`SCHEMA_CHANGE` 워크플로우 설정이 있는 경우) 즉시 반영되지 않고 `ApprovalRequest`(`targetType=SCHEMA`)가 생성됨.
2. **Review & Approve:** 라우팅된 결재권자가 검토 후 승인(Approve) 혹은 반려(Reject)함.
3. **Publish:** 승인 시 실제 스키마 테이블(`field_definition`, `classification_node`)에 변경 사항이 반영되고 `SchemaHistory`에 Before/After가 기록된다.

### 10.4 데이터 마스킹 및 개인정보 보호 - 구현 완료 (키 관리는 갭 존재)
- `is_encrypted=true` 인 필드(예: 주민등록번호, 계좌번호 등)는 `FieldEncryptionService`(AES-256-GCM)로 암호화되어 `record.data` JSON 내에 값 자체가 암호문으로 대체 저장된다(별도 테이블 분리 없음, 5-way 라우팅 대신 **단일 컬럼 저장** — 상세는 `2_data_model.md` 참고).
- 조회 API 호출 시, 사용자가 `record:unmask` 권한 아이템을 보유했는지(`AuthContext.hasPermission("record:unmask")`)에 따라 마스킹 처리된 값(`******`)을 반환하거나 복호화된 평문을 반환함. 스펙에서 언급한 `can_unmask_pii` 속성명 대신 권한 아이템 체계로 구현됨.
- **갭:** 암호화 키는 애플리케이션 전역 단일 정적 마스터 키(환경변수)만 사용하며, 필드별/도메인별 키 분리나 외부 KMS(AWS KMS, Vault 등) 연동은 없음.
