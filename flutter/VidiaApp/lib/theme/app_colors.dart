import 'package:flutter/material.dart';

abstract final class AppColors {
  static const midnight = Color(0xFF16143A);
  static const indigo = Color(0xFF302C78);
  static const violet = Color(0xFF7451D9);
  static const cyan = Color(0xFF27C6E8);
  static const magenta = Color(0xFFD44BC8);
  static const coral = Color(0xFFFF746C);
  static const softBackground = Color(0xFFF8F7FC);
  static const darkBackground = Color(0xFF10101C);
  static const success = Color(0xFF187A5B);
  static const warning = Color(0xFFC66A13);
  static const danger = Color(0xFFBB3852);

  static const brandGradient = LinearGradient(
    colors: [indigo, violet, magenta, coral],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );
}
