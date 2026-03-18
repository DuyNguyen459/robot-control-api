import 'package:flutter/material.dart';

import '../constants/app_colors.dart';

class AppStatusChip extends StatelessWidget {
  final String status;

  const AppStatusChip({super.key, required this.status});

  bool get _isActive {
    final normalized = status.trim().toUpperCase();
    return normalized == 'ACTIVE' || normalized == 'ONLINE';
  }

  bool get _isInactive {
    final normalized = status.trim().toUpperCase();
    return normalized == 'INACTIVE' || normalized == 'OFFLINE';
  }

  @override
  Widget build(BuildContext context) {
    final Color fg;
    final Color bg;
    final Color bd;

    if (_isActive) {
      fg = AppColors.success;
      bg = const Color(0xFF143126);
      bd = const Color(0xFF2C5A45);
    } else if (_isInactive) {
      fg = AppColors.slate;
      bg = const Color(0xFF212121);
      bd = const Color(0xFF343434);
    } else {
      fg = AppColors.warning;
      bg = const Color(0xFF382A16);
      bd = const Color(0xFF5C4626);
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 5),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: bd),
      ),
      child: Text(
        status.toUpperCase(),
        style: TextStyle(
          fontSize: 10,
          fontWeight: FontWeight.w700,
          letterSpacing: 0.45,
          color: fg,
        ),
      ),
    );
  }
}
