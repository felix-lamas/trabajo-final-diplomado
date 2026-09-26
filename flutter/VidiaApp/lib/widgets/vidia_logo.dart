import 'package:flutter/material.dart';

class VidiaLogo extends StatelessWidget {
  const VidiaLogo({
    super.key,
    this.width,
    this.height,
    this.fit = BoxFit.contain,
  });

  static const assetPath = 'assets/images/vidia_logo.png';

  final double? width;
  final double? height;
  final BoxFit fit;

  @override
  Widget build(BuildContext context) => Image.asset(
        assetPath,
        width: width,
        height: height,
        fit: fit,
        filterQuality: FilterQuality.high,
        errorBuilder: (_, __, ___) => const SizedBox.shrink(),
      );
}
