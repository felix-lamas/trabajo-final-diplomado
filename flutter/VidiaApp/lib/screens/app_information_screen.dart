import 'package:flutter/material.dart';

enum AppInformation { help, contact, about }

class AppInformationScreen extends StatelessWidget {
  const AppInformationScreen({super.key, required this.section});
  final AppInformation section;
  @override
  Widget build(BuildContext context) {
    final (title, text) = switch (section) {
      AppInformation.help => (
        'Ayuda',
        'Descubre eventos desde Eventos y abre el detalle para inscribirte.\n\nEn Actividad puedes consultar tus inscripciones, gestionar pagos y comprobantes, registrar asistencia con el QR de una sesión y consultar certificados.\n\nLa asistencia presencial puede solicitar cámara y ubicación. El resultado de la validación se muestra al registrar tu asistencia.\n\nPara recuperar tu cuenta, utiliza la opción de recuperación del Login y abre el enlace recibido por correo.',
      ),
      AppInformation.contact => (
        'Contacto',
        'Consulta la información de contacto publicada por el organizador en el detalle del evento.\n\nSi necesitas ayuda con un pago, una sesión de asistencia o un certificado, indica el evento y el estado mostrado en tu cuenta.\n\nNo compartas tu contraseña ni los enlaces privados de recuperación o verificación.',
      ),
      AppInformation.about => (
        'Acerca de Vidia',
        'Vidia reúne eventos y la actividad de sus participantes.\n\nDesde la aplicación puedes descubrir eventos, consultar tus inscripciones y pagos, registrar asistencia y acceder a tus certificados.\n\nLa disponibilidad y los estados de cada actividad corresponden a la información del sistema.',
      ),
    };
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(24),
          children: [
            Icon(
              switch (section) {
                AppInformation.help => Icons.help_outline_rounded,
                AppInformation.contact => Icons.contact_support_outlined,
                AppInformation.about => Icons.info_outline_rounded,
              },
              size: 48,
              color: Theme.of(context).colorScheme.primary,
            ),
            const SizedBox(height: 24),
            Text(title, style: Theme.of(context).textTheme.headlineSmall),
            const SizedBox(height: 16),
            Text(text, style: Theme.of(context).textTheme.bodyLarge),
          ],
        ),
      ),
    );
  }
}
