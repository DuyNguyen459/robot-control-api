import 'package:flutter/material.dart';

import '../constants/app_navigation.dart';
import '../constants/app_spacing.dart';
import '../models/session_identity.dart';
import '../services/api_service.dart';
import '../services/session_service.dart';
import '../screens/api_login_screen.dart';

class AppNavigationDrawer extends StatelessWidget {
  const AppNavigationDrawer.user({
    super.key,
    required this.currentIndex,
    this.currentTitle,
  }) : isAdmin = false;

  const AppNavigationDrawer.admin({
    super.key,
    required this.currentIndex,
    this.currentTitle,
  }) : isAdmin = true;

  final bool isAdmin;
  final int currentIndex;
  final String? currentTitle;

  Future<_DrawerIdentityData?> _loadDrawerIdentity() async {
    final identity = await SessionService.getSessionIdentity();
    if (identity == null) {
      return null;
    }

    String factoryName = '--';
    final factoryId = identity.user.factoryId;
    if (factoryId != null) {
      try {
        final factories = await ApiService().fetchFactories();
        for (final factory in factories) {
          if (factory.id == factoryId.toString()) {
            factoryName = factory.name;
            break;
          }
        }
      } catch (_) {
        // Keep drawer available even if the lookup fails.
      }
    }

    return _DrawerIdentityData(identity: identity, factoryName: factoryName);
  }

  Future<void> _goToLogin(BuildContext context) async {
    await SessionService.clearSession();
    if (!context.mounted) {
      return;
    }

    Navigator.of(
      context,
    ).pushAndRemoveUntil(appRoute(const ApiLoginScreen()), (route) => false);
  }

  Future<void> _confirmSignOut(BuildContext context) async {
    final shouldSignOut = await showDialog<bool>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Logout'),
          content: const Text('Do you want to logout now?'),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () => Navigator.pop(context, true),
              child: const Text('Logout'),
            ),
          ],
        );
      },
    );

    if (shouldSignOut == true) {
      if (!context.mounted) {
        return;
      }
      await _goToLogin(context);
    }
  }

  @override
  Widget build(BuildContext context) {
    final title = currentTitle ?? (isAdmin ? 'Admin Menu' : 'Navigation');

    return Drawer(
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.md),
          child: Column(
            children: [
              FutureBuilder<_DrawerIdentityData?>(
                future: _loadDrawerIdentity(),
                builder: (context, snapshot) {
                  final info = snapshot.data;
                  final name = info?.identity.user.username ?? title;
                  final email = info?.identity.user.email ?? '--';
                  final factory = info?.factoryName ?? '--';

                  return Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(AppSpacing.md),
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(color: Theme.of(context).dividerColor),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Container(
                              width: 44,
                              height: 44,
                              decoration: BoxDecoration(
                                borderRadius: BorderRadius.circular(12),
                                border: Border.all(
                                  color: Theme.of(context).dividerColor,
                                ),
                              ),
                              child: const Icon(Icons.phone_android),
                            ),
                            const SizedBox(width: AppSpacing.sm),
                            Expanded(
                              child: Text(
                                'Mobile Control',
                                style: Theme.of(context).textTheme.titleMedium,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: AppSpacing.sm),
                        Text(
                          name,
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                        const SizedBox(height: 2),
                        Text(
                          email,
                          style: Theme.of(context).textTheme.bodySmall,
                        ),
                        const SizedBox(height: 2),
                        Text(
                          'Factory: $factory',
                          style: Theme.of(context).textTheme.bodySmall,
                        ),
                      ],
                    ),
                  );
                },
              ),
              const SizedBox(height: AppSpacing.md),
              const Spacer(),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed: () => _confirmSignOut(context),
                  icon: const Icon(Icons.logout),
                  label: const Text('Logout'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _DrawerIdentityData {
  final SessionIdentity identity;
  final String factoryName;

  const _DrawerIdentityData({
    required this.identity,
    required this.factoryName,
  });
}
