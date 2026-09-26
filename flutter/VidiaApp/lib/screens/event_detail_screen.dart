import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/evento.dart';
import '../repositories/evento_repository.dart';
import '../repositories/inscripcion_repository.dart';
import '../widgets/load_error.dart';
import 'registration_confirmation_screen.dart';

class EventDetailScreen extends StatefulWidget {
  const EventDetailScreen({super.key, required this.eventoId});

  final String eventoId;

  @override
  State<EventDetailScreen> createState() => _EventDetailScreenState();
}

class _EventDetailScreenState extends State<EventDetailScreen> {
  Evento? _event;
  bool _loading = true;
  bool _enrolling = false;
  String? _error;

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
      final event =
          await context.read<EventoRepository>().fetchById(widget.eventoId);
      if (mounted) setState(() => _event = event);
    } catch (error) {
      if (mounted) setState(() => _error = error.toString());
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _enroll() async {
    final event = _event;
    if (event == null || !event.esGratuito || _enrolling) return;
    setState(() => _enrolling = true);
    try {
      final registration =
          await context.read<InscripcionRepository>().create(event.id);
      if (!mounted) return;
      await Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => RegistrationConfirmationScreen(
            registration: registration,
          ),
        ),
      );
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(error.toString())),
        );
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
                    enrolling: _enrolling,
                    onEnroll: _enroll,
                  ),
      );
}

class _DetailBody extends StatelessWidget {
  const _DetailBody({
    required this.event,
    required this.enrolling,
    required this.onEnroll,
  });

  final Evento event;
  final bool enrolling;
  final VoidCallback onEnroll;

  @override
  Widget build(BuildContext context) {
    final canEnroll = event.esGratuito &&
        event.requiereInscripcion &&
        !event.sinCupo &&
        event.estado == 'PUBLICADO';
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        Text(event.titulo, style: Theme.of(context).textTheme.headlineSmall),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            Chip(label: Text(event.categoriaNombre)),
            Chip(label: Text(event.modalidad)),
            Chip(label: Text(formatEventPrice(event))),
          ],
        ),
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
            text: formatEventDate(event.fechaInicio)),
        _Info(
          icon: Icons.schedule,
          text: '${event.horaInicio} - ${event.horaFin}',
        ),
        if (event.ubicacion != null)
          _Info(icon: Icons.place_outlined, text: event.ubicacion!),
        if (event.direccion != null)
          _Info(icon: Icons.map_outlined, text: event.direccion!),
        if (event.enlaceVirtual != null)
          const _Info(
            icon: Icons.videocam_outlined,
            text: 'El enlace virtual se encuentra disponible.',
          ),
        if (event.cupoLimitado)
          _Info(
            icon: Icons.groups_outlined,
            text: 'Cupos disponibles: ${event.cupoDisponible ?? 0}',
          ),
        _Info(
          icon: Icons.workspace_premium_outlined,
          text: event.emiteCertificado
              ? 'Emite certificado ${event.tipoCertificado ?? ''}'.trim()
              : 'No emite certificado',
        ),
        const SizedBox(height: 26),
        if (!event.esGratuito)
          const Card(
            child: Padding(
              padding: EdgeInsets.all(16),
              child: Text(
                'Este evento requiere pago. El flujo de pagos estará disponible en una etapa posterior.',
              ),
            ),
          )
        else if (!event.requiereInscripcion)
          const Text('Este evento no requiere inscripción.')
        else if (event.sinCupo)
          const Text('No quedan cupos disponibles para este evento.')
        else
          FilledButton.icon(
            onPressed: canEnroll && !enrolling ? onEnroll : null,
            icon: enrolling
                ? const SizedBox.square(
                    dimension: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.how_to_reg_rounded),
            label: Text(enrolling ? 'Inscribiendo...' : 'Inscribirme'),
          ),
      ],
    );
  }
}

class _Info extends StatelessWidget {
  const _Info({required this.icon, required this.text});

  final IconData icon;
  final String text;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(bottom: 12),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(icon, size: 20),
            const SizedBox(width: 10),
            Expanded(child: Text(text)),
          ],
        ),
      );
}
