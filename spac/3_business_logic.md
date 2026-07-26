# 4. 비즈니스 로직 및 생명주기

### 4.1 필드 상속 알고리즘 (EffectiveFields 계산) - 구현 완료
특정 노드 N의 유효 필드 목록을 구하는 절차:
1. 루트(도메인)부터 N까지의 조상 경로를 구한다: [root, ..., parent, N]
2. 경로를 따라 내려가며 각 노드에 정의된 field_definition을 순서대로 누적한다.
3. 동일 key가 다시 나타나면 가장 가까운(하위) 정의로 교체(Override) 한다.
4. is_removed = true인 정의가 나타나면 해당 key를 결과 집합에서 제외한다.
5. 최종적으로 남은 필드 집합 = N의 EffectiveFields.

> **갭:** `FieldDefinitionService.getEffectiveFields()`는 매 호출마다 트리를 순회하며 계산하고, 캐싱(`@Cacheable`)이 적용되어 있지 않다. Domain 조회는 Redis로 캐싱 중이므로 동일한 방식의 확장을 검토할 만하다.

### 4.2 노드 이동(Move) 시 데이터 처리 정책 - **미구현**
> 아래는 최초 설계이며, 실제 `ClassificationNodeService`/`ClassificationNodeController`에는 노드 이동(Move) 기능 자체가 없다(생성/수정/트리 조회만 구현됨). `Record.status`의 `MISMATCHED` 값도 엔티티 정의에만 존재할 뿐, 이 값을 실제로 세팅하는 서비스 로직은 없다. 노드 재배치가 필요한 시나리오가 있다면 아래 절차를 실제로 구현해야 한다.

노드를 다른 부모 아래로 이동시킬 경우, 이동 자체는 허용(유연성)하되 기존 데이터는 보호한다.
1. 노드의 parent_id 및 path, depth를 업데이트한다.
2. 이동된 노드의 새로운 EffectiveFields를 계산한다.
3. 해당 노드에 속한 기존 레코드들을 새로운 EffectiveFields와 비교한다.
4. 필수 필드 누락 등 스키마 불일치가 발생한 레코드들의 status를 ACTIVE에서 **MISMATCHED**로 변경한다.
5. 기본 조회 API에서는 WHERE status = 'ACTIVE' 조건을 주어 불일치 레코드를 자연스럽게 숨김 처리한다. 관리자는 별도 관리 화면에서 MISMATCHED 레코드를 보정할 수 있다.

### 4.3 노드 삭제(Delete) 정책 - **미구현**
> `ClassificationNodeController`에는 `DELETE` 매핑이 없다. `ClassificationNode.isDeleted` 컬럼(및 조회 시 `is_deleted = false` 필터)은 엔티티에 존재하지만, 이를 `true`로 전환하는 삭제 API/서비스 로직은 확인되지 않는다. 아래는 구현이 필요한 최초 설계다.

하위 레코드가 존재하더라도 데이터는 삭제하지 않고 연쇄 논리 삭제(Cascade Soft Delete) 를 수행한다.
1. 삭제 대상 노드의 is_deleted = true, deleted_at = 현재시간으로 업데이트.
2. path 컬럼을 활용하여 하위 노드를 효율적으로 탐색 후 모두 is_deleted = true로 변경.
   ```sql
   UPDATE classification_node 
   SET is_deleted = true, deleted_at = NOW() 
   WHERE path LIKE '/삭제노드경로/%' OR id = '삭제노드ID';
   ```
3. 하위 노드에 속한 레코드 데이터(record 등)는 물리적 삭제하지 않으나, 소속 노드가 논리 삭제되었으므로 자연스럽게 조회에서 제외된다.

### 4.4 도메인 참조(REFERENCE) 해석 로직
다른 도메인을 참조할 때, domain 테이블에 정의된 식별 속성을 활용하여 UI와 검색 쿼리를 동적으로 구성한다.
- 검색 키: 대상 도메인의 identifier_field_id가 가리키는 필드의 key
- 표시 라벨: 대상 도메인의 display_name_field_id가 가리키는 필드의 key
- 부가 설명: 대상 도메인의 description_field_id가 가리키는 필드의 key
- 동적 쿼리 예시: 사용자가 "홍"으로 검색 시, 백엔드는 display_name_field_id의 key를 찾아 `WHERE record.data->>'성명' LIKE '%홍%'` 쿼리를 동적으로 생성한다.

---

# 5. 암호화 방식
1차 구현은 자체 암복호화 로직 기준으로 한다.
- 알고리즘: AES-256-GCM (인증 태그 포함, 필드별 nonce 매번 새로 생성)
- 키 관리: 애플리케이션 서버 환경변수/Secret 저장소에 마스터 키 보관 (DB 하드코딩 금지)
- 시점: 저장 시 앱 레이어에서 암호화 후 DB 저장, 조회 시 앱 레이어에서 복호화
- 검색 제약: DB에서 직접 검색 불가. 동일 값 일치 검색이 필요한 필드는 평문을 HMAC 해시한 blind_index를 저장하여 동등 비교만 지원. (범위/부분 일치 검색 불가)

---

# 6. 데이터 품질 및 유효성 검증 (Data Quality & Validation)

데이터 오염(예: 숫자가 문자로 저장되는 현상) 방지 및 무결성을 보장하기 위해 프론트엔드와 백엔드에서 엄격한 검증을 수행한다.

### 6.1 UI 입력 제어 (프론트엔드)
- 동적 폼 생성 시 필드 타입(`nodeFields`)에 따라 적절한 컴포넌트로 렌더링.
- **숫자형 강제 차단**: `NUMBER`, `DECIMAL`, `FLOAT`, `INTEGER` 타입은 브라우저 네이티브 `<va-input type="number">`로 렌더링되어 텍스트 입력을 원천 차단.

### 6.2 데이터 타입 직렬화 (Type Casting)
- **저장 전 형변환 (`formatDataForSave`)**: 문자열로 넘어올 수 있는 폼 데이터를 실제 데이터 타입에 맞게 캐스팅.
  - 숫자형 데이터: `Number(val)`
  - 논리형 데이터: `Boolean(val)`
- **AG-Grid 호환성 최적화**: 동적 컬럼 바인딩으로 인한 "Invalid Number" 오류를 방지하기 위해 `cellDataType: false` 속성을 기본 설정으로 적용.

### 6.3 외부 서비스 연동 (Integration)
- **사용자 매핑**: UUID로 저장된 식별자(`requesterId`, `assigneeId` 등)를 인증/사용자 서비스 API(`/api/auth/users`)와 통신하여 실제 이름으로 매핑.
- **파일 업로드**: 파일(`FILE`) 필드 타입의 데이터는 전용 스토리지 API(`/api/files/upload`)를 통해 선업로드 후 발급받은 다운로드 경로만 JSON에 저장.

### 6.4 결재 모니터링 UI의 식별/이름 속성 추론 (Fallback)
데이터 생성 및 대량 업로드 등으로 인해 도메인 메타데이터(`identifier_field_id`, `display_name_field_id`)와 실제 동적 데이터 간의 매핑이 불완전하거나 누락될 경우, 프론트엔드 모니터링 화면(`useApprovalEnricher`)에서 다음의 Fallback 휴리스틱을 사용하여 **ID 속성** 및 **이름 속성**을 동적으로 추론하여 표시한다.
1. **키워드 매칭 (1st Pass)**: 저장된 JSON 데이터(`recordData`)의 필드명(한글 번역명 및 원본 영문 키)에 특정 키워드가 포함되어 있는지 순차적으로 검사.
   - **ID 속성 키워드**: '코드', '번호', '사번', 'id', 'code', 'ticker', 'no'
   - **이름 속성 키워드**: '명', '이름', 'name', 'title'
2. **강제 할당 (2nd Pass)**: 키워드 기반 매칭에도 실패할 경우, 원본 JSON 객체의 값들 중 단순 값(객체가 아닌 값) 목록을 추출하여 첫 번째 요소를 ID 속성으로, 두 번째 요소를 이름 속성으로 강제 할당한다. 이를 통해 관리자 화면에서 핵심 결재 대상 정보가 공백으로 표기되는 현상을 원천 방지한다.

### 6.5 저장 시점 실시간 차단 (구현 완료, 스펙에 누락되어 있던 실제 동작)
프론트엔드 입력 검증과는 별개로, 백엔드 `ApprovalService`는 레코드 생성/수정 기안 시점에 **동기적으로** 아래를 수행하며, 실패 시 기안 자체가 예외로 거부된다(soft-block이 아닌 hard-block).
1. **DQ 룰 검증**: `DataQualityService.validateData()`로 해당 노드의 모든 DQ 룰을 평가하고, 위반이 하나라도 있으면 `DATA_QUALITY_CHECK_FAILED` 오류로 기안을 막는다.
2. **중복 검사 및 자동 UPSERT**: `MatchingService.checkDuplicates()`(정확/퍼지)로 중복을 검사해, 중복 1건이면 자동으로 수정 요청으로 전환하고, 중복이 여러 건이면 `DEDUPLICATION_FAILED`로 기안을 막는다.

---



# 7. 결재/워크플로우 엔진 (Approval & Workflow Engine)

결재 상태 전이 및 워크플로우 진행 로직은 강결합을 피하기 위해 **Spring ApplicationEvent 기반의 이벤트 드리븐(Event-Driven) 아키텍처**로 구현되어 있다. 

### 7.1 주요 이벤트 및 리스너(`ApprovalEventListener`) 역할
- **`ApprovalRequestCreatedEvent`**: 결재가 상신되었을 때 발행.
  - **결재선 검증 및 자동 승인**: 실제 결재자(`stepOrder > 0`)가 0명일 경우, 시스템이 즉시 해당 결재 건 전체를 자동 승인(`APPROVED`) 처리하고 데이터를 최종 반영한다.
  - **기안자 자동 전결**: 첫 번째 결재자가 기안자 본인일 경우, 자동으로 해당 단계를 승인(`APPROVED`) 처리하고 다음 단계로 진행시킨다.
- **`ApprovalStepApprovedEvent`**: 특정 결재 단계가 승인되었을 때 발행.
  - **상태 전이(State Transition)**: 현재 차수(`currentStepOrder`)의 모든 결재자가 승인했는지 검사한다.
  - 모두 승인 완료 시, 다음 차수의 결재 단계들을 대기 상태(`PENDING`)로 활성화한다.
  - 더 이상 진행할 결재 차수가 없다면 전체 결재 요청을 **최종 승인(APPROVED)** 상태로 변경하고, `applyFinalApproval` 로직을 호출하여 `Record` 데이터의 물리적 저장(CREATE/UPDATE/DELETE)을 확정한다.

### 7.2 예외 상황 및 보완 로직
- **결재 반려(Reject) 연쇄 동기화**: 결재가 반려될 경우 이벤트 발행을 생략하고 즉시 전체 워크플로우를 종료하며, 대상이 `RECORD`인 경우 원본 레코드의 상태 역시 `REJECTED`로 동기화한다.
- **관리자 대리 결재 (Admin Proxy)**: `ADMIN` 역할을 가진 관리자는 담당자가 아니어도 특정 대기 중(`PENDING`)인 단계를 강제로 대리 승인/반려할 수 있다. 승인 시에는 동일하게 `ApprovalStepApprovedEvent`가 발행되어 이벤트 드리븐 파이프라인을 그대로 타게 되며, 이력에는 `(Admin Proxy)`가 남는다.

### 7.3 WorkflowConfig 해석(Resolution) 순서 - `ApprovalService.resolveWorkflows`
레코드/스키마 변경 기안 시점에 어떤 결재선을 적용할지는 `workflow_config` 테이블을 다음 순서로 조회하여 결정한다.
1. 대상 노드(`nodeId`)부터 시작해, 해당 노드에 `action_type`이 일치하고 `is_active=true`이며 **비어있지 않은**(`isEffectiveConfig`) `workflow_config`가 있는지 확인한다.
2. 없으면 부모 노드로 한 단계씩 올라가며(`ClassificationNode.parent`) 동일하게 반복한다. 즉, **가장 가까운 조상 노드의 설정이 우선**하며 상위(도메인에 더 가까운) 노드의 설정은 무시된다.
3. 트리 전체에 노드 레벨 설정이 하나도 없으면, 최종적으로 도메인 레벨(`domain_id`가 있고 `node_id`가 없는) 설정으로 폴백한다.
4. `isEffectiveConfig`는 `steps_config`가 `null`/빈 문자열이거나 정확히 `{"steps":[],"observerIds":[]}`(빈 결재선 placeholder)인 경우 "설정 없음"으로 취급해 건너뛴다.
5. 위 과정으로 찾은 목록(`resolveWorkflows`) 중 **하나를 선택**할 때(`resolveWorkflow`)는 `is_default=true`인 규칙을 우선하고, 없으면 목록의 첫 번째 규칙을 사용한다.
6. 레코드 기안(`RecordRequest.workflowConfigId`)에서 특정 규칙의 ID를 직접 지정한 경우, 위 탐색 로직 전체를 건너뛰고 해당 ID의 `workflow_config`를 그대로 사용한다(`resolveWorkflowById`).

> 하나의 노드/도메인에 동일 `action_type`으로 여러 `workflow_config`가 있을 수 있으므로(4.4 참고), 기안자는 프론트엔드에서 `GET /api/approval-requests/available-workflows/{nodeId}`로 후보 목록을 조회한 뒤 원하는 서식을 선택해 기안할 수 있다.

### 7.4 `steps_config` JSON 스키마와 필드/행위 단위 권한
`workflow_config.steps_config`는 아래 두 영역을 함께 담는 단일 JSON 컬럼이다.

```json
{
  "permissions": [
    {
      "targetType": "ROLE",
      "targetId": "",
      "targetRole": "DOMAIN_EDITOR",
      "ruleName": { "ko": "편집자 규칙" },
      "allowedActions": ["CREATE", "UPDATE"],
      "editableFields": ["종목명", "수량"],
      "readOnlyFields": ["등록일"],
      "hiddenFields": ["내부메모"]
    }
  ],
  "approvalLine": [
    { "stepOrder": 1, "stepName": "1차 검토", "assigneeType": "USER", "assigneeId": "...", "approvalMode": "ANY" }
  ],
  "observerIds": ["user-uuid-1"]
}
```
(과거 데이터 호환을 위해 `approvalLine` 대신 `steps` 키도 동일하게 인식한다.)

- **결재선 생성(`buildDynamicSteps`)**: `approvalLine`/`steps` 배열의 각 항목마다 `ApprovalStep`을 생성한다. `stepOrder`가 1인 단계만 최초 상태를 `PENDING`으로, 나머지는 `WAITING`으로 시작한다. `assigneeId`가 지정되면 UUID로, `assigneeRole`이 지정되면 역할로 저장된다.
- **행위 권한 검증(`validateUserActionPermission`, hard-block)**: 기안자의 사용자ID/역할과 `permissions[].targetType`+`targetId`(또는 `targetRole`)이 일치하는 규칙을 찾아, `allowedActions`에 요청 행위(`CREATE`/`UPDATE`)가 없으면 `ACCESS_DENIED` 예외로 기안 자체를 차단한다. 일치하는 권한 규칙이 없으면 제약 없이 통과한다.
- **필드 단위 권한(`extractEditableFields`/`extractReadOnlyFields`/`extractHiddenFields`)**: 동일한 매칭 로직으로 기안자에게 적용되는 규칙을 찾아 편집 가능/읽기 전용/숨김 필드 목록을 반환한다. `editableFields`가 반환되면 DQ 검증(`DataQualityService.validateData`) 시 해당 필드 목록으로 제한된다.
- **사용자 식별 매칭(`matchesUserIdentity`)**: `permissions[].targetId`에는 UUID 또는 username 문자열이 모두 저장될 수 있다. UUID 문자열 비교가 실패하면 `UserRepository`로 username → UUID 변환 후 재비교한다.
- **규칙 이름 조회(`extractRuleName`)**: 매칭된 권한 규칙의 `ruleName`을 그대로 반환해, `GET /api/approval-requests/effective-permission/{nodeId}` 응답에서 어떤 규칙이 적용되었는지 프론트엔드에 노출한다.
- **저장 시점 유효성 검증(`WorkflowConfigController.validateWorkflowConfig`)**: `steps_config` 저장(`POST /api/workflow-configs`, `/domain/{domainId}`, `/node/{nodeId}`) 시 `steps` 배열의 `stepOrder`가 1부터 시작해 공백 없이 연속(1,2,3...)해야 하며, 위반 시 `INVALID_WORKFLOW_CONFIG` 오류로 저장이 거부된다.
- **갭 (스펙에 없던 실제 동작):** `ApprovalStepConfigDto.approvalMode`(`ANY`/`ALL`) 필드가 존재하지만, 실제 결재 단계 진행 로직(`ApprovalEventListener.onApprovalStepApproved`)은 이 값을 참조하지 않고 항상 **같은 차수(stepOrder)의 모든 결재자가 승인해야 다음 차수로 진행**하는 것으로 고정 동작한다. 즉 UI/DTO 상으로만 존재하고 실제로는 강제되지 않는 미사용 옵션이다.

### 7.5 다중 워크플로우 관리 화면 (`/admin/workflow`)
- 하나의 노드 또는 도메인에 `action_type`(`CREATE`/`UPDATE`/`DELETE`/`SCHEMA_CHANGE`)별로 여러 개의 `workflow_config`를 등록·관리할 수 있으며, 목록 화면에서는 페이징(`GET /api/workflow-configs/page`)과 행위 유형/도메인/노드/검색어 필터를 지원한다.
- 규칙 저장 시 `is_default` 체크박스로 기본 규칙을 지정할 수 있고, `SCHEMA_CHANGE` 유형은 결재선(기안자 규칙)만 노출되며 그 외 유형은 결재선과 필드/행위 권한 규칙을 함께 설정한다.
- 노드/도메인 단위로 규칙을 저장(`POST /api/workflow-configs/node/{nodeId}`, `/domain/{domainId}`)하면 해당 노드(또는 도메인) + `node_id IS NULL` 조합의 기존 규칙을 전부 삭제한 뒤 새 목록으로 재생성하는 **전체 교체(replace-all)** 방식으로 동작한다. 개별 규칙만 부분 수정하려면 `POST /api/workflow-configs`(단건 upsert)를 사용해야 한다.
