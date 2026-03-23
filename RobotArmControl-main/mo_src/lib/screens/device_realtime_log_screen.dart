import 'dart:async';

import 'package:flutter/material.dart';

import '../constants/app_colors.dart';
import '../constants/app_spacing.dart';
import '../models/robot_alert.dart';
import '../models/robot_telemetry.dart';
import '../services/api_service.dart';
import '../widgets/app_shell.dart';
import '../widgets/app_states.dart';

class DeviceRealtimeLogScreen extends StatefulWidget {
  final int robotId;
  final String? deviceName;

  const DeviceRealtimeLogScreen({
    super.key,
    required this.robotId,
    this.deviceName,
  });

  @override
  State<DeviceRealtimeLogScreen> createState() =>
      _DeviceRealtimeLogScreenState();
}

class _DeviceRealtimeLogScreenState extends State<DeviceRealtimeLogScreen> {
  final ApiService _apiService = ApiService();

  static const int _telemetryLimit = 1;
  static const int _alertLimit = 10;
  static const Duration _pollInterval = Duration(milliseconds: 100);

  bool _loading = true;
  bool _refreshing = false;
  String? _error;

  RobotTelemetry? _latestTelemetry;
  List<RobotAlert> _alerts = const [];
  DateTime? _lastUpdated;
  bool _requestInFlight = false;

  Timer? _pollTimer;

  @override
  void initState() {
    super.initState();
    _loadSnapshot();
    _pollTimer = Timer.periodic(_pollInterval, (_) => _refreshSilently());
  }

  @override
  void dispose() {
    _pollTimer?.cancel();
    super.dispose();
  }

  Future<void> _refreshSilently() async {
    if (!mounted || _refreshing || _requestInFlight) {
      return;
    }
    await _loadSnapshot(
      showLoading: false,
      showRefreshingIndicator: false,
      fetchAlerts: true,
    );
  }

  Future<void> _loadSnapshot({
    bool showLoading = true,
    bool showRefreshingIndicator = true,
    bool fetchAlerts = true,
  }) async {
    if (_requestInFlight) {
      return;
    }
    _requestInFlight = true;

    if (showLoading) {
      setState(() {
        _loading = true;
        _error = null;
      });
    } else if (showRefreshingIndicator) {
      setState(() {
        _refreshing = true;
      });
    }

    try {
      final telemetry = (await _apiService.fetchRobotTelemetryToday(
        widget.robotId,
        limit: _telemetryLimit,
      )).toList();
      telemetry.sort((a, b) {
        final aTs = a.timestamp ?? DateTime.fromMillisecondsSinceEpoch(0);
        final bTs = b.timestamp ?? DateTime.fromMillisecondsSinceEpoch(0);
        return bTs.compareTo(aTs);
      });

      if (!mounted) {
        return;
      }

      final latestTelemetry = telemetry.isEmpty ? null : telemetry.first;
      setState(() {
        _latestTelemetry = latestTelemetry;
        _lastUpdated = latestTelemetry?.timestamp?.toLocal() ?? DateTime.now();
        _error = null;
      });

      if (fetchAlerts) {
        final alerts = (await _apiService.fetchRobotAlertsToday(
          widget.robotId,
          limit: _alertLimit,
        )).toList();
        alerts.sort((a, b) {
          final aTs = a.eventTime ?? DateTime.fromMillisecondsSinceEpoch(0);
          final bTs = b.eventTime ?? DateTime.fromMillisecondsSinceEpoch(0);
          return bTs.compareTo(aTs);
        });

        if (!mounted) {
          return;
        }

        setState(() {
          _alerts = alerts;
        });
      }
    } catch (e) {
      if (!mounted) {
        return;
      }

      setState(() {
        _error = e.toString();
      });
    } finally {
      _requestInFlight = false;
      if (mounted) {
        setState(() {
          _loading = false;
          if (showRefreshingIndicator) {
            _refreshing = false;
          }
        });
      }
    }
  }

  Color _severityColor(String severity) {
    final normalized = severity.toUpperCase();
    if (normalized.contains('ERROR') || normalized.contains('CRITICAL')) {
      return Colors.redAccent;
    }
    if (normalized.contains('WARN')) {
      return AppColors.warning;
    }
    return AppColors.slate;
  }

  String _formatDateTime(DateTime? dt) {
    if (dt == null) {
      return '--';
    }

    final local = dt.toLocal();
    String two(int n) => n.toString().padLeft(2, '0');
    final ms = local.millisecond.toString().padLeft(3, '0');
    return '${two(local.day)}/${two(local.month)}/${local.year} ${two(local.hour)}:${two(local.minute)}:${two(local.second)}.$ms';
  }

  String _formatDouble(double value) {
    return value.toString();
  }

  String _jointAnglesText(List<double> values) {
    if (values.isEmpty) {
      return '[]';
    }
    final formatted = values.map((v) => v.toStringAsFixed(3)).join(', ');
    return '[$formatted]';
  }

  Widget _metricTile({
    required String label,
    required String value,
    IconData? icon,
    Color? valueColor,
  }) {
    return Container(
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
              if (icon != null) ...[
                Icon(icon, size: 14, color: AppColors.charcoal),
                const SizedBox(width: 4),
              ],
              Expanded(
                child: Text(
                  label,
                  style: const TextStyle(fontSize: 12, color: AppColors.slate),
                ),
              ),
            ],
          ),
          const SizedBox(height: 6),
          Text(
            value,
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w700,
              color: valueColor ?? AppColors.ink,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSessionWidget() {
    final telemetry = _latestTelemetry;

    if (telemetry == null) {
      return Card(
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.md),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: const [
              Text(
                'Session Widget (Realtime Telemetry)',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
              ),
              SizedBox(height: AppSpacing.sm),
              Text(
                'No telemetry data available for this robot today.',
                style: TextStyle(color: AppColors.slate),
              ),
            ],
          ),
        ),
      );
    }

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.md),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Session Widget (Realtime Telemetry)',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: AppSpacing.xs),
            Text(
              'Last update: ${_formatDateTime(_lastUpdated)}',
              style: const TextStyle(fontSize: 12, color: AppColors.slate),
            ),
            const SizedBox(height: AppSpacing.sm),
            _metricTile(
              label: 'Joint Angles',
              value: _jointAnglesText(telemetry.jointData),
              icon: Icons.rotate_right,
            ),
            const SizedBox(height: AppSpacing.sm),
            GridView.count(
              physics: const NeverScrollableScrollPhysics(),
              shrinkWrap: true,
              crossAxisCount: 2,
              mainAxisSpacing: AppSpacing.sm,
              crossAxisSpacing: AppSpacing.sm,
              childAspectRatio: 1.75,
              children: [
                _metricTile(
                  label: 'Battery',
                  value: '${telemetry.batteryPercent}%',
                  icon: Icons.battery_std,
                ),
                _metricTile(
                  label: 'Battery Status',
                  value: telemetry.batteryStatus.isEmpty
                      ? '--'
                      : telemetry.batteryStatus,
                  icon: Icons.battery_alert,
                ),
                _metricTile(
                  label: 'Robot Temp',
                  value: '${_formatDouble(telemetry.robotTemp)} C',
                  icon: Icons.thermostat,
                ),
                _metricTile(
                  label: 'Motor Temp',
                  value: '${_formatDouble(telemetry.motorTemp)} C',
                  icon: Icons.electric_bolt,
                ),
                _metricTile(
                  label: 'CPU Temp',
                  value: '${_formatDouble(telemetry.cpuTemp)} C',
                  icon: Icons.memory,
                ),
                _metricTile(
                  label: 'FPS',
                  value: _formatDouble(telemetry.fps),
                  icon: Icons.speed,
                ),
                _metricTile(
                  label: 'Internet',
                  value: telemetry.internetReachability.isEmpty
                      ? '--'
                      : telemetry.internetReachability,
                  icon: Icons.wifi,
                ),
                _metricTile(
                  label: 'Uptime',
                  value: '${_formatDouble(telemetry.uptimeSeconds)} s',
                  icon: Icons.timer,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAlertsWidget() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.md),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Alerts Widget (${_alerts.length})',
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: AppSpacing.sm),
            if (_alerts.isEmpty)
              const Text(
                'No alerts found for this robot today.',
                style: TextStyle(color: AppColors.slate),
              )
            else
              ..._alerts.map((alert) {
                final severityColor = _severityColor(alert.severity);
                return Container(
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
                          Expanded(
                            child: Text(
                              _formatDateTime(alert.eventTime),
                              style: const TextStyle(
                                fontWeight: FontWeight.w700,
                                color: AppColors.ink,
                              ),
                            ),
                          ),
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 8,
                              vertical: 4,
                            ),
                            decoration: BoxDecoration(
                              color: severityColor.withValues(alpha: 0.2),
                              borderRadius: BorderRadius.circular(20),
                              border: Border.all(color: severityColor),
                            ),
                            child: Text(
                              alert.severity.isEmpty
                                  ? 'UNKNOWN'
                                  : alert.severity,
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.w700,
                                color: severityColor,
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: AppSpacing.xs),
                      Text(
                        'LogType: ${alert.logType.isEmpty ? '--' : alert.logType}',
                        style: const TextStyle(color: AppColors.charcoal),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        'Status: ${alert.status.isEmpty ? '--' : alert.status}',
                        style: const TextStyle(color: AppColors.charcoal),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        'Command: ${alert.command.isEmpty ? '--' : alert.command}',
                        style: const TextStyle(color: AppColors.charcoal),
                      ),
                    ],
                  ),
                );
              }),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final titleName = widget.deviceName?.trim().isNotEmpty == true
        ? widget.deviceName!.trim()
        : 'Robot ${widget.robotId}';

    return AppShell(
      title: 'Realtime Logs • $titleName',
      currentTabIndex: 0,
      actions: [
        IconButton(
          tooltip: 'Refresh',
          onPressed: _refreshing
              ? null
              : () => _loadSnapshot(
                  showLoading: false,
                  showRefreshingIndicator: true,
                ),
          icon: _refreshing
              ? const SizedBox(
                  width: 18,
                  height: 18,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              : const Icon(Icons.refresh),
        ),
      ],
      child: _loading
          ? const AppLoadingState()
          : _error != null
          ? AppErrorState(
              title: 'Unable to load realtime logs',
              message: _error!,
              actionLabel: 'Retry',
              onAction: _loadSnapshot,
            )
          : RefreshIndicator(
              onRefresh: () => _loadSnapshot(
                showLoading: false,
                showRefreshingIndicator: true,
              ),
              child: ListView(
                padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
                children: [
                  _buildSessionWidget(),
                  const SizedBox(height: AppSpacing.md),
                  _buildAlertsWidget(),
                ],
              ),
            ),
    );
  }
}
