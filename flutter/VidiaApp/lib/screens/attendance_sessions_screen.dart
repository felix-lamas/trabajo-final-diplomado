import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/asistencia.dart';
import '../models/sesion_evento.dart';
import '../repositories/asistencia_repository.dart';
import '../services/api_exception.dart';
import '../services/attendance_location_source.dart';
import '../widgets/load_error.dart';
import '../widgets/vidia_empty_state.dart';
import '../widgets/vidia_status_chip.dart';
import 'attendance_scanner_screen.dart';

class AttendanceSessionsScreen extends StatefulWidget {
  const AttendanceSessionsScreen({
    super.key,
    required this.eventoId,
    required this.eventoTitulo,
    required this.eventoModalidad,
  });

  final String eventoId;
  final String eventoTitulo;
  final String eventoModalidad;

  bool _requiresGps(SesionEvento session) =>
      session.requiereGps || eventoModalidad.toUpperCase() != 'VIRTUAL';

  @override
  State<AttendanceSessionsScreen> createState() =>
      _AttendanceSessionsScreenState();
}

class _AttendanceSessionsScreenState extends State<AttendanceSessionsScreen> {
  bool _loading = true;
  String? _error;
  List<SesionEvento> _sessions = const [];
  Map<String, Asistencia> _attendanceBySession = const {};

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    final repository = context.read<AsistenciaRepository>();
    try {
      final results = await Future.wait<Object>([
        repository.fetchSessions(widget.eventoId),
        repository.fetchMine(),
      ]);
      final sessions = results[0] as List<SesionEvento>;
      final sessionIds = sessions.map((item) => item.id).toSet();
      final ownAttendance = results[1] as List<Asistencia>;
      if (mounted) {
        setState(() {
          _sessions = sessions;
          _attendanceBySession = {
            for (final item in ownAttendance)
              if (sessionIds.contains(item.sesionEventoId))
                item.sesionEventoId: item,
          };
        });
      }
    } catch (error) {
      if (mounted) {
        setState(() {
          _error = error is ApiException && error.statusCode == 404
              ? 'No hay sesiones disponibles para esta inscripción.'
              : error is ApiException && error.statusCode == 403
                  ? 'No tienes acceso a las sesiones de este evento.'
                  : error is ApiException && error.statusCode == 401
                      ? 'Tu sesión expiró. Inicia sesión nuevamente.'
                      : 'No se pudieron cargar las sesiones. Inténtalo otra vez.';
        });
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _scan(SesionEvento session) async {
    final result = await Navigator.push<Asistencia>(
      context,
      MaterialPageRoute(
        builder: (_) => AttendanceScannerScreen(
          session: session,
          requiresGps: widget._requiresGps(session),
          repository: context.read<AsistenciaRepository>(),
          locationSource: context.read<AttendanceLocationSource>(),
        ),
      ),
    );
    if (!mounted) return;
    if (result != null) {
      await _load();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Asistencia registrada correctamente.')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Asistencia')),
        body: _loading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
                ? LoadError(message: _error!, onRetry: _load)
                : RefreshIndicator(
                    onRefresh: _load,
                    child: _sessions.isEmpty
                        ? ListView(
                            children: [
                              const SizedBox(height: 140),
                              VidiaEmptyState(
                                icon: Icons.qr_code_scanner_rounded,
                                message:
                                    'Todavía no hay sesiones de asistencia para ${widget.eventoTitulo}.',
                              ),
                            ],
                          )
                        : ListView(
                            padding: const EdgeInsets.all(20),
                            children: [
                              Text(widget.eventoTitulo,
                                  style:
                                      Theme.of(context).textTheme.titleLarge),
                              const SizedBox(height: 8),
                              const Text(
                                'El código temporal se muestra durante la sesión. El servidor valida el código, tu inscripción y, si corresponde, la ubicación.',
                              ),
                              const SizedBox(height: 18),
                              for (final session in _sessions) ...[
                                _SessionCard(
                                  session: session,
                                  attendance: _attendanceBySession[session.id],
                                  onScan: () => _scan(session),
                                ),
                                const SizedBox(height: 12),
                              ],
                            ],
                          ),
                  ),
      );
}

class _SessionCard extends StatelessWidget {
  const _SessionCard({
    required this.session,
    required this.attendance,
    required this.onScan,
  });

  final SesionEvento session;
  final Asistencia? attendance;
  final VoidCallback onScan;

  @override
  Widget build(BuildContext context) {
    final available = session.activa == true && session.historica != true;
    final needsAttendance = session.requiereAsistencia == true;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(session.nombre,
                style: Theme.of(context).textTheme.titleMedium),
            if (session.fecha != null || session.horaInicio.isNotEmpty) ...[
              const SizedBox(height: 6),
              Text(_sessionSchedule(session)),
            ],
            if (session.descripcion != null) ...[
              const SizedBox(height: 8),
              Text(session.descripcion!),
            ],
            const SizedBox(height: 12),
            if (attendance != null)
              Row(
                children: [
                  const VidiaStatusChip(
                    label: 'Registrada',
                    kind: VidiaStatusKind.success,
                  ),
                  if (attendance!.fechaHoraRegistro != null) ...[
                    const SizedBox(width: 10),
                    Expanded(
                        child: Text(_dateTime(attendance!.fechaHoraRegistro!))),
                  ],
                ],
              )
            else if (!needsAttendance)
              const Text('Esta sesión no requiere registrar asistencia.')
            else if (!available)
              const Text(
                  'Esta sesión no está disponible para registrar asistencia.')
            else
              SizedBox(
                width: double.infinity,
                child: FilledButton.icon(
                  onPressed: onScan,
                  icon: const Icon(Icons.qr_code_scanner_rounded),
                  label: const Text('Escanear código de asistencia'),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

String _sessionSchedule(SesionEvento session) {
  final date = session.fecha;
  final dateText = date == null
      ? ''
      : '${date.day.toString().padLeft(2, '0')}/'
          '${date.month.toString().padLeft(2, '0')}/${date.year}';
  final timeText = session.horaInicio.isEmpty
      ? ''
      : '${session.horaInicio} – ${session.horaFin}';
  return [dateText, timeText].where((part) => part.isNotEmpty).join(' · ');
}

String _dateTime(DateTime value) => '${value.day.toString().padLeft(2, '0')}/'
    '${value.month.toString().padLeft(2, '0')}/${value.year} '
    '${value.hour.toString().padLeft(2, '0')}:${value.minute.toString().padLeft(2, '0')}';
