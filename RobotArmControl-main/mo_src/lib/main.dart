import 'package:flutter/material.dart';

import 'constants/app_theme.dart';
import 'screens/api_login_screen.dart';
import 'screens/main_screen.dart';
import 'services/session_service.dart';

void main() => runApp(const RobotControlApp());

class RobotControlApp extends StatefulWidget {
  const RobotControlApp({super.key});

  @override
  State<RobotControlApp> createState() => _RobotControlAppState();
}

class _RobotControlAppState extends State<RobotControlApp>
    with WidgetsBindingObserver {
  late Future<bool> _sessionCheckFuture;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _sessionCheckFuture = SessionService.restoreSessionIfValid();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _revalidateSessionOnResume();
    }
  }

  Future<void> _revalidateSessionOnResume() async {
    final isValid = await SessionService.refreshOrExpireSession();
    if (!mounted) {
      return;
    }

    if (!isValid) {
      setState(() {
        _sessionCheckFuture = Future.value(false);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light(),
      home: FutureBuilder<bool>(
        future: _sessionCheckFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const Scaffold(
              body: Center(child: CircularProgressIndicator()),
            );
          }

          final hasValidSession = snapshot.data == true;
          return hasValidSession ? const MainScreen() : const ApiLoginScreen();
        },
      ),
    );
  }
}
