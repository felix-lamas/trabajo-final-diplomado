import 'package:flutter/material.dart';

import '../models/inscripcion.dart';
import 'my_registrations_screen.dart';

class RegistrationConfirmationScreen extends StatelessWidget {
  const RegistrationConfirmationScreen({
    super.key,
    required this.registration,
  });

  final Inscripcion registration;

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Inscripción confirmada')),
        body: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(28),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(
                  padding: const EdgeInsets.all(22),
                  decoration: BoxDecoration(
                    color: Colors.green.withValues(alpha: .12),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(
                    Icons.check_circle_outline_rounded,
                    color: Colors.green,
                    size: 64,
                  ),
                ),
                const SizedBox(height: 24),
                Text(
                  '¡Inscripción realizada!',
                  style: Theme.of(context).textTheme.headlineSmall,
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 10),
                Text(
                  registration.eventoTitulo,
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 6),
                Text('Estado: ${_friendlyStatus(registration.estado)}'),
                const SizedBox(height: 28),
                FilledButton.icon(
                  onPressed: () => Navigator.pushReplacement(
                    context,
                    MaterialPageRoute(
                      builder: (_) => const MyRegistrationsScreen(),
                    ),
                  ),
                  icon: const Icon(Icons.list_alt_rounded),
                  label: const Text('Ver mis inscripciones'),
                ),
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Volver al catálogo'),
                ),
              ],
            ),
          ),
        ),
      );
}

String _friendlyStatus(String status) => status
    .toLowerCase()
    .split('_')
    .map((part) =>
        part.isEmpty ? part : '${part[0].toUpperCase()}${part.substring(1)}')
    .join(' ');
