import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../controllers/account_overview_controller.dart';
import '../widgets/activity_destination_tile.dart';
import 'history_screen.dart';
import 'my_attendance_screen.dart';
import 'my_certificates_screen.dart';
import 'my_payments_screen.dart';
import 'my_registrations_screen.dart';

class ActivityScreen extends StatelessWidget {
  const ActivityScreen({super.key});
  @override
  Widget build(BuildContext context) {
    final overview = context.watch<AccountOverviewController>();
    Future<void> open(Widget screen) async {
      await Navigator.of(
        context,
      ).push(MaterialPageRoute<void>(builder: (_) => screen));
      if (context.mounted) await overview.refresh();
    }

    String count(List<Object>? values, String singular, String plural) =>
        overview.loading
        ? 'Actualizando…'
        : values == null
        ? 'No disponible · toca para consultar'
        : '${values.length} ${values.length == 1 ? singular : plural}';
    return Scaffold(
      appBar: AppBar(
        title: const Text('Actividad'),
        automaticallyImplyLeading: false,
      ),
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: overview.refresh,
          child: ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
            children: [
              Text(
                'Tu participación en Vidia',
                style: Theme.of(context).textTheme.headlineSmall,
              ),
              const SizedBox(height: 8),
              const Text(
                'Consulta tus inscripciones, pagos y logros en un solo lugar.',
              ),
              const SizedBox(height: 20),
              if (overview.loading) const LinearProgressIndicator(),
              Card(
                child: Column(
                  children: [
                    ActivityDestinationTile(
                      icon: Icons.how_to_reg_outlined,
                      title: 'Mis inscripciones',
                      subtitle: count(
                        overview.registrations,
                        'inscripción',
                        'inscripciones',
                      ),
                      onTap: () => open(const MyRegistrationsScreen()),
                    ),
                    const Divider(height: 1),
                    ActivityDestinationTile(
                      icon: Icons.payments_outlined,
                      title: 'Mis pagos',
                      subtitle: overview.payments == null || overview.loading
                          ? count(overview.payments, 'pago', 'pagos')
                          : '${count(overview.payments, 'pago', 'pagos')} · ${overview.pendingPayments} ${overview.pendingPayments == 1 ? 'pendiente' : 'pendientes'}',
                      onTap: () => open(const MyPaymentsScreen()),
                    ),
                    const Divider(height: 1),
                    ActivityDestinationTile(
                      icon: Icons.fact_check_outlined,
                      title: 'Mis asistencias',
                      subtitle: count(
                        overview.attendance,
                        'registro',
                        'registros',
                      ),
                      onTap: () => open(const MyAttendanceScreen()),
                    ),
                    const Divider(height: 1),
                    ActivityDestinationTile(
                      icon: Icons.workspace_premium_outlined,
                      title: 'Mis certificados',
                      subtitle: count(
                        overview.certificates,
                        'certificado',
                        'certificados',
                      ),
                      onTap: () => open(const MyCertificatesScreen()),
                    ),
                    const Divider(height: 1),
                    ActivityDestinationTile(
                      icon: Icons.history_rounded,
                      title: 'Historial',
                      subtitle: 'Todas tus actividades registradas',
                      onTap: () => open(const HistoryScreen()),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
