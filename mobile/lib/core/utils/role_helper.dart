import 'package:flutter/material.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/utils/l10n_helper.dart';

class RoleHelper {
  static String formatRoleText(String? code, BuildContext context, {bool includeIcon = true}) {
    if (code == null || code.trim().isEmpty) return '';
    final l10n = AppLocalizations.of(context)!;
    final norm = code.replaceFirst(RegExp(r'^ROLE_'), '').toUpperCase().trim();

    switch (norm) {
      case 'ADMIN':
        return includeIcon ? '👑 ${l10n.roleAdmin}' : l10n.roleAdmin;
      case 'ORG_ADMIN':
        return '조직 관리자';
      case 'DATA_STEWARD':
        return '데이터 스튜어드';
      case 'DOMAIN_EDITOR':
        return l10n.roleDomainEditor;
      case 'DQ_MANAGER':
        return '품질 관리자';
      case 'INTEGRATION':
        return '연동 관리자';
      case 'WORKFLOW':
        return '결재 관리자';
      case 'RECORD_MANAGER':
        return l10n.roleRecordManager;
      case 'USER':
        return l10n.roleUser;
      case 'VIEWER':
        return l10n.roleDomainViewer;
      default:
        return L10nHelper.parseLocalizedMap(code, context);
    }
  }

  static BoxDecoration getRoleBadgeDecoration(String? code) {
    final norm = (code ?? '').replaceFirst(RegExp(r'^ROLE_'), '').toUpperCase().trim();
    Color startColor;
    Color endColor;
    Color borderColor;

    switch (norm) {
      case 'ADMIN':
        startColor = const Color(0xFFEF4444);
        endColor = const Color(0xFFDC2626);
        borderColor = const Color(0xFFFCA5A5).withOpacity(0.4);
        break;
      case 'ORG_ADMIN':
        startColor = const Color(0xFFF59E0B);
        endColor = const Color(0xFFD97706);
        borderColor = const Color(0xFFFCD34D).withOpacity(0.4);
        break;
      case 'DATA_STEWARD':
        startColor = const Color(0xFF2563EB);
        endColor = const Color(0xFF1D4ED8);
        borderColor = const Color(0xFF93C5FD).withOpacity(0.4);
        break;
      case 'DOMAIN_EDITOR':
        startColor = const Color(0xFF06B6D4);
        endColor = const Color(0xFF0891B2);
        borderColor = const Color(0xFF67E8F9).withOpacity(0.4);
        break;
      case 'DQ_MANAGER':
        startColor = const Color(0xFF10B981);
        endColor = const Color(0xFF059669);
        borderColor = const Color(0xFF6EE7B7).withOpacity(0.4);
        break;
      case 'INTEGRATION':
        startColor = const Color(0xFF8B5CF6);
        endColor = const Color(0xFF7C3AED);
        borderColor = const Color(0xFFC4B5FD).withOpacity(0.4);
        break;
      case 'WORKFLOW':
        startColor = const Color(0xFFF97316);
        endColor = const Color(0xFFEA580C);
        borderColor = const Color(0xFFFED7AA).withOpacity(0.4);
        break;
      case 'USER':
        startColor = const Color(0xFF6366F1);
        endColor = const Color(0xFF4F46E5);
        borderColor = const Color(0xFFA5B4FC).withOpacity(0.4);
        break;
      case 'VIEWER':
      default:
        startColor = const Color(0xFF64748B);
        endColor = const Color(0xFF475569);
        borderColor = const Color(0xFFCBD5E1).withOpacity(0.4);
        break;
    }

    return BoxDecoration(
      gradient: LinearGradient(
        colors: [startColor, endColor],
        begin: Alignment.topLeft,
        end: Alignment.bottomRight,
      ),
      borderRadius: BorderRadius.circular(12),
      border: Border.all(color: borderColor, width: 1),
      boxShadow: [
        BoxShadow(
          color: endColor.withOpacity(0.25),
          blurRadius: 4,
          offset: const Offset(0, 2),
        ),
      ],
    );
  }
}
