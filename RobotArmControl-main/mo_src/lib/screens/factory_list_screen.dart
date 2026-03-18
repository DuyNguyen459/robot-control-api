import 'package:flutter/material.dart';

import '../constants/app_navigation.dart';
import '../constants/app_spacing.dart';
import '../models/factory.dart';
import '../services/api_service.dart';
import '../services/session_service.dart';
import '../widgets/app_entity_tile.dart';
import '../widgets/app_shell.dart';
import '../widgets/app_states.dart';
import 'area_list_screen.dart';
import 'api_login_screen.dart';

class FactoryListScreen extends StatefulWidget {
  const FactoryListScreen({super.key});

  @override
  State<FactoryListScreen> createState() => _FactoryListScreenState();
}

class _FactoryListScreenState extends State<FactoryListScreen> {
  final ApiService _apiService = ApiService();
  late Future<List<Factory>> _factoriesFuture;

  Future<List<Factory>> _loadAssignedFactoryOnly() async {
    final identity = await SessionService.getSessionIdentity();
    final allFactories = await _apiService.fetchFactories();

    final assignedFactoryId = identity?.user.factoryId;
    if (assignedFactoryId == null) {
      return const [];
    }

    return allFactories
        .where((factory) => factory.id == assignedFactoryId.toString())
        .toList();
  }

  @override
  void initState() {
    super.initState();
    _factoriesFuture = _loadAssignedFactoryOnly();
  }

  void _reloadFactories() {
    setState(() {
      _factoriesFuture = _loadAssignedFactoryOnly();
    });
  }

  Future<void> _openLoginScreenAndReload() async {
    final bool? isLoggedIn = await Navigator.push<bool>(
      context,
      appRoute(const ApiLoginScreen()),
    );

    if (isLoggedIn == true) {
      _reloadFactories();
    }
  }

  @override
  Widget build(BuildContext context) {
    return AppShell(
      title: 'Select Factory',
      currentTabIndex: 1,
      actions: [
        IconButton(
          tooltip: 'Sign in again',
          onPressed: _openLoginScreenAndReload,
          icon: const Icon(Icons.login),
        ),
      ],
      child: FutureBuilder<List<Factory>>(
        future: _factoriesFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const AppLoadingState();
          }

          if (snapshot.hasError) {
            final isAuthError = snapshot.error is ApiAuthException;
            return AppErrorState(
              title: 'Unable to load factories',
              message: '${snapshot.error}',
              actionLabel: isAuthError ? 'Sign in' : 'Retry',
              onAction: isAuthError
                  ? _openLoginScreenAndReload
                  : _reloadFactories,
            );
          }

          final factories = snapshot.data ?? [];
          if (factories.isEmpty) {
            return const AppEmptyState(
              message: 'No assigned factory found for this user.',
            );
          }

          return RefreshIndicator(
            onRefresh: () async => _reloadFactories(),
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
              itemCount: factories.length,
              separatorBuilder: (context, index) =>
                  const SizedBox(height: AppSpacing.sm),
              itemBuilder: (context, index) {
                final factory = factories[index];
                return AppEntityTile(
                  icon: Icons.domain,
                  title: factory.name,
                  subtitle: factory.location,
                  status: factory.status,
                  onTap: () => Navigator.push(
                    context,
                    appRoute(AreaListScreen(factory: factory)),
                  ),
                );
              },
            ),
          );
        },
      ),
    );
  }
}
