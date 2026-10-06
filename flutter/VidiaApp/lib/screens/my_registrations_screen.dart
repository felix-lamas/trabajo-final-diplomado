import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/inscripcion.dart';
import '../repositories/inscripcion_repository.dart';
import '../services/api_exception.dart';
import '../widgets/load_error.dart';
import '../widgets/vidia_empty_state.dart';
import '../widgets/vidia_status_chip.dart';
import 'event_detail_screen.dart';

class MyRegistrationsScreen extends StatefulWidget {
  const MyRegistrationsScreen({super.key});

  @override
  State<MyRegistrationsScreen> createState() => _MyRegistrationsScreenState();
}

class _MyRegistrationsScreenState extends State<MyRegistrationsScreen> {
  bool _loading = true;
  String? _error;
  List<Inscripcion> _registrations = const [];

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
      final registrations =
          await context.read<InscripcionRepository>().fetchMine();
      if (mounted) setState(() => _registrations = registrations);
    } catch (error) {
      if (mounted) {
        setState(() => _error = error is ApiException
            ? error.message
            : 'No se pudieron cargar tus inscripciones.');
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _cancel(Inscripcion registration) async {
    final repository = context.read<InscripcionRepository>();
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Cancelar inscripción'),
        content: Text(
            '¿Quieres cancelar tu inscripción a "${registration.eventoTitulo}"?'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('Volver')),
          FilledButton(
              onPressed: () => Navigator.pop(context, true),
              child: const Text('Cancelar inscripción')),
        ],
      ),
    );
    if (confirm != true || !mounted) return;
    try {
      await repository.cancel(registration.id);
      await _load();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Inscripción cancelada.')));
      }
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
            content: Text('No se pudo cancelar la inscripción.')));
      }
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Mis inscripciones')),
        body: _loading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
                ? LoadError(message: _error!, onRetry: _load)
                : RefreshIndicator(
                    onRefresh: _load,
                    child: _registrations.isEmpty
                        ? ListView(
                            children: const [
                              SizedBox(height: 140),
                              VidiaEmptyState(
                                icon: Icons.how_to_reg_outlined,
                                message: 'Todavía no tienes inscripciones.',
                              ),
                            ],
                          )
                        : ListView.separated(
                            padding: const EdgeInsets.all(16),
                            itemCount: _registrations.length,
                            separatorBuilder: (_, __) =>
                                const SizedBox(height: 10),
                            itemBuilder: (_, index) {
                              final registration = _registrations[index];
                              return Card(
                                child: ListTile(
                                  contentPadding: const EdgeInsets.all(16),
                                  leading: const CircleAvatar(
                                    child: Icon(Icons.event_available_outlined),
                                  ),
                                  title: Text(registration.eventoTitulo),
                                  subtitle: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      const SizedBox(height: 8),
                                      Text(
                                        'Inscripción: ${_formatDate(registration.fechaInscripcion)}',
                                      ),
                                      const SizedBox(height: 8),
                                      VidiaStatusChip(
                                        label: _friendlyStatus(
                                          registration.estado,
                                        ),
                                        kind: _statusKind(registration.estado),
                                      ),
                                    ],
                                  ),
                                  trailing: registration.estado == 'CANCELADA'
                                      ? const Icon(Icons.chevron_right_rounded)
                                      : PopupMenuButton<String>(
                                          onSelected: (_) =>
                                              _cancel(registration),
                                          itemBuilder: (_) => const [
                                            PopupMenuItem(
                                                value: 'cancel',
                                                child: Text(
                                                    'Cancelar inscripción'))
                                          ],
                                        ),
                                  onTap: () async {
                                    await Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                        builder: (_) => EventDetailScreen(
                                          eventoId: registration.eventoId,
                                        ),
                                      ),
                                    );
                                    if (mounted) await _load();
                                  },
                                ),
                              );
                            },
                          ),
                  ),
      );
}

String _formatDate(DateTime? date) {
  if (date == null) return 'Sin fecha';
  return '${date.day.toString().padLeft(2, '0')}/'
      '${date.month.toString().padLeft(2, '0')}/${date.year} '
      '${date.hour.toString().padLeft(2, '0')}:'
      '${date.minute.toString().padLeft(2, '0')}';
}

String _friendlyStatus(String status) => status
    .toLowerCase()
    .split('_')
    .map((part) =>
        part.isEmpty ? part : '${part[0].toUpperCase()}${part.substring(1)}')
    .join(' ');

VidiaStatusKind _statusKind(String status) => switch (status) {
      'CONFIRMADA' => VidiaStatusKind.success,
      'CANCELADA' => VidiaStatusKind.error,
      'PENDIENTE_PAGO' || 'PENDIENTE_VALIDACION' => VidiaStatusKind.warning,
      _ => VidiaStatusKind.info,
    };
