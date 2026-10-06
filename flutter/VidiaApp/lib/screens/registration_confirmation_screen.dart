import 'package:flutter/material.dart';

import '../models/inscripcion.dart';
import 'payment_management_screen.dart';
import 'my_registrations_screen.dart';

class RegistrationConfirmationScreen extends StatelessWidget {
  const RegistrationConfirmationScreen({super.key, required this.registration});
  final Inscripcion registration;

  @override
  Widget build(BuildContext context) {
    final confirmed = registration.estado == 'CONFIRMADA';
    final pendingPayment = registration.estado == 'PENDIENTE_PAGO';
    return Scaffold(
      appBar: AppBar(title: const Text('Resultado de inscripción')),
      body: Center(
          child: SingleChildScrollView(
        padding: const EdgeInsets.all(28),
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          Icon(confirmed ? Icons.check_circle_outline : Icons.schedule,
              color: confirmed ? Colors.green : Colors.orange, size: 68),
          const SizedBox(height: 24),
          Text(
              confirmed
                  ? '¡Inscripción confirmada!'
                  : pendingPayment
                      ? 'Inscripción pendiente de pago'
                      : 'Solicitud de inscripción recibida',
              style: Theme.of(context).textTheme.headlineSmall,
              textAlign: TextAlign.center),
          const SizedBox(height: 10),
          Text(registration.eventoTitulo, textAlign: TextAlign.center),
          const SizedBox(height: 6),
          Text('Estado: ${friendlyStatus(registration.estado)}'),
          const SizedBox(height: 28),
          FilledButton.icon(
            onPressed: () => Navigator.pushReplacement(
                context,
                MaterialPageRoute(
                    builder: (_) => const MyRegistrationsScreen())),
            icon: const Icon(Icons.list_alt_rounded),
            label: const Text('Ver mis inscripciones'),
          ),
          if (pendingPayment) ...[
            const SizedBox(height: 8),
            OutlinedButton.icon(
              onPressed: () => Navigator.push(
                context,
                MaterialPageRoute<void>(
                  builder: (_) =>
                      PaymentManagementScreen(registration: registration),
                ),
              ),
              icon: const Icon(Icons.payments_outlined),
              label: const Text('Ver pago y enviar comprobante'),
            ),
          ],
          TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Volver al catálogo')),
        ]),
      )),
    );
  }
}

String friendlyStatus(String status) => status
    .toLowerCase()
    .split('_')
    .map((part) =>
        part.isEmpty ? part : '${part[0].toUpperCase()}${part.substring(1)}')
    .join(' ');
