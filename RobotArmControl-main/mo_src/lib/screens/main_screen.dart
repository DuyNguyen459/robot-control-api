// lib/screens/main_screen.dart
import 'package:flutter/material.dart';

import 'device_screen.dart';
import 'factory_list_screen.dart';
import 'control_screen.dart';
import 'push_messages_screen.dart';
import 'profile_screen.dart';

class MainScreen extends StatefulWidget {
  final int initialIndex;
  const MainScreen({super.key, this.initialIndex = 0});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  Widget _resolvePage() {
    switch (widget.initialIndex) {
      case 0:
        return const DeviceScreen();
      case 1:
        return const FactoryListScreen();
      case 2:
        return const ControlScreen();
      case 3:
        return const RobotLogHistoryScreen();
      case 4:
      default:
        return const ProfileScreen(showBackButton: false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return _resolvePage();
  }
}
