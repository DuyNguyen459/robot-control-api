import 'package:flutter/material.dart';

import '../constants/app_colors.dart';
import '../constants/app_navigation.dart';
import '../constants/app_spacing.dart';
import '../models/factory.dart';
import '../models/session_identity.dart';
import '../services/api_service.dart';
import '../services/session_service.dart';
import '../widgets/app_shell.dart';
import 'api_login_screen.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key, this.showBackButton = true});

  final bool showBackButton;

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  late Future<_ProfileViewData?> _profileFuture;
  final ApiService _apiService = ApiService();

  @override
  void initState() {
    super.initState();
    _profileFuture = _loadProfileData();
  }

  Future<_ProfileViewData?> _loadProfileData() async {
    final identity = await SessionService.getSessionIdentity();
    if (identity == null) {
      return null;
    }

    Factory? managedFactory;
    int areaCount = 0;
    int hubCount = 0;
    int deviceCount = 0;
    final factoryId = identity.user.factoryId;

    if (factoryId != null) {
      try {
        final factories = await _apiService.fetchFactories();
        for (final factory in factories) {
          if (factory.id == factoryId.toString()) {
            managedFactory = factory;
            break;
          }
        }

        if (managedFactory != null) {
          final areas = await _apiService.fetchAreasByFactory(
            managedFactory.id,
          );
          areaCount = areas.length;

          for (final area in areas) {
            final hubs = await _apiService.fetchHubsByArea(area.id);
            hubCount += hubs.length;

            for (final hub in hubs) {
              final devices = await _apiService.fetchDevicesByHub(hub.id);
              deviceCount += devices.length;
            }
          }
        }
      } catch (_) {
        // Keep profile usable even if factory lookup fails.
      }
    }

    return _ProfileViewData(
      identity: identity,
      managedFactory: managedFactory,
      areaCount: areaCount,
      hubCount: hubCount,
      deviceCount: deviceCount,
    );
  }

  Future<void> _reloadProfile() async {
    setState(() {
      _profileFuture = _loadProfileData();
    });
    await _profileFuture;
  }

  Widget _buildInfoLine(IconData icon, String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.sm),
      child: Row(
        children: [
          Icon(icon, size: 16, color: AppColors.slate),
          const SizedBox(width: AppSpacing.sm),
          Expanded(
            child: Text(
              '$label: $value',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildOutlineCard({required Widget child}) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.lg),
      decoration: BoxDecoration(
        color: AppColors.paper,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: AppColors.line),
      ),
      child: child,
    );
  }

  @override
  Widget build(BuildContext context) {
    final content = Container(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [Color(0xFF101010), Color(0xFF191919)],
        ),
      ),
      child: RefreshIndicator(
        onRefresh: _reloadProfile,
        child: FutureBuilder<_ProfileViewData?>(
          future: _profileFuture,
          builder: (context, snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return ListView(
                physics: const AlwaysScrollableScrollPhysics(),
                children: const [
                  SizedBox(height: 240),
                  Center(child: CircularProgressIndicator()),
                ],
              );
            }

            final data = snapshot.data;
            if (data == null) {
              return ListView(
                physics: const AlwaysScrollableScrollPhysics(),
                padding: const EdgeInsets.all(AppSpacing.md),
                children: [
                  const SizedBox(height: 220),
                  ElevatedButton(
                    onPressed: () {
                      Navigator.of(context).pushAndRemoveUntil(
                        appRoute(const ApiLoginScreen()),
                        (route) => false,
                      );
                    },
                    child: const Text('Go to login'),
                  ),
                ],
              );
            }

            final user = data.identity.user;
            final managedFactory = data.managedFactory;

            return ListView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.all(AppSpacing.md),
              children: [
                Center(
                  child: ConstrainedBox(
                    constraints: const BoxConstraints(maxWidth: 560),
                    child: Column(
                      children: [
                        _buildOutlineCard(
                          child: Column(
                            children: [
                              Container(
                                width: 92,
                                height: 92,
                                decoration: BoxDecoration(
                                  shape: BoxShape.circle,
                                  border: Border.all(color: AppColors.line),
                                  color: AppColors.mist,
                                ),
                                child: const Icon(Icons.person, size: 50),
                              ),
                              const SizedBox(height: AppSpacing.md),
                              Text(
                                user.username,
                                textAlign: TextAlign.center,
                                style: Theme.of(
                                  context,
                                ).textTheme.headlineSmall,
                              ),
                              const SizedBox(height: 4),
                              Text(
                                user.email,
                                textAlign: TextAlign.center,
                                style: Theme.of(context).textTheme.bodyMedium
                                    ?.copyWith(color: AppColors.slate),
                              ),
                              const SizedBox(height: AppSpacing.md),
                              _buildInfoLine(
                                Icons.verified_user_outlined,
                                'Role',
                                user.role.toUpperCase(),
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(height: AppSpacing.md),
                        _buildOutlineCard(
                          child: Row(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Container(
                                width: 92,
                                height: 92,
                                decoration: BoxDecoration(
                                  borderRadius: BorderRadius.circular(14),
                                  border: Border.all(color: AppColors.line),
                                  color: AppColors.mist,
                                ),
                                child: const Icon(
                                  Icons.factory_outlined,
                                  size: 44,
                                ),
                              ),
                              const SizedBox(width: AppSpacing.md),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      'Factory Dang Quan Ly',
                                      style: Theme.of(
                                        context,
                                      ).textTheme.titleLarge,
                                    ),
                                    const SizedBox(height: AppSpacing.sm),
                                    _buildInfoLine(
                                      Icons.apartment_outlined,
                                      'Factory',
                                      managedFactory?.name ?? '--',
                                    ),
                                    _buildInfoLine(
                                      Icons.pin_drop_outlined,
                                      'Location',
                                      managedFactory?.location ?? '--',
                                    ),
                                    _buildInfoLine(
                                      Icons.numbers,
                                      'Factory ID',
                                      user.factoryId?.toString() ?? '--',
                                    ),
                                    _buildInfoLine(
                                      Icons.grid_view_outlined,
                                      'Areas',
                                      data.areaCount.toString(),
                                    ),
                                    _buildInfoLine(
                                      Icons.hub_outlined,
                                      'Hubs',
                                      data.hubCount.toString(),
                                    ),
                                    _buildInfoLine(
                                      Icons.memory_outlined,
                                      'Devices',
                                      data.deviceCount.toString(),
                                    ),
                                  ],
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            );
          },
        ),
      ),
    );

    if (!widget.showBackButton) {
      return AppShell(title: 'Profile', currentTabIndex: 4, child: content);
    }

    return Scaffold(
      appBar: AppBar(
        leading: widget.showBackButton
            ? IconButton(
                onPressed: () => Navigator.of(context).pop(),
                icon: const Icon(Icons.arrow_back),
              )
            : null,
        title: const Text('Profile'),
      ),
      body: content,
    );
  }
}

class _ProfileViewData {
  final SessionIdentity identity;
  final Factory? managedFactory;
  final int areaCount;
  final int hubCount;
  final int deviceCount;

  const _ProfileViewData({
    required this.identity,
    required this.managedFactory,
    required this.areaCount,
    required this.hubCount,
    required this.deviceCount,
  });
}
