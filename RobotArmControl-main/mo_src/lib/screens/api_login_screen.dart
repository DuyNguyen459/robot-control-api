import 'package:flutter/material.dart';

import '../constants/app_colors.dart';
import '../constants/app_navigation.dart';
import '../constants/app_spacing.dart';
import '../services/api_service.dart';
import 'admin_main_screen.dart';
import 'main_screen.dart';

class ApiLoginScreen extends StatefulWidget {
  const ApiLoginScreen({super.key});

  @override
  State<ApiLoginScreen> createState() => _ApiLoginScreenState();
}

class _ApiLoginScreenState extends State<ApiLoginScreen> {
  final ApiService _apiService = ApiService();
  final TextEditingController _usernameController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  bool _isSubmitting = false;

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  void _showTopMessage(String message) {
    final messenger = ScaffoldMessenger.of(context);
    final mediaQuery = MediaQuery.of(context);
    final topInset = mediaQuery.padding.top;
    final screenHeight = mediaQuery.size.height;
    final bottomMargin = (screenHeight - topInset - 120).clamp(
      0.0,
      screenHeight,
    );

    messenger
      ..hideCurrentSnackBar()
      ..showSnackBar(
        SnackBar(
          behavior: SnackBarBehavior.floating,
          margin: EdgeInsets.fromLTRB(
            AppSpacing.lg,
            topInset + AppSpacing.md,
            AppSpacing.lg,
            bottomMargin,
          ),
          content: Text(message),
        ),
      );
  }

  String _mapErrorMessage(Object error) {
    final message = error.toString();
    final normalized = message.toLowerCase();

    if (normalized.contains('timed out') ||
        normalized.contains('unable to connect to backend') ||
        normalized.contains('socketexception') ||
        normalized.contains('failed host lookup') ||
        normalized.contains('connection refused')) {
      return 'Server có lỗi. Vui lòng thử lại sau.';
    }

    return message;
  }

  Future<void> _handleLogin() async {
    final username = _usernameController.text.trim();
    final password = _passwordController.text;

    if (username.isEmpty || password.isEmpty) {
      _showTopMessage('Please enter username and password.');
      return;
    }

    setState(() {
      _isSubmitting = true;
    });

    try {
      final user = await _apiService.login(
        username: username,
        password: password,
      );
      if (!mounted) {
        return;
      }

      if (user.isAdmin) {
        Navigator.of(context).pushAndRemoveUntil(
          appRoute(const AdminMainScreen()),
          (route) => false,
        );
      } else {
        Navigator.of(
          context,
        ).pushAndRemoveUntil(appRoute(const MainScreen()), (route) => false);
      }
    } on ApiAuthException catch (e) {
      if (!mounted) {
        return;
      }
      _showTopMessage(_mapErrorMessage(e));
    } catch (e) {
      if (!mounted) {
        return;
      }
      _showTopMessage(_mapErrorMessage(e));
    } finally {
      if (mounted) {
        setState(() {
          _isSubmitting = false;
        });
      }
    }
  }

  Future<void> _handleForgotPassword() async {
    if (!mounted) {
      return;
    }

    await showDialog<void>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Forgot Password'),
          content: const Text(
            'Password reset is not available in-app yet. Please contact your administrator to reset your account password.',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('OK'),
            ),
          ],
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Color(0xFF101010), Color(0xFF1B1B1B)],
          ),
        ),
        child: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(AppSpacing.lg),
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 460),
                child: Card(
                  child: Padding(
                    padding: const EdgeInsets.all(AppSpacing.lg),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        const Text(
                          'Robot Control',
                          style: TextStyle(
                            fontSize: 28,
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                        const SizedBox(height: AppSpacing.xs),
                        Text(
                          'Sign in to manage factory, area, hub and devices.',
                          style: textTheme.bodySmall,
                        ),
                        const SizedBox(height: AppSpacing.lg),
                        TextField(
                          controller: _usernameController,
                          textInputAction: TextInputAction.next,
                          decoration: const InputDecoration(
                            labelText: 'Username',
                            prefixIcon: Icon(Icons.person_outline),
                          ),
                        ),
                        const SizedBox(height: AppSpacing.md),
                        TextField(
                          controller: _passwordController,
                          obscureText: true,
                          onSubmitted: (_) {
                            if (!_isSubmitting) {
                              _handleLogin();
                            }
                          },
                          decoration: const InputDecoration(
                            labelText: 'Password',
                            prefixIcon: Icon(Icons.lock_outline),
                          ),
                        ),
                        const SizedBox(height: AppSpacing.sm),
                        Align(
                          alignment: Alignment.centerRight,
                          child: TextButton(
                            onPressed: _isSubmitting
                                ? null
                                : _handleForgotPassword,
                            child: const Text('Forgot password?'),
                          ),
                        ),
                        const SizedBox(height: AppSpacing.lg),
                        ElevatedButton(
                          onPressed: _isSubmitting ? null : _handleLogin,
                          child: _isSubmitting
                              ? const SizedBox(
                                  width: 18,
                                  height: 18,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                    color: AppColors.paper,
                                  ),
                                )
                              : const Text('Sign In'),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
