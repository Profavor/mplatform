import 'package:flutter_test/flutter_test.dart';
import 'package:mplatform_mobile/core/security/permission_helper.dart';

void main() {
  group('PermissionHelper Tests (TDD - admin(*) & Wildcard Protection)', () {
    test('returns false when user permissions list is empty', () {
      expect(PermissionHelper.hasPermission('domain:read', []), false);
      expect(PermissionHelper.hasPermission('record:write', null), false);
    });

    test('returns true for all checks when user possesses admin(*) permission', () {
      final perms = ['admin(*)', 'user:read'];
      expect(PermissionHelper.hasPermission('domain:read', perms), true);
      expect(PermissionHelper.hasPermission('domain:write', perms), true);
      expect(PermissionHelper.hasPermission('record:delete', perms), true);
      expect(PermissionHelper.hasPermission('workflow:approve', perms), true);
    });

    test('returns true for all checks when user possesses admin:* trailing wildcard permission', () {
      final perms = ['admin:*'];
      expect(PermissionHelper.hasPermission('admin:write', perms), true);
      expect(PermissionHelper.hasPermission('admin:read', perms), true);
      expect(PermissionHelper.hasPermission('domain:write', perms), true);
      expect(PermissionHelper.hasPermission('record:delete', perms), true);
      expect(PermissionHelper.hasPermission('workflow:approve', perms), true);
    });

    test('returns true for global wildcard (* or *:*) permissions', () {
      expect(PermissionHelper.hasPermission('domain:read', ['*']), true);
      expect(PermissionHelper.hasPermission('record:write', ['*:*']), true);
      expect(PermissionHelper.hasPermission('admin:write', ['*:write']), true);
    });

    test('returns true for domain-specific wildcard (e.g., domain:*)', () {
      final perms = ['domain:*', 'record:read'];
      expect(PermissionHelper.hasPermission('domain:read', perms), true);
      expect(PermissionHelper.hasPermission('domain:write', perms), true);
      expect(PermissionHelper.hasPermission('domain:delete', perms), true);
      expect(PermissionHelper.hasPermission('record:write', perms), false); // only record:read is possessed
    });

    test('returns true for exact permission matching and case-insensitive check', () {
      final perms = ['RECORD:READ', 'workflow:approve'];
      expect(PermissionHelper.hasPermission('record:read', perms), true);
      expect(PermissionHelper.hasPermission('WORKFLOW:APPROVE', perms), true);
      expect(PermissionHelper.hasPermission('record:write', perms), false);
    });

    test('isAdmin Returns true when role or permissions indicate admin status', () {
      expect(PermissionHelper.isAdmin(role: 'ADMIN', permissions: []), true);
      expect(PermissionHelper.isAdmin(role: 'ROLE_ADMIN', permissions: []), true);
      expect(PermissionHelper.isAdmin(role: 'user', permissions: ['admin(*)']), true);
      expect(PermissionHelper.isAdmin(role: 'user', permissions: ['*']), true);
      expect(PermissionHelper.isAdmin(role: 'user', permissions: ['record:read']), false);
    });
  });
}
