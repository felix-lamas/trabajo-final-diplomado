import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/evento.dart';
import '../models/inscripcion.dart';
import '../repositories/evento_repository.dart';
import '../repositories/inscripcion_repository.dart';
import '../services/api_exception.dart';
import '../widgets/load_error.dart';
import 'attendance_sessions_screen.dart';
import 'payment_management_screen.dart';
import 'registration_confirmation_screen.dart';

class EventDetailScreen extends StatefulWidget {
  const EventDetailScreen({super.key, required this.eventoId});
  final String eventoId;

  @override
  State<EventDetailScreen> createState() => _EventDetailScreenState();
}

class _EventDetailScreenState extends State<EventDetailScreen> {
  Evento? _event;
  Inscripcion? _registration;
  bool _loading = true;
  bool _enrolling = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final eventoRepository = context.read<EventoRepository>();
    final inscripcionRepository = context.read<InscripcionRepository>();
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final event = await eventoRepository.fetchById(widget.eventoId);
      Inscripcion? registration;
      try {
        registration = (await inscripcionRepository.fetchMine())
            .where((item) =>
                item.eventoId == event.id && item.estado != 'CANCELADA')
            .firstOrNull;
      } catch (_) {
        // La consulta pública del evento sigue disponible; el backend valida el POST.
      }
      if (mounted) {
        setState(() {
          _event = event;
          _registration = registration;
        });
      }
    } catch (_) {
      if (mounted) {
        setState(() => _error =
            'No se pudo abrir el evento. Puede que ya no esté disponible.');
      }
    } finally {
      if (mounted) {
        setState(() => _loading = false);
      }
    }
  }

  Future<void> _enroll() async {
    final event = _event;
    if (event == null ||
        !event.requiereInscripcion ||
        event.sinCupo ||
        _enrolling ||
        _registration != null) {
      return;
    }
    setState(() => _enrolling = true);
    final repository = context.read<InscripcionRepository>();
    try {
      final registration = await repository.create(event.id);
      if (!mounted) return;
      setState(() => _registration = registration);
      await Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (_) =>
                RegistrationConfirmationScreen(registration: registration),
          ));
    } catch (error) {
      if (error is ApiException && error.statusCode == 409) {
        try {
          final mine = await repository.fetchMine();
          _registration =
              mine.where((item) => item.eventoId == event.id).firstOrNull;
        } catch (_) {}
      }
      if (mounted) {
        final message = error is ApiException
            ? error.message
            : 'No se pudo procesar la inscripción.';
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(message)));
        setState(() {});
      }
    } finally {
      if (mounted) setState(() => _enrolling = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Detalle del evento')),
        body: _loading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
                ? LoadError(message: _error!, onRetry: _load)
                : _DetailBody(
                    event: _event!,
                    registration: _registration,
                    enrolling: _enrolling,
                    onEnroll: _enroll),
      );
}

class _DetailBody extends StatelessWidget {
  const _DetailBody(
      {required this.event,
      required this.registration,
      required this.enrolling,
      required this.onEnroll});
  final Evento event;
  final Inscripcion? registration;
  final bool enrolling;
  final VoidCallback onEnroll;

  @override
  Widget build(BuildContext context) {
    final canEnroll = event.requiereInscripcion &&
        !event.sinCupo &&
        event.estado == 'PUBLICADO' &&
        registration == null;
    return ListView(padding: const EdgeInsets.all(20), children: [
      Text(event.titulo, style: Theme.of(context).textTheme.headlineSmall),
      if (_validImage(event.imagenPortada)) ...[
        const SizedBox(height: 12),
        ClipRRect(
            borderRadius: BorderRadius.circular(16),
            child: Image.network(
              event.imagenPortada!,
              height: 210,
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => const SizedBox.shrink(),
            )),
      ],
      const SizedBox(height: 12),
      Wrap(spacing: 8, runSpacing: 8, children: [
        Chip(label: Text(event.categoriaNombre)),
        Chip(label: Text(event.modalidad)),
        Chip(label: Text(formatEventPrice(event))),
        if (event.publicoObjetivo != null)
          Chip(label: Text(event.publicoObjetivo!)),
      ]),
      const SizedBox(height: 18),
      if (event.descripcion.isNotEmpty) Text(event.descripcion),
      if (event.objetivos.isNotEmpty) ...[
        const SizedBox(height: 18),
        Text('Objetivos', style: Theme.of(context).textTheme.titleMedium),
        const SizedBox(height: 6),
        Text(event.objetivos),
      ],
      const SizedBox(height: 22),
      _Info(
          icon: Icons.calendar_today,
          text:
              '${formatEventDate(event.fechaInicio)} – ${formatEventDate(event.fechaFin)}'),
      _Info(
          icon: Icons.schedule,
          text:
              '${event.horaInicio.isEmpty ? 'Hora por confirmar' : event.horaInicio} – ${event.horaFin}'),
      if (event.ubicacion != null)
        _Info(icon: Icons.place_outlined, text: event.ubicacion!),
      if (event.direccion != null)
        _Info(icon: Icons.map_outlined, text: event.direccion!),
      if (event.enlaceVirtual != null)
        _Info(icon: Icons.videocam_outlined, text: event.enlaceVirtual!),
      if (event.cupoLimitado && event.cupoDisponible != null)
        _Info(
            icon: Icons.groups_outlined,
            text: event.sinCupo
                ? 'Evento sin cupos disponibles'
                : 'Cupos disponibles: ${event.cupoDisponible}'),
      _Info(icon: Icons.info_outline, text: 'Estado: ${event.estado}'),
      if (event.emiteCertificado)
        _Info(
            icon: Icons.workspace_premium_outlined,
            text:
                'Certificado ${event.tipoCertificado ?? ''} ${event.horasAcademicas == null ? '' : '· ${event.horasAcademicas} horas académicas'}'
                    .trim()),
      if (event.emailContacto != null)
        _Info(icon: Icons.email_outlined, text: event.emailContacto!),
      if (event.telefonoContacto != null)
        _Info(icon: Icons.phone_outlined, text: event.telefonoContacto!),
      if (event.whatsappContacto != null)
        _Info(icon: Icons.chat_outlined, text: event.whatsappContacto!),
      if (!event.esGratuito && event.instruccionesPago != null)
        _Info(icon: Icons.payments_outlined, text: event.instruccionesPago!),
      const SizedBox(height: 18),
      if (!event.requiereInscripcion)
        const Text('Este evento no requiere inscripción.')
      else if (event.sinCupo)
        const Text('No quedan cupos disponibles para este evento.')
      else if (registration != null)
        Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Card(
                child: ListTile(
                    leading: const Icon(Icons.how_to_reg),
                    title: const Text('Ya tienes una inscripción'),
                    subtitle: Text(
                        friendlyRegistrationStatus(registration!.estado)))),
            if (!event.esGratuito)
              OutlinedButton.icon(
                onPressed: () => Navigator.push(
                  context,
                  MaterialPageRoute<void>(
                    builder: (_) =>
                        PaymentManagementScreen(registration: registration!),
                  ),
                ),
                icon: const Icon(Icons.payments_outlined),
                label: const Text('Ver estado de pago'),
              ),
            if (registration!.estado == 'CONFIRMADA')
              OutlinedButton.icon(
                onPressed: () => Navigator.push(
                  context,
                  MaterialPageRoute<void>(
                    builder: (_) => AttendanceSessionsScreen(
                      eventoId: event.id,
                      eventoTitulo: event.titulo,
                      eventoModalidad: event.modalidad,
                    ),
                  ),
                ),
                icon: const Icon(Icons.qr_code_scanner_rounded),
                label: const Text('Asistencia y sesiones'),
              ),
          ],
        )
      else
        FilledButton.icon(
          onPressed: canEnroll && !enrolling ? onEnroll : null,
          icon: enrolling
              ? const SizedBox.square(
                  dimension: 18,
                  child: CircularProgressIndicator(strokeWidth: 2))
              : const Icon(Icons.how_to_reg_rounded),
          label: Text(enrolling
              ? 'Inscribiendo...'
              : event.esGratuito
                  ? 'Inscribirme'
                  : 'Solicitar inscripción · ${formatEventPrice(event)}'),
        ),
    ]);
  }

  bool _validImage(String? value) {
    final uri = value == null ? null : Uri.tryParse(value);
    return uri != null && (uri.scheme == 'http' || uri.scheme == 'https');
  }
}

String friendlyRegistrationStatus(String status) => status
    .toLowerCase()
    .split('_')
    .map((part) =>
        part.isEmpty ? part : '${part[0].toUpperCase()}${part.substring(1)}')
    .join(' ');

class _Info extends StatelessWidget {
  const _Info({required this.icon, required this.text});
  final IconData icon;
  final String text;
  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(bottom: 12),
        child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Icon(icon, size: 20),
          const SizedBox(width: 10),
          Expanded(child: Text(text)),
        ]),
      );
}
