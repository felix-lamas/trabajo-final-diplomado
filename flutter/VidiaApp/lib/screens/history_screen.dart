import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/asistencia.dart';
import '../models/certificado.dart';
import '../models/inscripcion.dart';
import '../models/pago.dart';
import '../repositories/asistencia_repository.dart';
import '../repositories/certificado_repository.dart';
import '../repositories/inscripcion_repository.dart';
import '../repositories/pago_repository.dart';
import '../services/api_exception.dart';
import '../widgets/load_error.dart';
import '../widgets/vidia_empty_state.dart';
import '../widgets/vidia_status_chip.dart';
import 'certificate_detail_screen.dart';
import 'event_detail_screen.dart';
import 'payment_management_screen.dart';

enum _ActivityKind { registration, payment, certificate, attendance }

class _Activity {
  const _Activity({
    required this.kind,
    required this.title,
    required this.description,
    required this.status,
    this.date,
    this.registration,
    this.certificate,
  });

  final _ActivityKind kind;
  final String title;
  final String description;
  final String status;
  final DateTime? date;
  final Inscripcion? registration;
  final Certificado? certificate;
}

class HistoryScreen extends StatefulWidget {
  const HistoryScreen({super.key});

  @override
  State<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends State<HistoryScreen> {
  bool _loading = true;
  String? _error;
  List<_Activity> _activities = const [];

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
    try {
      final results = await Future.wait<Object>([
        context.read<InscripcionRepository>().fetchMine(),
        context.read<PagoRepository>().fetchMine(),
        context.read<CertificadoRepository>().fetchMine(),
        context.read<AsistenciaRepository>().fetchMine(),
      ]);
      final registrations = results[0] as List<Inscripcion>;
      final payments = results[1] as List<Pago>;
      final certificates = results[2] as List<Certificado>;
      final attendance = results[3] as List<Asistencia>;
      final registrationsById = {
        for (final value in registrations) value.id: value
      };
      final activities = <_Activity>[
        for (final value in registrations)
          _Activity(
            kind: _ActivityKind.registration,
            title: value.eventoTitulo,
            description: 'Inscripción',
            status: value.estado,
            date: value.fechaInscripcion,
            registration: value,
          ),
        for (final value in payments)
          _Activity(
            kind: _ActivityKind.payment,
            title: value.eventoTitulo,
            description: 'Pago · Bs. ${value.monto.toStringAsFixed(2)}',
            status: value.estado,
            date: value.fechaPago ?? value.fechaResolucion,
            registration: registrationsById[value.inscripcionId],
          ),
        for (final value in certificates)
          _Activity(
            kind: _ActivityKind.certificate,
            title: value.evento,
            description:
                'Certificado · ${_friendlyType(value.tipoCertificado)}',
            status: value.estado,
            date: value.fechaEmision,
            certificate: value,
          ),
        for (final value in attendance)
          _Activity(
            kind: _ActivityKind.attendance,
            title: value.evento,
            description: 'Asistencia · ${value.sesion}',
            status: value.resultadoValidacion,
            date: value.fechaHoraRegistro,
          ),
      ]..sort((a, b) {
          if (a.date == null) return b.date == null ? 0 : 1;
          if (b.date == null) return -1;
          return b.date!.compareTo(a.date!);
        });
      if (mounted) setState(() => _activities = activities);
    } catch (error) {
      if (mounted) {
        setState(() => _error = error is ApiException
            ? error.message
            : 'No se pudo cargar tu historial. Inténtalo nuevamente.');
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _open(_Activity item) async {
    Widget? page;
    switch (item.kind) {
      case _ActivityKind.registration:
        final registration = item.registration;
        if (registration != null) {
          page = EventDetailScreen(eventoId: registration.eventoId);
        }
      case _ActivityKind.payment:
        final registration = item.registration;
        if (registration != null) {
          page = PaymentManagementScreen(registration: registration);
        }
      case _ActivityKind.certificate:
        final certificate = item.certificate;
        if (certificate != null) {
          page = CertificateDetailScreen(certificadoId: certificate.id);
        }
      case _ActivityKind.attendance:
        break;
    }
    if (page == null || !mounted) return;
    await Navigator.push<void>(
      context,
      MaterialPageRoute<void>(builder: (_) => page!),
    );
    if (mounted) await _load();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Historial')),
        body: _loading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
                ? LoadError(message: _error!, onRetry: _load)
                : RefreshIndicator(
                    onRefresh: _load,
                    child: _activities.isEmpty
                        ? ListView(
                            children: const [
                              SizedBox(height: 140),
                              VidiaEmptyState(
                                icon: Icons.history_rounded,
                                message: 'Sin actividad registrada.',
                              ),
                            ],
                          )
                        : ListView.separated(
                            padding: const EdgeInsets.all(16),
                            itemCount: _activities.length,
                            separatorBuilder: (_, __) =>
                                const SizedBox(height: 8),
                            itemBuilder: (context, index) {
                              final item = _activities[index];
                              return Card(
                                child: ListTile(
                                  contentPadding: const EdgeInsets.all(14),
                                  leading: CircleAvatar(
                                    child: Icon(_icon(item.kind)),
                                  ),
                                  title: Text(item.title),
                                  subtitle: Padding(
                                    padding: const EdgeInsets.only(top: 6),
                                    child: Column(
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        Text(item.description),
                                        if (item.date != null)
                                          Text(_dateTime(item.date!)),
                                        const SizedBox(height: 7),
                                        VidiaStatusChip(
                                          label: item.status,
                                          kind: _statusKind(item),
                                        ),
                                      ],
                                    ),
                                  ),
                                  trailing: _isNavigable(item)
                                      ? const Icon(Icons.chevron_right_rounded)
                                      : null,
                                  onTap: _isNavigable(item)
                                      ? () => _open(item)
                                      : null,
                                ),
                              );
                            },
                          ),
                  ),
      );
}

bool _isNavigable(_Activity item) =>
    item.registration != null || item.certificate != null;

IconData _icon(_ActivityKind kind) => switch (kind) {
      _ActivityKind.registration => Icons.event_available_outlined,
      _ActivityKind.payment => Icons.payments_outlined,
      _ActivityKind.certificate => Icons.workspace_premium_outlined,
      _ActivityKind.attendance => Icons.fact_check_outlined,
    };

VidiaStatusKind _statusKind(_Activity item) => switch (item.kind) {
      _ActivityKind.registration => switch (item.status) {
          'CONFIRMADA' => VidiaStatusKind.success,
          'CANCELADA' => VidiaStatusKind.error,
          'PENDIENTE_PAGO' || 'PENDIENTE_VALIDACION' => VidiaStatusKind.warning,
          _ => VidiaStatusKind.info,
        },
      _ActivityKind.payment => switch (item.status) {
          'APROBADO' => VidiaStatusKind.success,
          'RECHAZADO' => VidiaStatusKind.error,
          'PENDIENTE_PAGO' || 'PENDIENTE_VALIDACION' => VidiaStatusKind.warning,
          _ => VidiaStatusKind.info,
        },
      _ActivityKind.certificate => switch (item.status) {
          'GENERADO' || 'DESCARGADO' => VidiaStatusKind.success,
          'ANULADO' => VidiaStatusKind.error,
          _ => VidiaStatusKind.info,
        },
      _ActivityKind.attendance => item.status == 'VALIDADA'
          ? VidiaStatusKind.success
          : VidiaStatusKind.info,
    };

String _friendlyType(String type) => switch (type) {
      'CURRICULAR' => 'Curricular',
      'NO_CURRICULAR' => 'No curricular',
      _ => type,
    };

String _dateTime(DateTime value) => '${value.day.toString().padLeft(2, '0')}/'
    '${value.month.toString().padLeft(2, '0')}/${value.year} '
    '${value.hour.toString().padLeft(2, '0')}:${value.minute.toString().padLeft(2, '0')}';
