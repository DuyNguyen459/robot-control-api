import 'package:flutter/material.dart';

import '../constants/app_colors.dart';
import '../constants/app_spacing.dart';
import 'app_status_chip.dart';

class AppEntityTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final String? status;
  final VoidCallback onTap;

  const AppEntityTile({
    super.key,
    required this.icon,
    required this.title,
    required this.subtitle,
    this.status,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return Card(
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.md),
          child: Row(
            children: [
              Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(
                  color: AppColors.mist,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.line),
                ),
                child: Icon(icon, color: AppColors.ink, size: 20),
              ),
              const SizedBox(width: AppSpacing.md),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: textTheme.titleMedium,
                    ),
                    const SizedBox(height: 2),
                    Text(
                      subtitle,
                      style: textTheme.bodySmall,
                    ),
                  ],
                ),
              ),
              if (status != null && status!.trim().isNotEmpty)
                Padding(
                  padding: const EdgeInsets.only(right: AppSpacing.sm),
                  child: AppStatusChip(status: status!),
                ),
              const Icon(Icons.chevron_right, color: AppColors.slate),
            ],
          ),
        ),
      ),
    );
  }
}
