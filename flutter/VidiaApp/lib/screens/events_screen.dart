import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/evento.dart';
import '../repositories/evento_repository.dart';
import '../widgets/load_error.dart';
import '../widgets/vidia_empty_state.dart';
import 'event_detail_screen.dart';

class EventsScreen extends StatefulWidget {
  const EventsScreen({super.key});

  @override
  State<EventsScreen> createState() => _EventsScreenState();
}

class _EventsScreenState extends State<EventsScreen> {
  bool _loading = true;
  String? _error;
  List<Evento> _events = const [];

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
      final events = await context.read<EventoRepository>().fetchPublished();
      if (mounted) setState(() => _events = events);
    } catch (error) {
      if (mounted) setState(() => _error = error.toString());
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Eventos publicados')),
        body: _loading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
                ? LoadError(message: _error!, onRetry: _load)
                : RefreshIndicator(
                    onRefresh: _load,
                    child: _events.isEmpty
                        ? ListView(
                            children: const [
                              SizedBox(height: 140),
                              VidiaEmptyState(
                                icon: Icons.event_busy_outlined,
                                message:
                                    'No hay eventos publicados por el momento.',
                              ),
                            ],
                          )
                        : ListView.separated(
                            padding: const EdgeInsets.all(16),
                            itemCount: _events.length,
                            separatorBuilder: (_, __) =>
                                const SizedBox(height: 12),
                            itemBuilder: (_, index) =>
                                _EventCard(event: _events[index]),
                          ),
                  ),
      );
}

class _EventCard extends StatelessWidget {
  const _EventCard({required this.event});

  final Evento event;

  @override
  Widget build(BuildContext context) => Card(
        clipBehavior: Clip.antiAlias,
        child: InkWell(
          onTap: () => Navigator.push(
            context,
            MaterialPageRoute(
              builder: (_) => EventDetailScreen(eventoId: event.id),
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (_validImage(event.imagenPortada))
                Image.network(
                  event.imagenPortada!,
                  height: 150,
                  width: double.infinity,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => const SizedBox.shrink(),
                ),
              Padding(
                padding: const EdgeInsets.all(18),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: [
                        Chip(label: Text(event.categoriaNombre)),
                        Chip(label: Text(formatEventPrice(event))),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Text(
                      event.titulo,
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    if (event.descripcion.isNotEmpty) ...[
                      const SizedBox(height: 8),
                      Text(
                        event.descripcion,
                        maxLines: 3,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                    const SizedBox(height: 14),
                    Text(
                      '${formatEventDate(event.fechaInicio)} · '
                      '${event.horaInicio.isEmpty ? 'Hora por confirmar' : event.horaInicio} · '
                      '${event.modalidad}',
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      );

  bool _validImage(String? value) {
    final uri = value == null ? null : Uri.tryParse(value);
    return uri != null && (uri.scheme == 'https' || uri.scheme == 'http');
  }
}
