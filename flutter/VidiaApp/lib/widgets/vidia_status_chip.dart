import 'package:flutter/material.dart';

import '../theme/app_colors.dart';

class VidiaStatusChip extends StatelessWidget {
  const VidiaStatusChip({super.key, required this.label, required this.kind});

  final String label;
  final VidiaStatusKind kind;

  @override
  Widget build(BuildContext context) {
    final (background, foreground, icon) = switch (kind) {
      VidiaStatusKind.success => (
          AppColors.success.withValues(alpha: .12),
          AppColors.success,
          Icons.check_circle_outline
        ),
      VidiaStatusKind.warning => (
          AppColors.warning.withValues(alpha: .12),
          AppColors.warning,
          Icons.schedule_outlined
        ),
      VidiaStatusKind.error => (
          AppColors.danger.withValues(alpha: .12),
          AppColors.danger,
          Icons.cancel_outlined
        ),
      VidiaStatusKind.info => (
          AppColors.cyan.withValues(alpha: .14),
          AppColors.indigo,
          Icons.info_outline
        ),
    };
    return Chip(
      avatar: Icon(icon, size: 16, color: foreground),
      label: Text(label),
      backgroundColor: background,
      labelStyle: TextStyle(color: foreground, fontWeight: FontWeight.w700),
      visualDensity: VisualDensity.compact,
    );
  }
}

enum VidiaStatusKind { success, warning, error, info }
