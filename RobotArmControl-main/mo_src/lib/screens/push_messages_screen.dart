import 'package:flutter/material.dart';

import '../constants/app_colors.dart';
import '../constants/app_navigation.dart';
import '../constants/app_spacing.dart';
import '../models/device.dart';
import '../models/robot_alert.dart';
import '../models/robot_telemetry.dart';
import '../services/api_service.dart';
import '../services/session_service.dart';
import '../widgets/app_shell.dart';
import '../widgets/app_states.dart';
import 'api_login_screen.dart';

class RobotLogHistoryScreen extends StatefulWidget {
  final int? initialRobotId;

  const RobotLogHistoryScreen({super.key, this.initialRobotId});

  @override
  State<RobotLogHistoryScreen> createState() => _RobotLogHistoryScreenState();
}

class _RobotLogHistoryScreenState extends State<RobotLogHistoryScreen> {
  final ApiService _apiService = ApiService();
  static const int _logLimit = 10;

  bool _loadingHierarchy = true;
  bool _loadingLogs = false;
  String? _hierarchyError;
  String? _logsError;

  DateTime _selectedDate = DateTime.now();
  List<_RobotOption> _robotOptions = const [];
  _RobotOption? _selectedRobot;
  List<_HistoryEntry> _entries = const [];
  int _alertCount = 0;
  int _telemetryCount = 0;
  _HistoryView _historyView = _HistoryView.all;

  @override
  void initState() {
    super.initState();
    _bootstrap();
  }

  Future<void> _bootstrap() async {
    setState(() {
      _loadingHierarchy = true;
      _hierarchyError = null;
    });

    try {
      final options = await _loadRobotOptions();
      _RobotOption? selected;

      if (widget.initialRobotId != null) {
        for (final option in options) {
          if (option.robotId == widget.initialRobotId) {
            selected = option;
            break;
          }
        }
      }

      selected ??= options.isNotEmpty ? options.first : null;

      if (!mounted) {
        return;
      }

      setState(() {
        _robotOptions = options;
        _selectedRobot = selected;
      });

      if (selected != null) {
        await _loadLogs();
      }
    } catch (e) {
      if (!mounted) {
        return;
      }

      setState(() {
        _hierarchyError = e.toString();
      });
    } finally {
      if (mounted) {
        setState(() {
          _loadingHierarchy = false;
        });
      }
    }
  }

  Future<List<_RobotOption>> _loadRobotOptions() async {
    final identity = await SessionService.getSessionIdentity();
    final assignedFactoryId = identity?.user.factoryId;

    var factories = await _apiService.fetchFactories();
    if (assignedFactoryId != null) {
      factories = factories
          .where((f) => f.id == assignedFactoryId.toString())
          .toList();
    }

    final options = <_RobotOption>[];

    for (final factory in factories) {
      final areas = await _apiService.fetchAreasByFactory(factory.id);

      for (final area in areas) {
        final hubs = await _apiService.fetchHubsByArea(area.id);

        for (final hub in hubs) {
          final devices = await _apiService.fetchDevicesByHub(hub.id);

          for (final device in devices) {
            final robotId = int.tryParse(device.id);
            if (robotId == null) {
              continue;
            }

            options.add(
              _RobotOption(
                robotId: robotId,
                factoryName: factory.name,
                areaName: area.name,
                hubName: hub.name,
                device: device,
              ),
            );
          }
        }
      }
    }

    options.sort(
      (a, b) => a.label.toLowerCase().compareTo(b.label.toLowerCase()),
    );
    return options;
  }

  Future<void> _openLoginScreenAndReload() async {
    final isLoggedIn = await Navigator.push<bool>(
      context,
      appRoute(const ApiLoginScreen()),
    );

    if (isLoggedIn == true) {
      await _bootstrap();
    }
  }

  Future<void> _pickDate() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2020),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );

    if (picked != null) {
      setState(() {
        _selectedDate = picked;
      });
    }
  }

  Future<void> _loadLogs() async {
    final selectedRobot = _selectedRobot;
    if (selectedRobot == null) {
      setState(() {
        _logsError = 'Please select a robot first.';
        _entries = const [];
      });
      return;
    }

    setState(() {
      _loadingLogs = true;
      _logsError = null;
    });

    try {
      final alerts = await _apiService.fetchRobotAlertsByDate(
        selectedRobot.robotId,
        date: _selectedDate,
        limit: _logLimit,
      );

      final telemetry = await _apiService.fetchTelemetryHistoryByDate(
        selectedRobot.robotId,
        date: _selectedDate,
        limit: _logLimit,
      );

      final entries = <_HistoryEntry>[
        ...alerts.map(_HistoryEntry.fromAlert),
        ...telemetry.map(_HistoryEntry.fromTelemetry),
      ];

      entries.sort((a, b) {
        final aTime = a.eventTime ?? DateTime.fromMillisecondsSinceEpoch(0);
        final bTime = b.eventTime ?? DateTime.fromMillisecondsSinceEpoch(0);
        return bTime.compareTo(aTime);
      });

      if (!mounted) {
        return;
      }

      setState(() {
        _alertCount = alerts.length;
        _telemetryCount = telemetry.length;
        _entries = entries.take(_logLimit).toList();
      });
    } catch (e) {
      if (!mounted) {
        return;
      }

      setState(() {
        _logsError = e.toString();
        _entries = const [];
        _alertCount = 0;
        _telemetryCount = 0;
      });
    } finally {
      if (mounted) {
        setState(() {
          _loadingLogs = false;
        });
      }
    }
  }

  Color _severityColor(String severity) {
    switch (severity.toUpperCase()) {
      case 'ERROR':
      case 'CRITICAL':
        return Colors.redAccent;
      case 'WARNING':
      case 'WARN':
        return AppColors.warning;
      case 'INFO':
      default:
        return AppColors.success;
    }
  }

  String _formatDateTime(DateTime? dt) {
    if (dt == null) {
      return '--';
    }

    final local = dt.toLocal();
    String two(int n) => n.toString().padLeft(2, '0');
    return '${two(local.day)}/${two(local.month)}/${local.year} ${two(local.hour)}:${two(local.minute)}:${two(local.second)}';
  }

  String _formatDateOnly(DateTime date) {
    final local = date.toLocal();
    final year = local.year.toString().padLeft(4, '0');
    final month = local.month.toString().padLeft(2, '0');
    final day = local.day.toString().padLeft(2, '0');
    return '$year-$month-$day';
  }

  Future<void> _showEntryDetails(_HistoryEntry entry) {
    return showDialog<void>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: Text('${entry.sourceLabel} Details'),
          content: SingleChildScrollView(child: SelectableText(entry.details)),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Close'),
            ),
          ],
        );
      },
    );
  }

  List<_HistoryEntry> get _visibleEntries {
    switch (_historyView) {
      case _HistoryView.alerts:
        return _entries
            .where((entry) => entry.source == _HistorySource.alert)
            .toList();
      case _HistoryView.telemetry:
        return _entries
            .where((entry) => entry.source == _HistorySource.telemetry)
            .toList();
      case _HistoryView.all:
        return _entries;
    }
  }

  @override
  Widget build(BuildContext context) {
    return AppShell(
      title: 'Robot Log History',
      currentTabIndex: 3,
      actions: [
        IconButton(
          tooltip: 'Reload robots',
          onPressed: _bootstrap,
          icon: const Icon(Icons.refresh),
        ),
      ],
      child: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_loadingHierarchy) {
      return const AppLoadingState();
    }

    if (_hierarchyError != null) {
      final isAuthError =
          _hierarchyError!.contains('Sign in is required') ||
          _hierarchyError!.contains('Token is invalid');
      return AppErrorState(
        title: 'Unable to load robot hierarchy',
        message: _hierarchyError!,
        actionLabel: isAuthError ? 'Sign in' : 'Retry',
        onAction: isAuthError ? _openLoginScreenAndReload : _bootstrap,
      );
    }

    if (_robotOptions.isEmpty) {
      return const AppEmptyState(
        message:
            'No robots available in your Factory -> Areas -> Hubs -> Devices hierarchy.',
      );
    }

    return RefreshIndicator(
      onRefresh: _bootstrap,
      child: ListView(
        padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(AppSpacing.md),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Query Logs By Robot And Date',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  DropdownButtonFormField<_RobotOption>(
                    initialValue: _selectedRobot,
                    items: _robotOptions
                        .map(
                          (option) => DropdownMenuItem<_RobotOption>(
                            value: option,
                            child: Text(
                              option.label,
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                        )
                        .toList(),
                    onChanged: (value) {
                      setState(() {
                        _selectedRobot = value;
                      });
                    },
                    decoration: const InputDecoration(labelText: 'Robot'),
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  InkWell(
                    onTap: _pickDate,
                    child: InputDecorator(
                      decoration: const InputDecoration(
                        labelText: 'Date',
                        suffixIcon: Icon(Icons.calendar_month),
                      ),
                      child: Text(_formatDateOnly(_selectedDate)),
                    ),
                  ),
                  const SizedBox(height: AppSpacing.md),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton.icon(
                      onPressed: _loadingLogs ? null : _loadLogs,
                      icon: const Icon(Icons.search),
                      label: Text(_loadingLogs ? 'Loading...' : 'Load Logs'),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.md),
          if (_logsError != null)
            Card(
              child: Padding(
                padding: const EdgeInsets.all(AppSpacing.md),
                child: Text(
                  _logsError!,
                  style: const TextStyle(color: Colors.redAccent),
                ),
              ),
            ),
          if (_logsError != null) const SizedBox(height: AppSpacing.md),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(AppSpacing.md),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Combined RobotAlert + RobotTelemetry',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  Wrap(
                    spacing: AppSpacing.sm,
                    runSpacing: AppSpacing.xs,
                    children: [
                      Chip(
                        label: Text('Alert: $_alertCount'),
                        avatar: const Icon(
                          Icons.warning_amber_rounded,
                          size: 16,
                          color: AppColors.warning,
                        ),
                      ),
                      Chip(
                        label: Text('Telemetry: $_telemetryCount'),
                        avatar: const Icon(
                          Icons.sensors,
                          size: 16,
                          color: AppColors.success,
                        ),
                      ),
                      Chip(label: Text('Showing max $_logLimit logs')),
                    ],
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  Wrap(
                    spacing: AppSpacing.sm,
                    children: [
                      ChoiceChip(
                        label: const Text('All'),
                        selected: _historyView == _HistoryView.all,
                        onSelected: (_) {
                          setState(() {
                            _historyView = _HistoryView.all;
                          });
                        },
                      ),
                      ChoiceChip(
                        label: const Text('Alerts'),
                        selected: _historyView == _HistoryView.alerts,
                        onSelected: (_) {
                          setState(() {
                            _historyView = _HistoryView.alerts;
                          });
                        },
                      ),
                      ChoiceChip(
                        label: const Text('Telemetry'),
                        selected: _historyView == _HistoryView.telemetry,
                        onSelected: (_) {
                          setState(() {
                            _historyView = _HistoryView.telemetry;
                          });
                        },
                      ),
                    ],
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  if (_loadingLogs)
                    const Padding(
                      padding: EdgeInsets.symmetric(vertical: AppSpacing.sm),
                      child: Center(child: CircularProgressIndicator()),
                    )
                  else if (_visibleEntries.isEmpty)
                    const Text(
                      'No log/telemetry records for selected date.',
                      style: TextStyle(color: AppColors.slate),
                    )
                  else
                    ..._visibleEntries.map((entry) {
                      final color = _severityColor(entry.severity);
                      return InkWell(
                        borderRadius: BorderRadius.circular(10),
                        onTap: () => _showEntryDetails(entry),
                        child: Container(
                          margin: const EdgeInsets.only(bottom: AppSpacing.sm),
                          padding: const EdgeInsets.all(AppSpacing.sm),
                          decoration: BoxDecoration(
                            color: AppColors.mist,
                            borderRadius: BorderRadius.circular(10),
                            border: Border.all(color: AppColors.line),
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Icon(entry.icon, color: color, size: 18),
                                  const SizedBox(width: AppSpacing.xs),
                                  Expanded(
                                    child: Text(
                                      entry.title,
                                      maxLines: 2,
                                      overflow: TextOverflow.ellipsis,
                                      style: const TextStyle(
                                        fontWeight: FontWeight.w600,
                                      ),
                                    ),
                                  ),
                                  const SizedBox(width: AppSpacing.xs),
                                  Text(
                                    _formatDateTime(entry.eventTime),
                                    style: const TextStyle(
                                      fontSize: 11,
                                      color: AppColors.slate,
                                    ),
                                  ),
                                ],
                              ),
                              const SizedBox(height: AppSpacing.xs),
                              Text(
                                entry.sourceLabel,
                                style: TextStyle(
                                  fontSize: 11,
                                  color: color,
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                              const SizedBox(height: 2),
                              Text(
                                entry.subtitle,
                                maxLines: 3,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(color: AppColors.slate),
                              ),
                            ],
                          ),
                        ),
                      );
                    }),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _RobotOption {
  final int robotId;
  final String factoryName;
  final String areaName;
  final String hubName;
  final Device device;

  const _RobotOption({
    required this.robotId,
    required this.factoryName,
    required this.areaName,
    required this.hubName,
    required this.device,
  });

  String get label =>
      '$robotId • ${device.name} • $factoryName/$areaName/$hubName';
}

class _HistoryEntry {
  final DateTime? eventTime;
  final String severity;
  final String title;
  final String subtitle;
  final String details;
  final IconData icon;
  final _HistorySource source;

  const _HistoryEntry({
    required this.eventTime,
    required this.severity,
    required this.title,
    required this.subtitle,
    required this.details,
    required this.icon,
    required this.source,
  });

  String get sourceLabel =>
      source == _HistorySource.alert ? 'ALERT' : 'TELEMETRY';

  factory _HistoryEntry.fromAlert(RobotAlert alert) {
    final title = alert.command.isNotEmpty ? alert.command : alert.logType;
    final subtitle = [
      if (alert.severity.isNotEmpty) 'severity=${alert.severity}',
      if (alert.status.isNotEmpty) 'status=${alert.status}',
      if (alert.logType.isNotEmpty) 'type=${alert.logType}',
    ].join(' • ');

    final details = [
      'EventTime: ${alert.eventTime?.toIso8601String() ?? '--'}',
      'Severity: ${alert.severity.isEmpty ? '--' : alert.severity}',
      'LogType: ${alert.logType.isEmpty ? '--' : alert.logType}',
      'Status: ${alert.status.isEmpty ? '--' : alert.status}',
      'Command: ${alert.command.isEmpty ? '--' : alert.command}',
      'RobotId: ${alert.robotId?.toString() ?? '--'}',
      'FactoryId: ${alert.factoryId?.toString() ?? '--'}',
      'UserId: ${alert.userId?.toString() ?? '--'}',
    ].join('\n');

    return _HistoryEntry(
      eventTime: alert.eventTime ?? alert.logDate,
      severity: alert.severity,
      title: title.isEmpty ? 'Robot alert' : title,
      subtitle: subtitle.isEmpty ? '--' : subtitle,
      details: details,
      icon: Icons.warning_amber_rounded,
      source: _HistorySource.alert,
    );
  }

  factory _HistoryEntry.fromTelemetry(RobotTelemetry telemetry) {
    final subtitle = [
      'battery=${telemetry.batteryPercent}%',
      if (telemetry.deviceType.isNotEmpty) 'type=${telemetry.deviceType}',
      'fps=${telemetry.fps.toStringAsFixed(1)}',
      if (telemetry.internetReachability.isNotEmpty)
        'network=${telemetry.internetReachability}',
    ].join(' • ');

    final details = [
      'Timestamp: ${telemetry.timestamp?.toIso8601String() ?? '--'}',
      'DeviceId: ${telemetry.deviceId?.toString() ?? '--'}',
      'DeviceName: ${telemetry.deviceName.isEmpty ? '--' : telemetry.deviceName}',
      'DeviceType: ${telemetry.deviceType.isEmpty ? '--' : telemetry.deviceType}',
      'Model: ${telemetry.deviceModel.isEmpty ? '--' : telemetry.deviceModel}',
      'JointData: ${telemetry.jointData.isEmpty ? '[]' : telemetry.jointData.join(', ')}',
      'Battery: ${telemetry.batteryPercent}% (${telemetry.batteryStatus})',
      'Temperatures: robot=${telemetry.robotTemp}, motor=${telemetry.motorTemp}, cpu=${telemetry.cpuTemp}',
      'FPS: ${telemetry.fps}',
      'Internet: ${telemetry.internetReachability.isEmpty ? '--' : telemetry.internetReachability}',
      'UptimeSeconds: ${telemetry.uptimeSeconds}',
    ].join('\n');

    return _HistoryEntry(
      eventTime: telemetry.timestamp,
      severity: 'INFO',
      title: telemetry.deviceName.isNotEmpty
          ? 'Telemetry • ${telemetry.deviceName}'
          : 'Telemetry',
      subtitle: subtitle,
      details: details,
      icon: Icons.sensors,
      source: _HistorySource.telemetry,
    );
  }
}

enum _HistoryView { all, alerts, telemetry }

enum _HistorySource { alert, telemetry }
