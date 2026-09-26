import 'package:flutter/material.dart';

class VidiaSectionTitle extends StatelessWidget {
  const VidiaSectionTitle({super.key, required this.title, this.subtitle});

  final String title;
  final String? subtitle;

  @override
  Widget build(BuildContext context) => Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: Theme.of(context).textTheme.headlineSmall),
          if (subtitle != null) ...[
            const SizedBox(height: 5),
            Text(subtitle!, style: Theme.of(context).textTheme.bodyMedium),
          ],
        ],
      );
}
