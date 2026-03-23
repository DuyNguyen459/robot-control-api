import 'package:flutter/material.dart';

import '../constants/app_navigation.dart';
import '../constants/app_colors.dart';
import '../constants/app_spacing.dart';
import '../models/device.dart';
import '../services/api_service.dart';
import '../widgets/app_shell.dart';
import '../widgets/app_states.dart';
import 'api_login_screen.dart';
import 'device_realtime_log_screen.dart';
import 'main_screen.dart';

class DeviceScreen extends StatefulWidget {
  const DeviceScreen({super.key});

  @override
  State<DeviceScreen> createState() => _DeviceScreenState();
}

class _DeviceScreenState extends State<DeviceScreen> {
  final ApiService _apiService = ApiService();
  late Future<List<_DeviceRecord>> _devicesFuture;

  int? _parseRobotId(String rawDeviceId) {
    final value = rawDeviceId.trim();
    if (value.isEmpty) {
      return null;
    }
    final direct = int.tryParse(value);
    if (direct != null) {
      return direct;
    }

    final match = RegExp(r'\d+').firstMatch(value);
    if (match == null) {
      return null;
    }
    return int.tryParse(match.group(0)!);
  }

  @override
  void initState() {
    super.initState();
    _devicesFuture = _loadAllDevices();
  }

  Future<List<_DeviceRecord>> _loadAllDevices() async {
    final stopwatch = Stopwatch()..start();
    final factories = await _apiService.fetchFactories();
    final nestedRecords = await Future.wait(
      factories.map((factory) async {
        final areas = await _apiService.fetchAreasByFactory(factory.id);

        final areaRecords = await Future.wait(
          areas.map((area) async {
            final hubs = await _apiService.fetchHubsByArea(area.id);

            final hubRecords = await Future.wait(
              hubs.map((hub) async {
                final devices = await _apiService.fetchDevicesByHub(hub.id);

                return devices
                    .map(
                      (device) => _DeviceRecord(
                        device: device,
                        factoryName: factory.name,
                        areaName: area.name,
                        hubName: hub.name,
                      ),
                    )
                    .toList(growable: false);
              }),
            );

            return hubRecords.expand((items) => items).toList(growable: false);
          }),
        );

        return areaRecords.expand((items) => items).toList(growable: false);
      }),
    );

    final records = nestedRecords
        .expand((items) => items)
        .toList(growable: false);

    records.sort(
      (a, b) =>
          a.device.name.toLowerCase().compareTo(b.device.name.toLowerCase()),
    );

    stopwatch.stop();
    debugPrint(
      'DeviceScreen loaded ${records.length} devices in ${stopwatch.elapsedMilliseconds}ms',
    );

    return records;
  }

  void _reloadDevices() {
    setState(() {
      _devicesFuture = _loadAllDevices();
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
      title: 'Device Screen',
      currentTabIndex: 0,
      actions: [
        IconButton(
          tooltip: 'Refresh',
          onPressed: _reloadDevices,
          icon: const Icon(Icons.refresh),
        ),
      ],
      child: FutureBuilder<List<_DeviceRecord>>(
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
            return const AppEmptyState(
              message: 'No devices found in current data scope.',
            );
          }

          return RefreshIndicator(
            onRefresh: () async => _reloadDevices(),
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
              itemCount: devices.length,
              separatorBuilder: (context, index) =>
                  const SizedBox(height: AppSpacing.sm),
              itemBuilder: (context, index) {
                final item = devices[index];
                final device = item.device;
                final actionButtonStyle = OutlinedButton.styleFrom(
                  minimumSize: const Size.fromHeight(44),
                );

                final subtitleParts = <String>[
                  if (device.deviceType.isNotEmpty) device.deviceType,
                  if (device.robotType.isNotEmpty) device.robotType,
                  if (device.model.isNotEmpty) device.model,
                  item.factoryName,
                  item.areaName,
                  item.hubName,
                ];
                final robotId = _parseRobotId(device.id);

                return Card(
                  child: Padding(
                    padding: const EdgeInsets.all(AppSpacing.md),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Container(
                              width: 42,
                              height: 42,
                              decoration: BoxDecoration(
                                color: AppColors.mist,
                                borderRadius: BorderRadius.circular(12),
                                border: Border.all(color: AppColors.line),
                              ),
                              child: const Icon(
                                Icons.memory,
                                color: AppColors.ink,
                                size: 20,
                              ),
                            ),
                            const SizedBox(width: AppSpacing.md),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    device.name,
                                    style: Theme.of(
                                      context,
                                    ).textTheme.titleMedium,
                                  ),
                                  const SizedBox(height: 2),
                                  Text(
                                    subtitleParts.join(' • '),
                                    style: Theme.of(
                                      context,
                                    ).textTheme.bodySmall,
                                  ),
                                  const SizedBox(height: 2),
                                  Text(
                                    'Status: ${device.status}',
                                    style: Theme.of(
                                      context,
                                    ).textTheme.labelMedium,
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: AppSpacing.md),
                        Row(
                          children: [
                            Expanded(
                              child: OutlinedButton.icon(
                                style: actionButtonStyle,
                                onPressed: () {
                                  if (robotId == null) {
                                    ScaffoldMessenger.of(context).showSnackBar(
                                      const SnackBar(
                                        content: Text(
                                          'Robot ID is invalid for this device. Pull down to refresh devices.',
                                        ),
                                      ),
                                    );
                                    return;
                                  }

                                  Navigator.of(context).push(
                                    appRoute(
                                      DeviceRealtimeLogScreen(
                                        robotId: robotId,
                                        deviceName: device.name,
                                      ),
                                    ),
                                  );
                                },
                                icon: const Icon(Icons.history),
                                label: const Text('Xem log'),
                              ),
                            ),
                            const SizedBox(width: AppSpacing.sm),
                            Expanded(
                              child: OutlinedButton.icon(
                                style: actionButtonStyle,
                                onPressed: () {
                                  Navigator.of(context).pushAndRemoveUntil(
                                    appRoute(const MainScreen(initialIndex: 2)),
                                    (route) => false,
                                  );
                                },
                                icon: const Icon(Icons.videogame_asset),
                                label: const Text('Điều khiển'),
                              ),
                            ),
                          ],
                        ),
                      ],
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

class _DeviceRecord {
  final Device device;
  final String factoryName;
  final String areaName;
  final String hubName;

  const _DeviceRecord({
    required this.device,
    required this.factoryName,
    required this.areaName,
    required this.hubName,
  });
}
