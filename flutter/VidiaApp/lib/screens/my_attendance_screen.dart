import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/asistencia.dart';
import '../models/inscripcion.dart';
import '../repositories/asistencia_repository.dart';
import '../repositories/evento_repository.dart';
import '../repositories/inscripcion_repository.dart';
import '../widgets/load_error.dart';
import '../widgets/vidia_empty_state.dart';
import 'attendance_sessions_screen.dart';

class MyAttendanceScreen extends StatefulWidget {
  const MyAttendanceScreen({super.key});
  @override
  State<MyAttendanceScreen> createState() => _MyAttendanceScreenState();
}

class _MyAttendanceScreenState extends State<MyAttendanceScreen> {
  late Future<List<Asistencia>> _data;
  @override
  void initState() {
    super.initState();
    _data = context.read<AsistenciaRepository>().fetchMine();
  }

  Future<void> _refresh() async {
    final future = context.read<AsistenciaRepository>().fetchMine();
    setState(() {
      _data = future;
    });
    try {
      await future;
    } catch (_) {
      /* FutureBuilder presents the error. */
    }
  }

  Future<void> _register() async {
    await Navigator.push(
      context,
      MaterialPageRoute<void>(
        builder: (_) => const AttendanceRegistrationScreen(),
      ),
    );
    if (mounted) await _refresh();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Mis asistencias')),
    body: SafeArea(
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: SizedBox(
              width: double.infinity,
              child: FilledButton.icon(
                onPressed: _register,
                icon: const Icon(Icons.qr_code_scanner_rounded),
                label: const Text('Registrar asistencia'),
              ),
            ),
          ),
          Expanded(
            child: FutureBuilder<List<Asistencia>>(
              future: _data,
              builder: (context, snapshot) {
                if (snapshot.connectionState != ConnectionState.done) {
                  return const Center(child: CircularProgressIndicator());
                }
                if (snapshot.hasError) {
                  return LoadError(
                    message: 'No pudimos consultar tus asistencias.',
                    onRetry: _refresh,
                  );
                }
                // The current response provides an event label, not an event identifier.
                // Group display labels only; never infer event IDs from these names.
                final groups = <String, List<Asistencia>>{};
                final records = [...snapshot.data!]
                  ..sort(
                    (a, b) => (b.fechaHoraRegistro ?? DateTime(0)).compareTo(
                      a.fechaHoraRegistro ?? DateTime(0),
                    ),
                  );
                for (final item in records) {
                  (groups[item.evento] ??= []).add(item);
                }
                return RefreshIndicator(
                  onRefresh: _refresh,
                  child: ListView(
                    physics: const AlwaysScrollableScrollPhysics(),
                    padding: const EdgeInsets.fromLTRB(20, 0, 20, 24),
                    children: [
                      if (groups.isEmpty)
                        const VidiaEmptyState(
                          icon: Icons.fact_check_outlined,
                          message: 'Todavía no tienes asistencias registradas.',
                        ),
                      for (final group in groups.entries) ...[
                        Padding(
                          padding: const EdgeInsets.symmetric(vertical: 12),
                          child: Text(
                            group.key,
                            style: Theme.of(context).textTheme.titleLarge,
                          ),
                        ),
                        Card(
                          child: Column(
                            children: [
                              for (final item in group.value)
                                ListTile(
                                  leading: const Icon(
                                    Icons.fact_check_outlined,
                                  ),
                                  title: Text(item.sesion),
                                  subtitle: Text(
                                    '${item.resultadoValidacion}\n${item.fechaHoraRegistro == null ? 'Sin fecha de registro' : _date(item.fechaHoraRegistro!)}${item.observacion == null ? '' : '\n${item.observacion}'}',
                                  ),
                                ),
                            ],
                          ),
                        ),
                      ],
                    ],
                  ),
                );
              },
            ),
          ),
        ],
      ),
    ),
  );
}

class AttendanceRegistrationScreen extends StatefulWidget {
  const AttendanceRegistrationScreen({super.key});
  @override
  State<AttendanceRegistrationScreen> createState() =>
      _AttendanceRegistrationScreenState();
}

class _AttendanceRegistrationScreenState
    extends State<AttendanceRegistrationScreen> {
  late Future<List<Inscripcion>> _data;
  String? _opening;
  @override
  void initState() {
    super.initState();
    _data = context.read<InscripcionRepository>().fetchMine();
  }

  Future<void> _refresh() async {
    final future = context.read<InscripcionRepository>().fetchMine();
    setState(() {
      _data = future;
    });
    try {
      await future;
    } catch (_) {
      /* FutureBuilder presents the error. */
    }
  }

  Future<void> _open(Inscripcion registration) async {
    setState(() => _opening = registration.id);
    try {
      final event = await context.read<EventoRepository>().fetchById(
        registration.eventoId,
      );
      if (!mounted) return;
      await Navigator.push(
        context,
        MaterialPageRoute<void>(
          builder: (_) => AttendanceSessionsScreen(
            eventoId: event.id,
            eventoTitulo: event.titulo,
            eventoModalidad: event.modalidad,
          ),
        ),
      );
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text(
              'No pudimos abrir las sesiones. Inténtalo nuevamente.',
            ),
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _opening = null);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Registrar asistencia')),
    body: SafeArea(
      child: FutureBuilder<List<Inscripcion>>(
        future: _data,
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return LoadError(
              message: 'No pudimos consultar tus inscripciones.',
              onRetry: _refresh,
            );
          }
          final registrations = snapshot.data!
              .where((item) => item.estado == 'CONFIRMADA')
              .toList();
          return RefreshIndicator(
            onRefresh: _refresh,
            child: ListView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.all(20),
              children: [
                const Text(
                  'Elige tu evento para consultar las sesiones y abrir el lector QR cuando la asistencia esté disponible.',
                ),
                const SizedBox(height: 16),
                if (registrations.isEmpty)
                  const VidiaEmptyState(
                    icon: Icons.event_note_outlined,
                    message:
                        'Necesitas una inscripción confirmada para registrar asistencia.',
                  ),
                for (final registration in registrations)
                  Card(
                    child: ListTile(
                      leading: const Icon(Icons.event_available_outlined),
                      title: Text(registration.eventoTitulo),
                      subtitle: const Text('Consultar sesiones'),
                      trailing: _opening == registration.id
                          ? const SizedBox(
                              width: 24,
                              height: 24,
                              child: CircularProgressIndicator(),
                            )
                          : const Icon(Icons.chevron_right_rounded),
                      onTap: _opening == null
                          ? () => _open(registration)
                          : null,
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

String _date(DateTime date) =>
    '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year} ${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
