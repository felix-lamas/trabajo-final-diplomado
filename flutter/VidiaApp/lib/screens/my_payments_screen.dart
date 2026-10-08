import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/inscripcion.dart';
import '../models/pago.dart';
import '../repositories/inscripcion_repository.dart';
import '../repositories/pago_repository.dart';
import '../widgets/load_error.dart';
import '../widgets/vidia_empty_state.dart';
import '../widgets/vidia_status_chip.dart';
import 'payment_management_screen.dart';

class MyPaymentsScreen extends StatefulWidget {
  const MyPaymentsScreen({super.key});
  @override
  State<MyPaymentsScreen> createState() => _MyPaymentsScreenState();
}

class _MyPaymentsScreenState extends State<MyPaymentsScreen> {
  late Future<(List<Pago>, List<Inscripcion>)> _data;
  @override
  void initState() {
    super.initState();
    _data = _fetch();
  }

  Future<(List<Pago>, List<Inscripcion>)> _fetch() async {
    final results = await Future.wait<Object>([
      context.read<PagoRepository>().fetchMine(),
      context.read<InscripcionRepository>().fetchMine(),
    ]);
    return (results[0] as List<Pago>, results[1] as List<Inscripcion>);
  }

  Future<void> _refresh() async {
    final future = _fetch();
    setState(() {
      _data = future;
    });
    try {
      await future;
    } catch (_) {
      /* FutureBuilder presents the error. */
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Mis pagos')),
    body: SafeArea(
      child: FutureBuilder<(List<Pago>, List<Inscripcion>)>(
        future: _data,
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return LoadError(
              message: 'No pudimos consultar tus pagos.',
              onRetry: _refresh,
            );
          }
          final (payments, registrations) = snapshot.data!;
          final byId = {for (final item in registrations) item.id: item};
          return RefreshIndicator(
            onRefresh: _refresh,
            child: ListView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.all(20),
              children: [
                const Text(
                  'Consulta el estado de tus pagos y gestiona los comprobantes desde cada detalle.',
                ),
                const SizedBox(height: 16),
                if (payments.isEmpty)
                  const VidiaEmptyState(
                    icon: Icons.payments_outlined,
                    message: 'Todavía no tienes pagos registrados.',
                  ),
                for (final payment in payments)
                  Card(
                    child: ListTile(
                      contentPadding: const EdgeInsets.all(16),
                      leading: const Icon(Icons.receipt_long_outlined),
                      title: Text(payment.eventoTitulo),
                      subtitle: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Bs. ${payment.monto.toStringAsFixed(2)}'),
                          const SizedBox(height: 8),
                          VidiaStatusChip(
                            label: payment.estado.replaceAll('_', ' '),
                            kind: switch (payment.estado) {
                              'APROBADO' => VidiaStatusKind.success,
                              'RECHAZADO' => VidiaStatusKind.error,
                              _ => VidiaStatusKind.info,
                            },
                          ),
                          if (byId[payment.inscripcionId] == null)
                            const Text(
                              'No está disponible la inscripción asociada a este pago.',
                            ),
                        ],
                      ),
                      trailing: byId[payment.inscripcionId] == null
                          ? null
                          : const Icon(Icons.chevron_right_rounded),
                      onTap: byId[payment.inscripcionId] == null
                          ? null
                          : () async {
                              await Navigator.push(
                                context,
                                MaterialPageRoute<void>(
                                  builder: (_) => PaymentManagementScreen(
                                    registration: byId[payment.inscripcionId]!,
                                  ),
                                ),
                              );
                              if (mounted) await _refresh();
                            },
                    ),
                  ),
              ],
            ),
          );
        },
      ),
    ),
  );
}
