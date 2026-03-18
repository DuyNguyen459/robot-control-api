import 'package:flutter/material.dart';

import '../constants/app_colors.dart';
import '../constants/app_spacing.dart';
import '../widgets/app_shell.dart';

class ControlScreen extends StatelessWidget {
  const ControlScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return AppShell(
      title: 'Remote Control',
      currentTabIndex: 2,
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 440),
          child: Card(
            child: Padding(
              padding: const EdgeInsets.all(AppSpacing.lg),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Text(
                    'Manual Control Pad',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.w700),
                  ),
                  const SizedBox(height: AppSpacing.md),
                  _btn(Icons.keyboard_arrow_up, 'FORWARD (W)'),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      _btn(Icons.keyboard_arrow_left, 'LEFT (A)'),
                      const SizedBox(width: AppSpacing.xl),
                      _btn(Icons.keyboard_arrow_right, 'RIGHT (D)'),
                    ],
                  ),
                  _btn(Icons.keyboard_arrow_down, 'BACK (S)'),
                  const SizedBox(height: AppSpacing.lg),
                  ElevatedButton.icon(
                    onPressed: () => debugPrint('LOG: Action Grab recorded'),
                    icon: const Icon(Icons.pan_tool_alt_outlined),
                    label: const Text('GRAB / RELEASE'),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _btn(IconData icon, String label) {
    return IconButton(
      icon: Container(
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: AppColors.line),
          color: AppColors.paper,
        ),
        padding: const EdgeInsets.all(6),
        child: Icon(icon, size: 52, color: AppColors.ink),
      ),
      onPressed: () => debugPrint('LOG: $label'),
    );
  }
}
