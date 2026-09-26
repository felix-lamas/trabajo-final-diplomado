import 'package:flutter/material.dart';

import '../theme/app_colors.dart';

class VidiaBrand extends StatelessWidget {
  const VidiaBrand({super.key, this.compact = false, this.onDark = false});

  final bool compact;
  final bool onDark;

  @override
  Widget build(BuildContext context) {
    final titleStyle = Theme.of(context).textTheme.headlineSmall?.copyWith(
          color: onDark ? Colors.white : AppColors.midnight,
          fontWeight: FontWeight.w900,
          letterSpacing: 1.8,
        );
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: compact ? 32 : 44,
          height: compact ? 32 : 44,
          decoration: BoxDecoration(
            gradient: AppColors.brandGradient,
            borderRadius: BorderRadius.circular(compact ? 11 : 15),
          ),
          child: Icon(
            Icons.auto_awesome_rounded,
            color: Colors.white,
            size: compact ? 18 : 25,
          ),
        ),
        const SizedBox(width: 10),
        Text('VIDIA', style: titleStyle),
      ],
    );
  }
}
