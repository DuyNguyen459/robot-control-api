import 'package:flutter/material.dart';

import '../constants/app_navigation.dart';
import '../constants/app_spacing.dart';
import '../models/area.dart';
import '../models/factory.dart';
import '../services/api_service.dart';
import '../widgets/app_entity_tile.dart';
import '../widgets/app_shell.dart';
import '../widgets/app_states.dart';
import 'api_login_screen.dart';
import 'hub_list_screen.dart';

class AreaListScreen extends StatefulWidget {
  final Factory factory;

  const AreaListScreen({super.key, required this.factory});

  @override
  State<AreaListScreen> createState() => _AreaListScreenState();
}

class _AreaListScreenState extends State<AreaListScreen> {
  final ApiService _apiService = ApiService();
  late Future<List<Area>> _areasFuture;

  @override
  void initState() {
    super.initState();
    _areasFuture = _apiService.fetchAreasByFactory(widget.factory.id);
  }

  void _reloadAreas() {
    setState(() {
      _areasFuture = _apiService.fetchAreasByFactory(widget.factory.id);
    });
  }

  Future<void> _openLoginScreenAndReload() async {
    final bool? isLoggedIn = await Navigator.push<bool>(
      context,
      appRoute(const ApiLoginScreen()),
    );

    if (isLoggedIn == true) {
      _reloadAreas();
    }
  }

  @override
  Widget build(BuildContext context) {
    return AppShell(
      title: 'Areas • ${widget.factory.name}',
      currentTabIndex: 1,
      actions: [
        IconButton(
          tooltip: 'Sign in again',
          onPressed: _openLoginScreenAndReload,
          icon: const Icon(Icons.login),
        ),
      ],
      child: FutureBuilder<List<Area>>(
        future: _areasFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const AppLoadingState();
          }

          if (snapshot.hasError) {
            final isAuthError = snapshot.error is ApiAuthException;
            return AppErrorState(
              title: 'Unable to load areas',
              message: '${snapshot.error}',
              actionLabel: isAuthError ? 'Sign in' : 'Retry',
              onAction: isAuthError ? _openLoginScreenAndReload : _reloadAreas,
            );
          }

          final areas = snapshot.data ?? [];
          if (areas.isEmpty) {
            return const AppEmptyState(message: 'No areas available.');
          }

          return RefreshIndicator(
            onRefresh: () async => _reloadAreas(),
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
              itemCount: areas.length,
              separatorBuilder: (context, index) =>
                  const SizedBox(height: AppSpacing.sm),
              itemBuilder: (context, index) {
                final area = areas[index];
                final subtitleParts = <String>[
                  if (area.description.isNotEmpty) area.description,
                ];

                return AppEntityTile(
                  icon: Icons.location_on,
                  title: area.name,
                  subtitle: subtitleParts.join(' • '),
                  status: area.status,
                  onTap: () => Navigator.push(
                    context,
                    appRoute(
                      HubListScreen(
                        factoryName: widget.factory.name,
                        area: area,
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
