import 'package:flutter/material.dart';

import '../constants/app_navigation.dart';
import '../constants/app_spacing.dart';
import '../models/device.dart';
import '../models/hub.dart';
import '../services/api_service.dart';
import '../widgets/app_entity_tile.dart';
import '../widgets/app_shell.dart';
import '../widgets/app_states.dart';
import 'api_login_screen.dart';
import 'main_screen.dart';

class DeviceListScreen extends StatefulWidget {
  final Hub hub;
  final String factoryName;
  final String areaName;

  const DeviceListScreen({
    super.key,
    required this.hub,
    required this.factoryName,
    required this.areaName,
  });

  @override
  State<DeviceListScreen> createState() => _DeviceListScreenState();
}

class _DeviceListScreenState extends State<DeviceListScreen> {
  final ApiService _apiService = ApiService();
  late Future<List<Device>> _devicesFuture;

  @override
  void initState() {
    super.initState();
    _devicesFuture = _apiService.fetchDevicesByHub(widget.hub.id);
  }

  void _reloadDevices() {
    setState(() {
      _devicesFuture = _apiService.fetchDevicesByHub(widget.hub.id);
    });
  }

  Future<void> _openLoginScreenAndReload() async {
    final bool? isLoggedIn = await Navigator.push<bool>(
      context,
      appRoute(const ApiLoginScreen()),
    );

    if (isLoggedIn == true) {
      _reloadDevices();
    }
  }

  @override
  Widget build(BuildContext context) {
    return AppShell(
      title: 'Devices • ${widget.hub.name}',
      currentTabIndex: 1,
      actions: [
        IconButton(
          tooltip: 'Sign in again',
          onPressed: _openLoginScreenAndReload,
          icon: const Icon(Icons.login),
        ),
      ],
      child: FutureBuilder<List<Device>>(
        future: _devicesFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const AppLoadingState();
          }

          if (snapshot.hasError) {
            final isAuthError = snapshot.error is ApiAuthException;
            return AppErrorState(
              title: 'Unable to load devices',
              message: '${snapshot.error}',
              actionLabel: isAuthError ? 'Sign in' : 'Retry',
              onAction: isAuthError
                  ? _openLoginScreenAndReload
                  : _reloadDevices,
            );
          }

          final devices = snapshot.data ?? [];
          if (devices.isEmpty) {
            return const AppEmptyState(message: 'No devices available.');
          }

          return RefreshIndicator(
            onRefresh: () async => _reloadDevices(),
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
              itemCount: devices.length,
              separatorBuilder: (context, index) =>
                  const SizedBox(height: AppSpacing.sm),
              itemBuilder: (context, index) {
                final device = devices[index];
                final subtitleParts = <String>[
                  if (device.deviceType.isNotEmpty) device.deviceType,
                  if (device.robotType.isNotEmpty) device.robotType,
                  if (device.model.isNotEmpty) device.model,
                ];

                return AppEntityTile(
                  icon: Icons.precision_manufacturing,
                  title: device.name,
                  subtitle: subtitleParts.join(' • '),
                  status: device.status,
                  onTap: () {
                    Navigator.of(context).pushAndRemoveUntil(
                      appRoute(const MainScreen(initialIndex: 2)),
                      (route) => false,
                    );
                  },
                );
              },
            ),
          );
        },
      ),
    );
  }
}
