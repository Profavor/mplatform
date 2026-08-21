import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/core/providers/locale_provider.dart';
import 'package:mplatform_mobile/core/utils/role_helper.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';

class ProfileSettingsScreen extends ConsumerStatefulWidget {
  const ProfileSettingsScreen({super.key});

  @override
  ConsumerState<ProfileSettingsScreen> createState() => _ProfileSettingsScreenState();
}

class _ProfileSettingsScreenState extends ConsumerState<ProfileSettingsScreen> {
  late String _currentTimezone;

  final List<Map<String, String>> _timezones = [
    {'code': 'Asia/Seoul', 'label': 'Asia/Seoul (KST, UTC+9)'},
    {'code': 'UTC', 'label': 'UTC (Universal Coordinated Time)'},
    {'code': 'America/New_York', 'label': 'America/New_York (EST, UTC-5)'},
    {'code': 'America/Los_Angeles', 'label': 'America/Los_Angeles (PST, UTC-8)'},
    {'code': 'Europe/London', 'label': 'Europe/London (GMT/BST)'},
    {'code': 'Asia/Tokyo', 'label': 'Asia/Tokyo (JST, UTC+9)'},
  ];

  @override
  void initState() {
    super.initState();
    final prefs = ref.read(sharedPreferencesProvider);
    _currentTimezone = prefs.getString('user_personal_timezone') ?? 'Asia/Seoul';
  }

  Future<void> _changeTimezone(String tz) async {
    setState(() {
      _currentTimezone = tz;
    });
    final prefs = ref.read(sharedPreferencesProvider);
    await prefs.setString('user_personal_timezone', tz);
    if (mounted) {
      final l10n = AppLocalizations.of(context);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('${l10n.timezone}: $tz')),
      );
    }
  }

  Future<void> _handleLogout() async {
    final l10n = AppLocalizations.of(context);
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(l10n.logout),
        content: Text(l10n.logout),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: Text(l10n.cancel),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            onPressed: () => Navigator.pop(ctx, true),
            child: Text(l10n.logout),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      await ref.read(authControllerProvider.notifier).logout();
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final currentUser = ref.watch(authControllerProvider).valueOrNull;
    final currentLocale = ref.watch(localeProvider);

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.userProfileTitle),
        elevation: 1,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // User Profile Card (Matching Web Frontend Look & Feel)
            Container(
              decoration: BoxDecoration(
                color: const Color(0xFF1E1B4B),
                borderRadius: BorderRadius.circular(16),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.12),
                    blurRadius: 10,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              padding: const EdgeInsets.all(18),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // User Avatar & Name & Org
                  Row(
                    children: [
                      CircleAvatar(
                        radius: 28,
                        backgroundColor: const Color(0xFFFBBF24),
                        child: Text(
                          (currentUser?.name.isNotEmpty == true
                                  ? currentUser!.name[0]
                                  : currentUser?.username.isNotEmpty == true
                                      ? currentUser!.username[0]
                                      : 'U')
                              .toUpperCase(),
                          style: const TextStyle(fontSize: 22, color: Color(0xFF1E1B4B), fontWeight: FontWeight.w900),
                        ),
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Text(
                                  currentUser?.name.isNotEmpty == true
                                      ? currentUser!.name
                                      : (currentUser?.username ?? l10n.username),
                                  style: const TextStyle(
                                    fontSize: 18,
                                    fontWeight: FontWeight.w900,
                                    color: Colors.white,
                                    letterSpacing: -0.3,
                                  ),
                                ),
                                if (currentUser?.role.toUpperCase().contains('ADMIN') == true) ...[
                                  const SizedBox(width: 6),
                                  const Icon(Icons.verified, color: Color(0xFFFBBF24), size: 18),
                                ],
                              ],
                            ),
                            const SizedBox(height: 4),
                            Row(
                              children: [
                                Text(
                                  '🏢 ${(currentUser?.orgName?.isNotEmpty == true ? currentUser!.orgName : (currentUser?.department.isNotEmpty == true ? currentUser!.department : l10n.belongsToOrg))}',
                                  style: TextStyle(fontSize: 13, color: Colors.grey[300], fontWeight: FontWeight.w500),
                                ),
                                if (currentUser?.deptName?.isNotEmpty == true) ...[
                                  Text(
                                    ' | 🏬 ${currentUser!.deptName}',
                                    style: TextStyle(fontSize: 13, color: Colors.grey[300], fontWeight: FontWeight.w500),
                                  ),
                                ],
                              ],
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),

                  // Effective Roles Section (Matching Web Frontend)
                  Container(
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.08),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: Colors.white.withOpacity(0.12)),
                    ),
                    padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              l10n.effectiveRoles,
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.w800,
                                color: Colors.grey[300],
                                letterSpacing: 0.5,
                              ),
                            ),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 1.5),
                              decoration: BoxDecoration(
                                color: Colors.black.withOpacity(0.35),
                                borderRadius: BorderRadius.circular(10),
                              ),
                              child: const Text(
                                'UNION',
                                style: TextStyle(
                                  fontSize: 9,
                                  fontWeight: FontWeight.w900,
                                  color: Color(0xFFDDD6FE),
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 8),

                        // Roles Badges
                        Wrap(
                          spacing: 6,
                          runSpacing: 6,
                          children: [
                            Container(
                              decoration: RoleHelper.getRoleBadgeDecoration(currentUser?.role ?? 'ROLE_USER'),
                              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4.5),
                              child: Text(
                                RoleHelper.formatRoleText(currentUser?.role ?? 'ROLE_USER', context),
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 12,
                                  fontWeight: FontWeight.w800,
                                ),
                              ),
                            ),
                          ],
                        ),

                        // Permissions Section
                        if (currentUser?.permissions.isNotEmpty == true) ...[
                          const SizedBox(height: 10),
                          Divider(color: Colors.white.withOpacity(0.15), height: 1),
                          const SizedBox(height: 8),
                          Text(
                            'PERMISSIONS',
                            style: TextStyle(
                              fontSize: 10,
                              fontWeight: FontWeight.w800,
                              color: Colors.grey[400],
                              letterSpacing: 0.5,
                            ),
                          ),
                          const SizedBox(height: 6),
                          Wrap(
                            spacing: 5,
                            runSpacing: 5,
                            children: currentUser!.permissions.map((p) {
                              return Container(
                                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                decoration: BoxDecoration(
                                  color: Colors.black.withOpacity(0.3),
                                  borderRadius: BorderRadius.circular(6),
                                  border: Border.all(color: Colors.white.withOpacity(0.12)),
                                ),
                                child: Text(
                                  p,
                                  style: const TextStyle(
                                    fontSize: 10.5,
                                    fontFamily: 'monospace',
                                    fontWeight: FontWeight.w600,
                                    color: Color(0xFFE2E8F0),
                                  ),
                                ),
                              );
                            }).toList(),
                          ),
                        ],
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            // Language Settings Section
            Text(
              l10n.language,
              style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Card(
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
              child: Column(
                children: [
                  RadioListTile<String>(
                    title: const Text('한국어 (Korean)'),
                    value: 'ko',
                    groupValue: currentLocale.languageCode,
                    onChanged: (val) {
                      if (val != null) {
                        ref.read(localeProvider.notifier).state = Locale(val);
                      }
                    },
                  ),
                  const Divider(height: 1),
                  RadioListTile<String>(
                    title: const Text('English'),
                    value: 'en',
                    groupValue: currentLocale.languageCode,
                    onChanged: (val) {
                      if (val != null) {
                        ref.read(localeProvider.notifier).state = Locale(val);
                      }
                    },
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            // Timezone Settings Section
            Text(
              l10n.timezone,
              style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Card(
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                child: DropdownButtonHideUnderline(
                  child: DropdownButton<String>(
                    isExpanded: true,
                    value: _currentTimezone,
                    items: _timezones.map((tz) {
                      return DropdownMenuItem<String>(
                        value: tz['code'],
                        child: Text(
                          tz['label']!,
                          style: const TextStyle(fontSize: 14),
                        ),
                      );
                    }).toList(),
                    onChanged: (val) {
                      if (val != null) {
                        _changeTimezone(val);
                      }
                    },
                  ),
                ),
              ),
            ),
            const SizedBox(height: 32),

            // Logout Button
            SizedBox(
              width: double.infinity,
              height: 48,
              child: OutlinedButton.icon(
                style: OutlinedButton.styleFrom(
                  foregroundColor: Colors.red,
                  side: const BorderSide(color: Colors.red),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                ),
                icon: const Icon(Icons.logout),
                label: Text(
                  l10n.logout,
                  style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                ),
                onPressed: _handleLogout,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
