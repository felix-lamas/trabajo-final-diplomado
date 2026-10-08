import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../controllers/account_overview_controller.dart';
import '../controllers/session_controller.dart';
import '../models/evento.dart';
import '../widgets/activity_destination_tile.dart';
import '../widgets/vidia_logo.dart';
import 'attendance_sessions_screen.dart';
import 'event_detail_screen.dart';
import 'my_certificates_screen.dart';
import 'my_payments_screen.dart';
import 'my_registrations_screen.dart';

class DashboardScreen extends StatelessWidget {
  const DashboardScreen({super.key, required this.onDiscover});
  final VoidCallback onDiscover;
  @override
  Widget build(BuildContext context) {
    final overview = context.watch<AccountOverviewController>();
    final user = context.watch<SessionController>().user;
    final next = overview.nextActivity;
    final upcoming = overview.upcomingEvents;
    final published = upcoming.isEmpty
        ? overview.events ?? <Evento>[]
        : upcoming;
    Future<void> open(Widget screen) async {
      await Navigator.push(
        context,
        MaterialPageRoute<void>(builder: (_) => screen),
      );
      if (context.mounted) await overview.refresh();
    }

    return Scaffold(
      appBar: AppBar(
        title: const VidiaLogo(height: 38),
        automaticallyImplyLeading: false,
      ),
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: overview.refresh,
          child: ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.fromLTRB(20, 16, 20, 28),
            children: [
              Text(
                'Hola, ${user?.nombres ?? 'participante'}',
                style: Theme.of(context).textTheme.headlineMedium,
              ),
              const SizedBox(height: 8),
              const Text('Descubre los próximos eventos de Vidia.'),
              if (user?.organizadorPendiente ?? false) ...[
                const SizedBox(height: 16),
                const Card(
                  child: ListTile(
                    leading: Icon(Icons.hourglass_top_rounded),
                    title: Text('Solicitud de organizador pendiente'),
                    subtitle: Text(
                      'Tu cuenta conserva el rol Usuario hasta que el administrador resuelva la solicitud.',
                    ),
                  ),
                ),
              ],
              const SizedBox(height: 24),
              Text(
                'Tu próxima actividad',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 8),
            if (overview.loading || overview.detailsLoading)
                const LinearProgressIndicator()
              else if (overview.registrations == null ||
                  overview.errors.contains('Próxima actividad'))
                Card(
                  child: ActivityDestinationTile(
                    icon: Icons.cloud_off_outlined,
                    title: 'No pudimos consultar tu próxima actividad',
                    subtitle:
                        'Consulta tus inscripciones o desliza para actualizar.',
                    onTap: () => open(const MyRegistrationsScreen()),
                  ),
                )
              else if (next != null)
                Card(
                  child: ActivityDestinationTile(
                    icon: Icons.event_available_outlined,
                    title: next.titulo,
                    subtitle:
                        '${formatEventDate(next.fechaInicio)} · ${next.horaInicio}',
                    onTap: () => open(EventDetailScreen(eventoId: next.id)),
                  ),
                )
              else
                Card(
                  child: ActivityDestinationTile(
                    icon: Icons.event_note_outlined,
                    title: 'Sin actividades próximas confirmadas',
                    subtitle: 'Explora eventos o revisa tus inscripciones.',
                    onTap: onDiscover,
                  ),
                ),
              if (!overview.loading &&
                  (overview.pendingPayments > 0 ||
                      overview.availableCertificates > 0 ||
                      overview.attendanceEvents.isNotEmpty)) ...[
                const SizedBox(height: 20),
                Text('Para ti', style: Theme.of(context).textTheme.titleLarge),
                const SizedBox(height: 8),
                Card(
                  child: Column(
                    children: [
                      if (overview.pendingPayments > 0)
                        ActivityDestinationTile(
                          icon: Icons.payments_outlined,
                          title: 'Tienes pagos pendientes',
                          subtitle:
                              '${overview.pendingPayments} por completar o validar',
                          onTap: () => open(const MyPaymentsScreen()),
                        ),
                      if (overview.attendanceEvents.isNotEmpty)
                        ActivityDestinationTile(
                          icon: Icons.qr_code_scanner_rounded,
                          title: 'Asistencia disponible',
                          subtitle: overview.attendanceEvents.first.titulo,
                          onTap: () {
                            final event = overview.attendanceEvents.first;
                            open(
                              AttendanceSessionsScreen(
                                eventoId: event.id,
                                eventoTitulo: event.titulo,
                                eventoModalidad: event.modalidad,
                              ),
                            );
                          },
                        ),
                      if (overview.availableCertificates > 0)
                        ActivityDestinationTile(
                          icon: Icons.workspace_premium_outlined,
                          title: 'Certificados disponibles',
                          subtitle:
                              '${overview.availableCertificates} para consultar',
                          onTap: () => open(const MyCertificatesScreen()),
                        ),
                    ],
                  ),
                ),
              ],
              const SizedBox(height: 24),
              Text(
                upcoming.isEmpty ? 'Eventos publicados' : 'Próximos eventos',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 8),
              if (overview.loading)
                const Padding(
                  padding: EdgeInsets.all(16),
                  child: Center(child: CircularProgressIndicator()),
                )
              else if (overview.events == null)
                Card(
                  child: ActivityDestinationTile(
                    icon: Icons.cloud_off_outlined,
                    title: 'No pudimos cargar los eventos',
                    subtitle: 'Toca para consultar el catálogo.',
                    onTap: onDiscover,
                  ),
                )
              else if (published.isEmpty)
                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 20),
                  child: Text(
                    'No hay eventos publicados por el momento. Vuelve pronto.',
                  ),
                )
              else
                for (final event in published.take(3))
                  Card(
                    child: ActivityDestinationTile(
                      icon: Icons.event_outlined,
                      title: event.titulo,
                      subtitle:
                          '${formatEventDate(event.fechaInicio)} · ${event.modalidad}\n${formatEventPrice(event)}',
                      onTap: () => open(EventDetailScreen(eventoId: event.id)),
                    ),
                  ),
              const SizedBox(height: 8),
              OutlinedButton.icon(
                onPressed: onDiscover,
                icon: const Icon(Icons.explore_outlined),
                label: const Text('Explorar eventos'),
              ),
              if (!overview.loading && overview.errors.isNotEmpty) ...[
                const SizedBox(height: 12),
                Text(
                  'Parte del resumen no está disponible: ${overview.errors.join(', ')}. Desliza para reintentar.',
                  style: Theme.of(context).textTheme.bodySmall,
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
