import 'package:flutter/material.dart';

import '../constants/app_colors.dart';
import '../constants/app_spacing.dart';
import '../screens/main_screen.dart';
import 'app_navigation_drawer.dart';

class AppShell extends StatelessWidget {
  final String? title;
  final List<Widget>? actions;
  final Widget child;
  final bool useSafeArea;
  final int currentTabIndex;

  const AppShell({
    super.key,
    this.title,
    this.actions,
    required this.child,
    this.useSafeArea = true,
    this.currentTabIndex = 0,
  });

  @override
  Widget build(BuildContext context) {
    final body = Container(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [Color(0xFF101010), Color(0xFF191919)],
        ),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: AppSpacing.md),
        child: child,
      ),
    );

    return Scaffold(
      drawer: AppNavigationDrawer.user(
        currentIndex: currentTabIndex,
        currentTitle: title,
      ),
      appBar: title == null
          ? null
          : AppBar(
              title: Text(title!),
              actions: actions,
              bottom: PreferredSize(
                preferredSize: const Size.fromHeight(1),
                child: Container(height: 1, color: AppColors.line),
              ),
            ),
      body: useSafeArea ? SafeArea(child: body) : body,
      bottomNavigationBar: Container(
        decoration: const BoxDecoration(
          border: Border(top: BorderSide(color: AppColors.line)),
        ),
        child: BottomNavigationBar(
          currentIndex: currentTabIndex,
          onTap: (i) {
            if (i == currentTabIndex) {
              return;
            }

            Navigator.of(context).pushAndRemoveUntil(
              MaterialPageRoute(builder: (_) => MainScreen(initialIndex: i)),
              (route) => false,
            );
          },
          items: const [
            BottomNavigationBarItem(icon: Icon(Icons.memory), label: 'Devices'),
            BottomNavigationBarItem(
              icon: Icon(Icons.factory_outlined),
              label: 'Factory',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.videogame_asset),
              label: 'Control',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.history),
              label: 'Robot Log',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.person_outline),
              label: 'Profile',
            ),
          ],
        ),
      ),
    );
  }
}
