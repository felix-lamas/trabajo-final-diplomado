import 'package:flutter/foundation.dart';
import '../models/asistencia.dart';
import '../models/certificado.dart';
import '../models/evento.dart';
import '../models/inscripcion.dart';
import '../models/pago.dart';
import '../repositories/asistencia_repository.dart';
import '../repositories/certificado_repository.dart';
import '../repositories/evento_repository.dart';
import '../repositories/inscripcion_repository.dart';
import '../repositories/pago_repository.dart';

/// Presentation summary; existing services and server retain business rules.
class AccountOverviewController extends ChangeNotifier {
  AccountOverviewController({
    required this.eventsRepository,
    required this.registrationsRepository,
    required this.paymentsRepository,
    required this.attendanceRepository,
    required this.certificatesRepository,
  });
  final EventoRepository eventsRepository;
  final InscripcionRepository registrationsRepository;
  final PagoRepository paymentsRepository;
  final AsistenciaRepository attendanceRepository;
  final CertificadoRepository certificatesRepository;
  bool loading = false;
  bool detailsLoading = false;
  bool _disposed = false;
  final errors = <String>{};
  List<Evento>? events;
  List<Inscripcion>? registrations;
  List<Pago>? payments;
  List<Asistencia>? attendance;
  List<Certificado>? certificates;
  Map<String, Evento> registeredEvents = {};
  List<Evento> attendanceEvents = [];

  Future<T?> _read<T>(String section, Future<T> Function() request) async {
    try {
      return await request().timeout(const Duration(seconds: 30));
    } catch (_) {
      errors.add(section);
      return null;
    }
  }

  Future<void> refresh() async {
    if (loading || detailsLoading || _disposed) return;
    loading = true;
    errors.clear();
    notifyListeners();
    await Future.wait<void>([
      () async {
        events = await _read('Eventos', eventsRepository.fetchPublished);
      }(),
      () async {
        registrations = await _read(
          'Inscripciones',
          registrationsRepository.fetchMine,
        );
      }(),
      () async {
        payments = await _read('Pagos', paymentsRepository.fetchMine);
      }(),
      () async {
        attendance = await _read('Asistencias', attendanceRepository.fetchMine);
      }(),
      () async {
        certificates = await _read(
          'Certificados',
          certificatesRepository.fetchMine,
        );
      }(),
    ]);
    registeredEvents = {};
    attendanceEvents = [];
    loading = false;
    detailsLoading = true;
    if (_disposed) return;
    notifyListeners();
    final ids = (registrations ?? <Inscripcion>[])
        .where((item) => item.estado == 'CONFIRMADA')
        .map((item) => item.eventoId)
        .toSet()
        .toList();
    // Bound concurrent requests for accounts with many registrations.
    for (var offset = 0; offset < ids.length; offset += 4) {
      if (_disposed) return;
      await Future.wait<void>(
        ids.skip(offset).take(4).map((id) async {
          final event = await _read(
            'Próxima actividad',
            () => eventsRepository.fetchById(id),
          );
          if (event != null) registeredEvents[id] = event;
          if (event == null || attendance == null) return;
          final sessions = await _read(
            'Sesiones',
            () => attendanceRepository.fetchSessions(id),
          );
          if (sessions?.any(
                (session) =>
                    session.activa == true &&
                    session.historica != true &&
                    session.requiereAsistencia == true &&
                    !attendance!.any(
                      (item) => item.sesionEventoId == session.id,
                    ),
              ) ??
              false) {
            attendanceEvents.add(event);
          }
        }),
      );
    }
    detailsLoading = false;
    if (!_disposed) notifyListeners();
  }

  List<Evento> get upcomingEvents {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    return (events ?? <Evento>[])
        .where(
          (event) =>
              event.fechaInicio != null && !event.fechaInicio!.isBefore(today),
        )
        .toList()
      ..sort((a, b) => a.fechaInicio!.compareTo(b.fechaInicio!));
  }

  Evento? get nextActivity {
    DateTime start(Evento event) {
      final date = event.fechaInicio!;
      final time = event.horaInicio.split(':');
      return DateTime(
        date.year,
        date.month,
        date.day,
        int.tryParse(time.first) ?? 0,
        time.length > 1 ? int.tryParse(time[1]) ?? 0 : 0,
      );
    }

    final now = DateTime.now();
    final upcoming =
        registeredEvents.values
            .where(
              (event) =>
                  event.fechaInicio != null && !start(event).isBefore(now),
            )
            .toList()
          ..sort((a, b) => start(a).compareTo(start(b)));
    return upcoming.isEmpty ? null : upcoming.first;
  }

  int get pendingPayments => (payments ?? <Pago>[])
      .where(
        (payment) =>
            payment.estado == 'PENDIENTE_PAGO' ||
            payment.estado == 'PENDIENTE_VALIDACION',
      )
      .length;
  int get availableCertificates => (certificates ?? <Certificado>[])
      .where((item) => item.estado == 'GENERADO' || item.estado == 'DESCARGADO')
      .length;

  @override
  void dispose() {
    _disposed = true;
    super.dispose();
  }
}
