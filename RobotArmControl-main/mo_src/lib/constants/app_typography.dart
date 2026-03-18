import 'package:flutter/material.dart';

import 'app_colors.dart';

class AppTypography {
  static TextTheme textTheme() {
    return const TextTheme(
      displaySmall: TextStyle(
        fontSize: 28,
        height: 1.15,
        fontWeight: FontWeight.w800,
        color: AppColors.ink,
        letterSpacing: 0.2,
      ),
      headlineSmall: TextStyle(
        fontSize: 22,
        height: 1.2,
        fontWeight: FontWeight.w700,
        color: AppColors.ink,
      ),
      titleLarge: TextStyle(
        fontSize: 18,
        height: 1.2,
        fontWeight: FontWeight.w700,
        color: AppColors.ink,
      ),
      titleMedium: TextStyle(
        fontSize: 16,
        height: 1.25,
        fontWeight: FontWeight.w600,
        color: AppColors.ink,
      ),
      bodyLarge: TextStyle(
        fontSize: 15,
        height: 1.35,
        fontWeight: FontWeight.w500,
        color: AppColors.ink,
      ),
      bodyMedium: TextStyle(
        fontSize: 14,
        height: 1.35,
        fontWeight: FontWeight.w400,
        color: AppColors.ink,
      ),
      bodySmall: TextStyle(
        fontSize: 12,
        height: 1.35,
        fontWeight: FontWeight.w400,
        color: AppColors.slate,
      ),
      labelLarge: TextStyle(
        fontSize: 13,
        height: 1.2,
        fontWeight: FontWeight.w600,
        color: AppColors.ink,
        letterSpacing: 0.2,
      ),
      labelMedium: TextStyle(
        fontSize: 11,
        height: 1.2,
        fontWeight: FontWeight.w600,
        color: AppColors.slate,
        letterSpacing: 0.3,
      ),
    );
  }
}
