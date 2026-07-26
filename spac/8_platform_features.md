# 8. 플랫폼 부가 기능 (실제 구현 기준, 최초 설계 문서에 없던 신규 문서)

> 이 문서는 1~7번 문서(도메인/분류체계/DQ/매칭/거버넌스/연계)가 다루지 않는, 실제 코드베이스에서 확인된 플랫폼 부가 기능을 정리한다. MDM 코어 로직은 아니지만 결재 라우팅, 접근 제어, 운영 모니터링에 실제로 쓰이고 있어 문서화가 필요하다.

---

## 8.1 조직 구조 (Organization / Department / Team) — 구현 완료

결재 라인 지정(`WorkflowConfig.assigneeId`)과 도메인 접근 제어의 기반이 되는 조직도 마스터 데이터. 3단 계층 구조다: **Organization → Department(자기참조 트리) → Team**.

| 엔티티 | 주요 컬럼 | 설명 |
|---|---|---|
| **organization** | `name`(unique), `display_name`, `description`, `icon`, `is_active` | 최상위 조직(회사/법인 단위) |
| **department** | `organization_id`(FK), `parent_department_id`(자기참조, nullable), `name`, `roles`(Set&lt;String&gt;), `icon`, `is_active` | 조직 하위 부서. `parent_department_id`로 부서 트리(본부→팀 등) 구성 가능 |
| **team** | `organization_id`(FK), `department_id`(FK), `name`, `description`, `is_active` | 부서 하위 팀(가장 말단 단위) |

- **API**: `/api/organizations`(CRUD), `/api/organizations/{orgId}/departments`(CRUD, 중첩), `/api/organizations/{orgId}/teams`(조회/생성)
- **Domain과의 연결**: `Domain.organization_id`로 특정 도메인을 특정 조직 소유로 지정할 수 있다(2_data_model.md 3.1 참고).
- **Department.roles / role(legacy)**: 부서에 역할(Role) 문자열 집합을 매핑해두는 필드가 있으나, 6_governance.md의 `Role`/`PermissionItem` 체계와 정확히 어떻게 연동되는지는 코드상 추가 확인이 필요하다(레거시 `role` 단일 필드와 신규 `roles` Set이 공존).

---

## 8.2 메뉴 관리 및 접근 제어 (Menu) — 구현 완료

역할(Role)별로 좌측 네비게이션 메뉴 노출 여부를 제어하고, 메뉴 접근 이력을 로깅한다.

| 엔티티 | 주요 컬럼 | 설명 |
|---|---|---|
| **menu** | `name`, `path`, `icon`, `parent_id`(자기참조 트리), `sort_order`, `required_role`(legacy 단일 역할), `required_roles`(Set&lt;String&gt;, 다중 역할 허용), `is_active` | 메뉴 트리 노드 |
| **menu_access_log** | `menu_id`, `menu_path`, `user_id`, `user_agent`, `client_ip`, `accessed_at` | 메뉴 클릭/접근 이력 |

- **API**
  - `GET /api/menus/tree` — 트리 구조로 메뉴 조회 (프론트에서 현재 사용자 역할에 맞게 필터링)
  - `POST /api/menus`, `PUT /api/menus/{id}`, `DELETE /api/menus/{id}` — 관리자 메뉴 CRUD (`admin/menus.vue`)
  - `POST /api/menus/access` — 메뉴 클릭 시 접근 로그 기록
  - `GET /api/menus/logs` — 접근 로그 조회(감사용)
- **초기화**: `MenuDataInitializer`(config 패키지)가 애플리케이션 최초 기동 시 기본 메뉴 트리를 시딩한다.

---

## 8.3 실시간 알림 (Notification & SSE) — 구현 완료

결재 처리·DQ 위반 스캔 등 백엔드 이벤트 발생 시, 대상 사용자에게 서버-전송 이벤트(Server-Sent Events)로 실시간 알림을 push한다.

| 컬럼 | 설명 |
|---|---|
| user_id | 알림 수신 대상 사용자 |
| title / message | 알림 제목/본문 |
| type | `INFO`, `WARNING`, `APPROVAL`, `DQ_VIOLATION` |
| is_read | 읽음 여부 |
| link_url | 클릭 시 이동할 프론트엔드 경로(예: 해당 결재 상세 화면) |

- **API**
  - `GET /api/notifications` — 알림 목록(페이징)
  - `GET /api/notifications/unread-count` — 안 읽은 알림 수
  - `GET /api/notifications/subscribe` — SSE 구독 엔드포인트(`SseNotificationService`가 커넥션을 유지하며 실시간 push)
- **발생 트리거(확인된 범위)**
  - `ApprovalEventListener` — 결재 요청 생성/승인/반려 시 기안자·결재자에게 알림
  - `DqScheduledScanService` — 정기 DQ 스캔에서 위반 발견 시 알림
- **갭:** 연계(Integration) 실패 시 알림을 보내는 트리거는 코드상 확인되지 않음(README/제안 문서의 "아웃바운드 실패 알림"이 아직 이 체계와 연결되어 있지 않음).

---

## 8.4 자동 채번 (Numbering Service) — 구현 완료

레코드 생성 시 도메인별로 설정된 패턴에 따라 고유 코드를 자동 발급한다(예: 사번, 발주번호).

- **설정 위치**: `Domain.numbering_pattern`(패턴 문자열), `Domain.current_sequence`(현재 시퀀스 값) — 2_data_model.md 3.1 참고.
- **패턴 토큰**: `{YYYY}`/`{MM}`/`{DD}`(오늘 날짜 기준 치환), `{SEQ:n}`(n자리 0-padding 시퀀스). 예: `EMP-{YYYY}-{SEQ:5}` → `EMP-2026-00042`
- **동작 (`NumberingService`)**
  - `generateCode(domain)`: 시퀀스를 증가시키지 않고 현재 값 기준으로 미리보기 코드만 생성(폼에 "예상 채번값"을 보여줄 때 사용).
  - `issueNextCode(domainId)`: `synchronized` + `@Transactional`로 시퀀스를 1 증가시키고 DB에 반영한 뒤 실제 코드를 발급(레코드 생성 확정 시 사용). 동시성 이슈를 락으로 방지.

---

## 8.5 시스템 로그 (Error Log / Login Log) — 구현 완료

| 엔티티 | 주요 컬럼 | 설명 |
|---|---|---|
| **error_log** | `user_id`, `request_uri`, `error_message`, `stack_trace`, `logged_at` | 처리되지 않은 예외를 전역 핸들러가 자동 기록(전역 `@ExceptionHandler` 추정) |
| **login_log** | `user_id`, `username`, `user_agent`, `client_ip`, `login_at` | 로그인 성공 시마다 기록 |

- **조회**: `admin/system-logs.vue` 화면에서 관리자가 조회 (`ErrorLogController`, 로그인 로그는 별도 컨트롤러 확인 필요 — Auth 흐름 쪽에 포함되어 있을 가능성 있음).
- **용도**: 장애 대응, 이상 로그인 패턴(비정상 IP 반복 로그인 등) 모니터링의 기초 데이터. 현재는 저장/조회만 있고, 임계치 기반 알림(8.3의 Notification과 연동)은 없음.

---

## 8.6 대시보드 (Dashboard) — 구현 완료 (단순 카운터 수준)

- **API**: `DashboardController` → `DashboardService.getStats()`가 `Map<String, Long>` 형태의 집계 통계(도메인 수, 레코드 수, 대기 중인 결재 건수 등 추정)를 반환.
- **갭:** 이름 그대로 요약 카운터 제공 수준이며, 시계열 추이나 도메인별 세부 분석은 없음. 3_business_logic.md/DQ 개선 제안에서 언급한 "DQ 스코어 트렌드 대시보드"와 통합하면 시너지가 있을 것으로 보임.

---

## 8.7 정리: 문서 커버리지 갱신

| 기능 | 이전 문서화 상태 | 현재 |
|---|---|---|
| 계산 필드(CALCULATED) | 없음 | `2_data_model.md` 3.3.1/3.3.2에 추가 |
| 결재 라우팅 설정(WorkflowConfig) | "라우팅 룰"로만 언급 | `6_governance.md` 10.3.1에 상세 추가 |
| 조직/부서/팀 | README 한 줄 언급 | 본 문서 8.1 |
| 메뉴 관리 | 없음 | 본 문서 8.2 |
| 알림/SSE | 없음 | 본 문서 8.3 |
| 자동 채번 | 없음 | 본 문서 8.4, `2_data_model.md` 3.1 |
| 시스템/로그인 로그 | 없음 | 본 문서 8.5 |
| 대시보드 | 없음 | 본 문서 8.6 |
