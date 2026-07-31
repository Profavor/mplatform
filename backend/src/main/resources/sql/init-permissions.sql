-- ============================================================================
-- 시스템 퍼미션 및 역할, 메뉴 기본 설정 초기화 DDL / DML 스크립트
-- ============================================================================
-- Notice: TRUNCATE TABLE 명령을 절대 사용하지 않고 안전하게 레코드를 시딩합니다.
-- ============================================================================

-- 1. 기본 역할 (Role) 및 퍼미션 (Role Permissions) 초기화 데이터
-- (각 조직별 기본 역할 데이터 구성을 위한 시딩)

-- 1-1. ROLE_ADMIN (시스템 관리자) 퍼미션
-- permissions: '*' (모든 권한)
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, '*' FROM role r WHERE r.name IN ('ROLE_ADMIN', 'ADMIN')
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission = '*');

INSERT INTO role_permissions (role_id, permission)
SELECT r.id, 'admin:write' FROM role r WHERE r.name IN ('ROLE_ADMIN', 'ADMIN')
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission = 'admin:write');

INSERT INTO role_permissions (role_id, permission)
SELECT r.id, 'admin:read' FROM role r WHERE r.name IN ('ROLE_ADMIN', 'ADMIN')
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission = 'admin:read');

-- 1-2. ORG_ADMIN (조직 관리자) 퍼미션
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.perm FROM role r
CROSS JOIN (
    SELECT 'org:*' AS perm UNION ALL SELECT 'domain:*' UNION ALL SELECT 'node:*' UNION ALL
    SELECT 'field:*' UNION ALL SELECT 'dq:*' UNION ALL SELECT 'user:*' UNION ALL
    SELECT 'role:*' UNION ALL SELECT 'admin:read' UNION ALL SELECT 'record:*'
) p
WHERE r.name = 'ORG_ADMIN'
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission = p.perm);

-- 1-3. DATA_STEWARD (데이터 스튜어드) 퍼미션
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.perm FROM role r
CROSS JOIN (
    SELECT 'domain:read' AS perm UNION ALL SELECT 'domain:write' UNION ALL SELECT 'node:*' UNION ALL
    SELECT 'field:*' UNION ALL SELECT 'dq:read' UNION ALL SELECT 'dq:write' UNION ALL
    SELECT 'record:read' UNION ALL SELECT 'record:write'
) p
WHERE r.name = 'DATA_STEWARD'
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission = p.perm);

-- 1-4. DOMAIN_EDITOR (도메인 편집자) 퍼미션
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.perm FROM role r
CROSS JOIN (
    SELECT 'domain:read' AS perm UNION ALL SELECT 'domain:write' UNION ALL SELECT 'node:read' UNION ALL
    SELECT 'node:write' UNION ALL SELECT 'field:read' UNION ALL SELECT 'field:write' UNION ALL
    SELECT 'record:read'
) p
WHERE r.name = 'DOMAIN_EDITOR'
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission = p.perm);

-- 1-5. DQ_MANAGER (데이터 품질 관리자) 퍼미션
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.perm FROM role r
CROSS JOIN (
    SELECT 'dq:read' AS perm UNION ALL SELECT 'dq:write' UNION ALL SELECT 'dq_rule:*' UNION ALL SELECT 'dq_scan:*'
) p
WHERE r.name = 'DQ_MANAGER'
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission = p.perm);

-- 1-6. INTEGRATION_MANAGER (연계 관리자) 퍼미션
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.perm FROM role r
CROSS JOIN (
    SELECT 'integration:*' AS perm UNION ALL SELECT 'channel:*' UNION ALL SELECT 'org:read' UNION ALL
    SELECT 'domain:read' UNION ALL SELECT 'node:read' UNION ALL SELECT 'field:read' UNION ALL SELECT 'user:read'
) p
WHERE r.name = 'INTEGRATION_MANAGER'
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission = p.perm);

-- 1-7. VIEWER (조회자) 퍼미션
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.perm FROM role r
CROSS JOIN (
    SELECT 'domain:read' AS perm UNION ALL SELECT 'node:read' UNION ALL SELECT 'field:read' UNION ALL
    SELECT 'record:read' UNION ALL SELECT 'dq:read'
) p
WHERE r.name = 'VIEWER'
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission = p.perm);

-- 1-8. ROLE_USER (일반 사용자) 퍼미션
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.perm FROM role r
CROSS JOIN (
    SELECT 'domain:read' AS perm UNION ALL SELECT 'node:read' UNION ALL SELECT 'record:read'
) p
WHERE r.name IN ('ROLE_USER', 'USER')
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission = p.perm);


-- 2. 메뉴 및 메뉴 권한 매핑 (menu & menu_roles) 기본 시딩 확인
-- 2-1. 시스템 관리자 전용 서브 메뉴 권한 보정
INSERT INTO menu_roles (menu_id, role_name)
SELECT m.id, 'ROLE_ADMIN' FROM menu m
WHERE m.path IN (
    '/admin', '/admin/approval-monitor', '/admin/organizations', '/admin/users',
    '/admin/menus', '/admin/system-logs', '/admin/match-review', '/admin/workflow',
    '/admin/integration/channels', '/admin/matching-rules', '/admin/survivorship'
)
AND NOT EXISTS (SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role_name = 'ROLE_ADMIN');

INSERT INTO menu_roles (menu_id, role_name)
SELECT m.id, 'DATA_STEWARD' FROM menu m
WHERE m.path IN ('/admin/match-review', '/admin/matching-rules', '/admin/survivorship')
AND NOT EXISTS (SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role_name = 'DATA_STEWARD');

INSERT INTO menu_roles (menu_id, role_name)
SELECT m.id, 'INTEGRATION_MANAGER' FROM menu m
WHERE m.path IN ('/admin/integration/channels')
AND NOT EXISTS (SELECT 1 FROM menu_roles mr WHERE mr.menu_id = m.id AND mr.role_name = 'INTEGRATION_MANAGER');
