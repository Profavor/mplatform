class PermissionHelper {
  /// Check if the user possesses the required permission, considering global wildcards and admin(*) rules.
  static bool hasPermission(String requiredPermission, List<String>? userPermissions) {
    if (userPermissions == null || userPermissions.isEmpty) {
      return false;
    }

    final normalizedRequired = requiredPermission.trim().toLowerCase();

    for (final perm in userPermissions) {
      final normalized = perm.trim().toLowerCase();

      // 1. Check global wildcards and admin(*) rules
      if (normalized == 'admin(*)' ||
          normalized == 'admin:*' ||
          normalized == '*' ||
          normalized == '*:*' ||
          normalized == 'role_admin') {
        return true;
      }

      // 2. Prefix wildcard (e.g., *:write)
      if (normalized.startsWith('*:') && normalized.substring(2) == normalizedRequired.split(':').last) {
        return true;
      }

      // 3. Exact match
      if (normalized == normalizedRequired) {
        return true;
      }

      // 4. Resource wildcard (e.g., domain:* matches domain:read, domain:write)
      if (normalizedRequired.contains(':') && normalized.endsWith(':*')) {
        final prefix = normalizedRequired.split(':').first + ':*';
        if (normalized == prefix) {
          return true;
        }
      }
    }

    return false;
  }

  /// Check if user has administrative level permissions or role.
  static bool isAdmin({required String? role, required List<String>? permissions}) {
    if (role != null) {
      final normRole = role.trim().toLowerCase();
      if (normRole == 'admin' || normRole == 'role_admin') {
        return true;
      }
    }
    if (permissions != null) {
      for (final perm in permissions) {
        final norm = perm.trim().toLowerCase();
        if (norm == 'admin(*)' || norm == 'admin:*' || norm == '*' || norm == '*:*') {
          return true;
        }
      }
    }
    return false;
  }
}
