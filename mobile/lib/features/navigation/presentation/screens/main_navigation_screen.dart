import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/features/approvals/presentation/screens/approvals_list_screen.dart';
import 'package:mplatform_mobile/features/chat/presentation/screens/chat_room_list_screen.dart';
import 'package:mplatform_mobile/features/home/presentation/screens/home_dashboard_screen.dart';
import 'package:mplatform_mobile/features/notifications/presentation/screens/notifications_screen.dart';
import 'package:mplatform_mobile/features/records/presentation/screens/records_list_screen.dart';

class MainNavigationScreen extends ConsumerStatefulWidget {
  const MainNavigationScreen({super.key});

  @override
  ConsumerState<MainNavigationScreen> createState() => _MainNavigationScreenState();
}

class _MainNavigationScreenState extends ConsumerState<MainNavigationScreen> {
  int _currentIndex = 2; // Default to Home Dashboard (index 2)

  @override
  void initState() {
    super.initState();
    _currentIndex = 2;
  }

  List<Widget> get _screens => const [
    RecordsListScreen(),
    ApprovalsListScreen(),
    HomeDashboardScreen(),
    NotificationsScreen(),
    ChatRoomListScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    // 규칙: 하드코딩 금지, vue-i18n에서 변환된 AppLocalizations 기반의 동적 라벨 사용
    return Scaffold(
      body: IndexedStack(
        index: _currentIndex,
        children: _screens,
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (index) {
          setState(() {
            _currentIndex = index;
          });
        },
        selectedItemColor: Colors.deepPurple,
        unselectedItemColor: Colors.grey[600],
        selectedLabelStyle: const TextStyle(fontWeight: FontWeight.w600, fontSize: 12),
        unselectedLabelStyle: const TextStyle(fontWeight: FontWeight.w500, fontSize: 11),
        type: BottomNavigationBarType.fixed,
        elevation: 10,
        items: [
          BottomNavigationBarItem(
            icon: const Icon(Icons.table_chart_outlined),
            activeIcon: const Icon(Icons.table_chart),
            label: l10n.navTabRecords,
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.playlist_add_check_outlined),
            activeIcon: const Icon(Icons.playlist_add_check),
            label: l10n.navTabApprovals,
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.home_outlined),
            activeIcon: const Icon(Icons.home),
            label: l10n.navTabHome,
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.notifications_outlined),
            activeIcon: const Icon(Icons.notifications),
            label: l10n.navTabNotifications,
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.chat_bubble_outline),
            activeIcon: const Icon(Icons.chat_bubble),
            label: l10n.navTabChat,
          ),
        ],
      ),
    );
  }
}
