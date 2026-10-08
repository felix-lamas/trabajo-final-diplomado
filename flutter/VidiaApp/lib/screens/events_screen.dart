import 'dart:async';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/evento.dart';
import '../models/categoria_evento.dart';
import '../repositories/categoria_repository.dart';
import '../repositories/evento_repository.dart';
import '../widgets/load_error.dart';
import '../widgets/vidia_empty_state.dart';
import 'event_detail_screen.dart';

class EventsScreen extends StatefulWidget {
  const EventsScreen({super.key, this.embedded = false});
  final bool embedded;

  @override
  State<EventsScreen> createState() => _EventsScreenState();
}

class _EventsScreenState extends State<EventsScreen> {
  bool _loading = true;
  String? _error;
  List<Evento> _events = const [];
  List<CategoriaEvento> _categories = const [];
  final _search = TextEditingController();
  Timer? _debounce;
  String? _categoryId;
  String? _type;
  String? _modality;
  int _requestId = 0;

  @override
  void initState() {
    super.initState();
    _load();
    _loadCategories();
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _search.dispose();
    super.dispose();
  }

  Future<void> _loadCategories() async {
    final repository = context.read<CategoriaRepository>();
    try {
      final categories = await repository.fetchActive();
      if (mounted) setState(() => _categories = categories);
    } catch (_) {
      // El listado por nombre de categoría en los eventos sigue disponible.
    }
  }

  Future<void> _load() async {
    final requestId = ++_requestId;
    final repository = context.read<EventoRepository>();
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final events =
          _search.text.trim().isEmpty &&
              _categoryId == null &&
              _type == null &&
              _modality == null
          ? await repository.fetchPublished()
          : await repository.searchPublished(
              text: _search.text,
              categoryId: _categoryId,
              type: _type,
              modality: _modality,
            );
      if (mounted && requestId == _requestId) setState(() => _events = events);
    } catch (error) {
      if (mounted && requestId == _requestId) {
        setState(() => _error = 'No se pudieron cargar los eventos.');
      }
    } finally {
      if (mounted && requestId == _requestId) {
        setState(() => _loading = false);
      }
    }
  }

  void _scheduleSearch() {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 350), _load);
  }

  void _clearFilters() {
    _search.clear();
    setState(() {
      _categoryId = null;
      _type = null;
      _modality = null;
    });
    _load();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(
      title: const Text('Eventos'),
      automaticallyImplyLeading: !widget.embedded,
    ),
    body: SafeArea(
      child: RefreshIndicator(
        onRefresh: _load,
        child: CustomScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          slivers: [
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(20, 12, 20, 8),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Encuentra tu próximo evento',
                      style: Theme.of(context).textTheme.headlineSmall,
                    ),
                    const SizedBox(height: 16),
                    TextField(
                      controller: _search,
                      onChanged: (_) {
                        setState(() {});
                        _scheduleSearch();
                      },
                      decoration: InputDecoration(
                        prefixIcon: const Icon(Icons.search),
                        hintText: 'Buscar eventos',
                        suffixIcon: _search.text.isEmpty
                            ? null
                            : IconButton(
                                tooltip: 'Limpiar búsqueda',
                                onPressed: () {
                                  _search.clear();
                                  _load();
                                },
                                icon: const Icon(Icons.close),
                              ),
                      ),
                    ),
                    const SizedBox(height: 8),
                    ExpansionTile(
                      tilePadding: EdgeInsets.zero,
                      title: Text(
                        _hasFilters ? 'Filtros aplicados' : 'Filtrar eventos',
                      ),
                      leading: const Icon(Icons.tune_rounded),
                      children: [
                        LayoutBuilder(
                          builder: (context, constraints) => Wrap(
                            spacing: 12,
                            runSpacing: 12,
                            children: [
                              SizedBox(
                                width: constraints.maxWidth > 600
                                    ? (constraints.maxWidth - 24) / 3
                                    : constraints.maxWidth,
                                child: DropdownButtonFormField<String>(
                                  initialValue: _categoryId,
                                  isExpanded: true,
                                  key: ValueKey('category-$_categoryId'),
                                  decoration: const InputDecoration(
                                    labelText: 'Categoría',
                                  ),
                                  items: [
                                    const DropdownMenuItem(
                                      value: null,
                                      child: Text('Todas'),
                                    ),
                                    ..._categories.map(
                                      (category) => DropdownMenuItem(
                                        value: category.id,
                                        child: Text(category.nombre),
                                      ),
                                    ),
                                  ],
                                  onChanged: (value) {
                                    setState(() => _categoryId = value);
                                    _load();
                                  },
                                ),
                              ),
                              SizedBox(
                                width: constraints.maxWidth > 600
                                    ? (constraints.maxWidth - 24) / 3
                                    : constraints.maxWidth,
                                child: DropdownButtonFormField<String>(
                                  initialValue: _type,
                                  isExpanded: true,
                                  key: ValueKey('type-$_type'),
                                  decoration: const InputDecoration(
                                    labelText: 'Precio',
                                  ),
                                  items: const [
                                    DropdownMenuItem(
                                      value: null,
                                      child: Text('Todos'),
                                    ),
                                    DropdownMenuItem(
                                      value: 'GRATUITO',
                                      child: Text('Gratuitos'),
                                    ),
                                    DropdownMenuItem(
                                      value: 'PAGO',
                                      child: Text('Pagados'),
                                    ),
                                  ],
                                  onChanged: (value) {
                                    setState(() => _type = value);
                                    _load();
                                  },
                                ),
                              ),
                              SizedBox(
                                width: constraints.maxWidth > 600
                                    ? (constraints.maxWidth - 24) / 3
                                    : constraints.maxWidth,
                                child: DropdownButtonFormField<String>(
                                  initialValue: _modality,
                                  isExpanded: true,
                                  key: ValueKey('modality-$_modality'),
                                  decoration: const InputDecoration(
                                    labelText: 'Modalidad',
                                  ),
                                  items: const [
                                    DropdownMenuItem(
                                      value: null,
                                      child: Text('Todas'),
                                    ),
                                    DropdownMenuItem(
                                      value: 'PRESENCIAL',
                                      child: Text('Presencial'),
                                    ),
                                    DropdownMenuItem(
                                      value: 'VIRTUAL',
                                      child: Text('Virtual'),
                                    ),
                                  ],
                                  onChanged: (value) {
                                    setState(() => _modality = value);
                                    _load();
                                  },
                                ),
                              ),
                            ],
                          ),
                        ),
                        Align(
                          alignment: Alignment.centerRight,
                          child: TextButton(
                            onPressed: _clearFilters,
                            child: const Text('Limpiar filtros'),
                          ),
                        ),
                      ],
                    ),
                    if (_loading) const LinearProgressIndicator(),
                  ],
                ),
              ),
            ),
            if (_error != null)
              SliverFillRemaining(
                hasScrollBody: false,
                child: LoadError(message: _error!, onRetry: _load),
              )
            else if (_loading && _events.isEmpty)
              const SliverFillRemaining(
                hasScrollBody: false,
                child: Center(child: CircularProgressIndicator()),
              )
            else if (_events.isEmpty)
              SliverFillRemaining(
                hasScrollBody: false,
                child: VidiaEmptyState(
                  icon: Icons.event_busy_outlined,
                  message: _hasFilters
                      ? 'No hay eventos que coincidan con la búsqueda y los filtros.'
                      : 'No hay eventos publicados por el momento.',
                ),
              )
            else
              SliverPadding(
                padding: const EdgeInsets.fromLTRB(16, 0, 16, 24),
                sliver: SliverList.separated(
                  itemCount: _events.length,
                  separatorBuilder: (_, _) => const SizedBox(height: 12),
                  itemBuilder: (_, index) => _EventCard(event: _events[index]),
                ),
              ),
          ],
        ),
      ),
    ),
  );

  bool get _hasFilters =>
      _search.text.trim().isNotEmpty ||
      _categoryId != null ||
      _type != null ||
      _modality != null;
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
                    if (event.sinCupo)
                      const Chip(label: Text('Sin cupos'))
                    else if (event.cupoLimitado && event.cupoDisponible != null)
                      Chip(label: Text('${event.cupoDisponible} cupos')),
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
