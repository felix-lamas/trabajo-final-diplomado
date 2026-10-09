import 'package:flutter/material.dart';
import '../models/organizer_application.dart';
import '../services/auth_service.dart';
import '../services/api_exception.dart';

class OrganizerApplicationScreen extends StatefulWidget {
  const OrganizerApplicationScreen({
    super.key,
    required this.auth,
    this.onSubmitted,
  });
  final AuthService auth;
  final Future<void> Function()? onSubmitted;
  @override
  State<OrganizerApplicationScreen> createState() =>
      _OrganizerApplicationScreenState();
}

class _OrganizerApplicationScreenState
    extends State<OrganizerApplicationScreen> {
  final _form = GlobalKey<FormState>();
  final _reason = TextEditingController();
  final _additional = TextEditingController();
  final _selected = <String>{};
  OrganizerApplication? _application;
  List<OrganizerEventType> _types = [];
  bool _loading = true;
  bool _sending = false;
  String? _error;
  String? _message;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _reason.dispose();
    _additional.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final application = await widget.auth.organizerApplication();
      final types = await widget.auth.organizerEventTypes();
      if (!mounted) return;
      setState(() {
        _application = application;
        _types = types;
        _loading = false;
      });
    } catch (error) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _error = error is ApiException
            ? error.message
            : 'No fue posible consultar tu solicitud.';
      });
    }
  }

  Future<void> _submit() async {
    if (_sending ||
        _application?.canRequest != true ||
        !_form.currentState!.validate()) {
      return;
    }
    if (_selected.isEmpty) {
      setState(() => _error = 'Selecciona al menos un tipo de evento.');
      return;
    }
    setState(() {
      _sending = true;
      _error = null;
      _message = null;
    });
    try {
      final result = await widget.auth.requestOrganizer(
        reason: _reason.text,
        eventTypes: _selected.toList(),
        additionalInformation: _additional.text,
      );
      if (!mounted) return;
      setState(() {
        _application = result;
        _message =
            'Solicitud enviada correctamente. Un administrador la revisará.';
      });
      await widget.onSubmitted?.call();
    } catch (error) {
      if (!mounted) return;
      setState(
        () => _error = error is ApiException
            ? error.message
            : 'No fue posible enviar tu solicitud.',
      );
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Solicitud de organizador')),
    body: SafeArea(
      child: _loading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _load,
              child: ListView(
                padding: const EdgeInsets.all(20),
                physics: const AlwaysScrollableScrollPhysics(),
                children: [
                  const Text(
                    'Solicita permiso para gestionar eventos desde la Web. Tu cuenta conserva sus funciones de participante.',
                  ),
                  const SizedBox(height: 16),
                  if (_application != null) ...[
                    Text(
                      'Estado: ${_application!.state}',
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    if (_application!.state == 'PENDIENTE')
                      const Text(
                        'Tu solicitud está en revisión. No necesitas enviarla nuevamente.',
                      ),
                    if (_application!.state == 'APROBADA')
                      const Text(
                        'Solicitud aprobada. La gestión de eventos está disponible en la Web.',
                      ),
                    if (_application!.rejectionReason != null)
                      Text(
                        'Motivo del rechazo: ${_application!.rejectionReason}',
                      ),
                    if (!_application!.canRequest &&
                        _application!.reason != null) ...[
                      const SizedBox(height: 12),
                      Text(_application!.reason!),
                      Text(_application!.eventTypeNames.join(', ')),
                      if (_application!.additionalInformation?.isNotEmpty ==
                          true)
                        Text(_application!.additionalInformation!),
                    ],
                  ],
                  if (_error != null)
                    Padding(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      child: Text(_error!),
                    ),
                  if (_message != null) Text(_message!),
                  if (_application?.canRequest == true)
                    Form(
                      key: _form,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          const SizedBox(height: 20),
                          TextFormField(
                            controller: _reason,
                            minLines: 3,
                            maxLines: 6,
                            maxLength: 1000,
                            decoration: const InputDecoration(
                              labelText: 'Motivo de solicitud',
                              helperText: 'Entre 30 y 1000 caracteres.',
                            ),
                            validator: (value) =>
                                (value?.trim().length ?? 0) < 30 ||
                                    (value?.length ?? 0) > 1000
                                ? 'Escribe entre 30 y 1000 caracteres.'
                                : null,
                          ),
                          const SizedBox(height: 16),
                          const Text(
                            'Tipos de eventos que te interesa publicar',
                          ),
                          for (final type in _types)
                            CheckboxListTile(
                              contentPadding: EdgeInsets.zero,
                              title: Text(type.name),
                              value: _selected.contains(type.code),
                              onChanged: _sending
                                  ? null
                                  : (checked) => setState(() {
                                      if (checked == true) {
                                        _selected.add(type.code);
                                      } else {
                                        _selected.remove(type.code);
                                      }
                                    }),
                            ),
                          const SizedBox(height: 16),
                          TextFormField(
                            controller: _additional,
                            minLines: 2,
                            maxLines: 4,
                            maxLength: 1000,
                            decoration: const InputDecoration(
                              labelText: 'Información adicional (opcional)',
                            ),
                            validator: (value) => (value?.length ?? 0) > 1000
                                ? 'Admite hasta 1000 caracteres.'
                                : null,
                          ),
                          const SizedBox(height: 16),
                          FilledButton(
                            onPressed: _sending ? null : _submit,
                            child: Text(
                              _sending
                                  ? 'Enviando...'
                                  : 'Solicitar ser organizador',
                            ),
                          ),
                        ],
                      ),
                    ),
                  const SizedBox(height: 16),
                  OutlinedButton(
                    onPressed: _sending ? null : _load,
                    child: const Text('Actualizar estado'),
                  ),
                ],
              ),
            ),
    ),
  );
}
