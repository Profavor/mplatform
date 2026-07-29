# 6. 거버넌스 & 스키마 감사

## 6.1 스키마 변경 워크플로우 & SchemaHistory
- 스키마 필드 및 노드의 생성/수정/삭제 조작은 `ApprovalService`를 거치며 `SCHEMA_CHANGE` 워크플로우 승인 후 반영된다.
- 스키마 변경 시 `SchemaHistory` 엔티티에 Before/After JSON 스냅샷 및 변경 사유가 시계열 기록된다.

## 6.2 스키마 과거 특정 시점 (As-Of) 조회
- `GET /api/nodes/{nodeId}/effective-fields/as-of?timestamp=` API를 통해 과거 특정 시점의 유효 필드 상태(EffectiveFields)를 재구성하여 조회할 수 있다.
- 스키마 변경 이력을 기반으로 레코드의 작성 당시 스키마 상태를 정확히 추적할 수 있다.

## 6.3 필드 수준 출처 계보 (Field Lineage)
- `RecordFieldSource` 엔티티가 `(record_id, field_key)`별로 최근 데이터 출처 소스 시스템명(`source_system`)과 변경 시각을 기록한다.
- 프론트엔드 레코드 상세 Drawer 및 결재 상세 Viewer에서 필드 단위 출처 정보가 시각화된다.
