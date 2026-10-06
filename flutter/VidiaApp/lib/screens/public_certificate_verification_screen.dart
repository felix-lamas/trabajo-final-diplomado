import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/certificado.dart';
import '../repositories/certificado_repository.dart';
import '../services/api_exception.dart';
import '../widgets/vidia_status_chip.dart';

class PublicCertificateVerificationScreen extends StatefulWidget {
  const PublicCertificateVerificationScreen({super.key, this.initialCode});

  final String? initialCode;

  @override
  State<PublicCertificateVerificationScreen> createState() =>
      _PublicCertificateVerificationScreenState();
}

class _PublicCertificateVerificationScreenState
    extends State<PublicCertificateVerificationScreen> {
  late final TextEditingController _codeController;
  bool _loading = false;
  String? _error;
  VerificacionCertificado? _result;

  @override
  void initState() {
    super.initState();
    _codeController = TextEditingController(text: widget.initialCode ?? '');
  }

  @override
  void dispose() {
    _codeController.dispose();
    super.dispose();
  }

  Future<void> _verify() async {
    final code = _codeController.text.trim();
    if (code.isEmpty || _loading) return;
    setState(() {
      _loading = true;
      _error = null;
      _result = null;
    });
    try {
      final result =
          await context.read<CertificadoRepository>().verifyPublic(code);
      if (mounted) setState(() => _result = result);
    } catch (error) {
      if (mounted) {
        setState(() => _error = error is ApiException
            ? error.message
            : 'No se pudo consultar la verificación. Comprueba tu conexión.');
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Verificar certificado')),
        body: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            const Text(
              'Consulta el código con el servicio público de verificación de Vidia.',
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _codeController,
              textCapitalization: TextCapitalization.characters,
              decoration: const InputDecoration(
                labelText: 'Código del certificado',
                hintText: 'UAJMS-…',
                border: OutlineInputBorder(),
              ),
              onSubmitted: (_) => _verify(),
            ),
            const SizedBox(height: 12),
            FilledButton.icon(
              onPressed: _loading ? null : _verify,
              icon: _loading
                  ? const SizedBox.square(
                      dimension: 18,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Icon(Icons.search_rounded),
              label: Text(_loading ? 'Verificando…' : 'Verificar'),
            ),
            if (_error != null) ...[
              const SizedBox(height: 16),
              Text(_error!,
                  style: TextStyle(color: Theme.of(context).colorScheme.error)),
            ],
            if (_result != null) ...[
              const SizedBox(height: 20),
              _verificationCard(context, _result!),
            ],
          ],
        ),
      );

  Widget _verificationCard(
    BuildContext context,
    VerificacionCertificado verification,
  ) =>
      Card(
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(
                    verification.valido
                        ? Icons.verified_outlined
                        : Icons.cancel_outlined,
                    color: verification.valido
                        ? Colors.green
                        : Theme.of(context).colorScheme.error,
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      verification.valido
                          ? 'Certificado válido'
                          : 'Certificado no válido',
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Text(verification.mensaje),
              if (verification.institucion.isNotEmpty)
                _row('Institución', verification.institucion),
              if (verification.nombreCompleto != null)
                _row('Participante', verification.nombreCompleto!),
              if (verification.evento != null)
                _row('Evento', verification.evento!),
              if (verification.tipoCertificado != null)
                _row('Tipo', verification.tipoCertificado!),
              if (verification.horasAcademicas != null)
                _row('Horas académicas', '${verification.horasAcademicas}'),
              if (verification.porcentajeAsistencia != null)
                _row('Asistencia', '${verification.porcentajeAsistencia}%'),
              if (verification.fechaEmision != null)
                _row('Emisión', _date(verification.fechaEmision!)),
              _row('Código', verification.codigoCertificado),
              VidiaStatusChip(
                label: verification.estado,
                kind: verification.valido
                    ? VidiaStatusKind.success
                    : VidiaStatusKind.error,
              ),
            ],
          ),
        ),
      );

  Widget _row(String label, String value) => Padding(
        padding: const EdgeInsets.only(top: 10),
        child: Text('$label: $value'),
      );
}

String _date(DateTime value) => '${value.day.toString().padLeft(2, '0')}/'
    '${value.month.toString().padLeft(2, '0')}/${value.year}';
