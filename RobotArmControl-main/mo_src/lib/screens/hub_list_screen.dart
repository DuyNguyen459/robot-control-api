import 'package:flutter/material.dart';

import '../constants/app_navigation.dart';
import '../constants/app_spacing.dart';
import '../models/area.dart';
import '../models/hub.dart';
import '../services/api_service.dart';
import '../widgets/app_entity_tile.dart';
import '../widgets/app_shell.dart';
import '../widgets/app_states.dart';
import 'api_login_screen.dart';
import 'device_list_screen.dart';

class HubListScreen extends StatefulWidget {
  final Area area;
  final String factoryName;

  const HubListScreen({
    super.key,
    required this.area,
    required this.factoryName,
  });

  @override
  State<HubListScreen> createState() => _HubListScreenState();
}

class _HubListScreenState extends State<HubListScreen> {
  final ApiService _apiService = ApiService();
  late Future<List<Hub>> _hubsFuture;

  @override
  void initState() {
    super.initState();
    _hubsFuture = _apiService.fetchHubsByArea(widget.area.id);
  }

  void _reloadHubs() {
    setState(() {
      _hubsFuture = _apiService.fetchHubsByArea(widget.area.id);
    });
  }

  Future<void> _openLoginScreenAndReload() async {
    final bool? isLoggedIn = await Navigator.push<bool>(
      context,
      appRoute(const ApiLoginScreen()),
    );

    if (isLoggedIn == true) {
      _reloadHubs();
    }
  }

  @override
  Widget build(BuildContext context) {
    return AppShell(
      title: 'Hubs • ${widget.area.name}',
      currentTabIndex: 1,
      actions: [
        IconButton(
          tooltip: 'Sign in again',
          onPressed: _openLoginScreenAndReload,
          icon: const Icon(Icons.login),
        ),
      ],
      child: FutureBuilder<List<Hub>>(
        future: _hubsFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const AppLoadingState();
          }

          if (snapshot.hasError) {
            final isAuthError = snapshot.error is ApiAuthException;
            return AppErrorState(
              title: 'Unable to load hubs',
              message: '${snapshot.error}',
              actionLabel: isAuthError ? 'Sign in' : 'Retry',
              onAction: isAuthError ? _openLoginScreenAndReload : _reloadHubs,
            );
          }

          final hubs = snapshot.data ?? [];
          if (hubs.isEmpty) {
            return const AppEmptyState(message: 'No hubs available.');
          }

          return RefreshIndicator(
            onRefresh: () async => _reloadHubs(),
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
              itemCount: hubs.length,
              separatorBuilder: (context, index) =>
                  const SizedBox(height: AppSpacing.sm),
              itemBuilder: (context, index) {
                final hub = hubs[index];
                final subtitleParts = <String>[
                  if (hub.description.isNotEmpty) hub.description,
                ];

                return AppEntityTile(
                  icon: Icons.hub,
                  title: hub.name,
                  subtitle: subtitleParts.join(' • '),
                  status: hub.status,
                  onTap: () => Navigator.push(
                    context,
                    appRoute(
                      DeviceListScreen(
                        factoryName: widget.factoryName,
                        areaName: widget.area.name,
                        hub: hub,
                      ),
                    ),
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
