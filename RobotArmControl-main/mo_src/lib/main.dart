import 'package:flutter/material.dart';

import 'constants/app_theme.dart';
import 'models/session_identity.dart';
import 'screens/admin_main_screen.dart';
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
  late Future<SessionIdentity?> _sessionCheckFuture;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _sessionCheckFuture = _restoreSessionIdentity();
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

    setState(() {
      _sessionCheckFuture = isValid
          ? SessionService.getSessionIdentity()
          : Future.value(null);
    });
  }

  Future<SessionIdentity?> _restoreSessionIdentity() async {
    final hasValidSession = await SessionService.restoreSessionIfValid();
    if (!hasValidSession) {
      return null;
    }

    return SessionService.getSessionIdentity();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: AppTheme.dark(),
      darkTheme: AppTheme.dark(),
      themeMode: ThemeMode.dark,
      home: FutureBuilder<SessionIdentity?>(
        future: _sessionCheckFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const Scaffold(
              body: Center(child: CircularProgressIndicator()),
            );
          }

          final session = snapshot.data;
          if (session == null) {
            return const ApiLoginScreen();
          }
          return session.isAdmin ? const AdminMainScreen() : const MainScreen();
        },
      ),
    );
  }
}
