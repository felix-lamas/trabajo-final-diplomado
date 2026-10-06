import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/certificado.dart';
import '../repositories/certificado_repository.dart';
import '../services/api_exception.dart';
import '../services/certificado_document_manager.dart';
import '../widgets/load_error.dart';
import '../widgets/vidia_status_chip.dart';
import 'public_certificate_verification_screen.dart';

class CertificateDetailScreen extends StatefulWidget {
  const CertificateDetailScreen({super.key, required this.certificadoId});

  final String certificadoId;

  @override
  State<CertificateDetailScreen> createState() =>
      _CertificateDetailScreenState();
}

class _CertificateDetailScreenState extends State<CertificateDetailScreen> {
  bool _loading = true;
  bool _saving = false;
  bool _opening = false;
  String? _error;
  Certificado? _certificado;

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
      final value = await context
          .read<CertificadoRepository>()
          .fetchById(widget.certificadoId);
      if (mounted) setState(() => _certificado = value);
    } catch (error) {
      if (mounted) {
        setState(() => _error = error is ApiException
            ? error.message
            : 'No se pudo cargar el certificado.');
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _download() async {
    final certificate = _certificado;
    if (certificate == null || _saving || _opening) return;
    setState(() => _saving = true);
    try {
      final path = await context
          .read<CertificadoDocumentManager>()
          .download(certificate.id);
      if (!mounted) return;
      await _refreshCertificate();
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: Text(
            path == null ? 'Descarga cancelada.' : 'Certificado PDF guardado.'),
      ));
    } catch (error) {
      if (mounted) _showError(error);
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  Future<void> _preview() async {
    final certificate = _certificado;
    if (certificate == null || _saving || _opening) return;
    setState(() => _opening = true);
    try {
      final opened = await context
          .read<CertificadoDocumentManager>()
          .preview(certificate.id);
      await _refreshCertificate();
      if (mounted && !opened) {
        _showMessage(
            'No se encontró una aplicación para abrir PDF. Puedes guardarlo en el dispositivo.');
      }
    } catch (error) {
      if (mounted) _showError(error);
    } finally {
      if (mounted) setState(() => _opening = false);
    }
  }

  Future<void> _refreshCertificate() async {
    try {
      final updated = await context
          .read<CertificadoRepository>()
          .fetchById(widget.certificadoId);
      if (mounted) setState(() => _certificado = updated);
    } catch (_) {
      // La descarga ya se procesó; el refresco solo sincroniza el estado visual.
    }
  }

  void _showError(Object error) => _showMessage(
        error is ApiException
            ? error.message
            : error is FormatException
                ? error.message
                : 'No se pudo procesar el certificado. Inténtalo otra vez.',
      );

  void _showMessage(String message) =>
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(message)),
      );

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Certificado')),
        body: _loading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
                ? LoadError(message: _error!, onRetry: _load)
                : _certificado == null
                    ? LoadError(
                        message: 'No se encontró el certificado.',
                        onRetry: _load,
                      )
                    : _content(context, _certificado!),
      );

  Widget _content(BuildContext context, Certificado certificate) => ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Icon(Icons.workspace_premium_outlined, size: 40),
                  const SizedBox(height: 14),
                  Text(certificate.evento,
                      style: Theme.of(context).textTheme.titleLarge),
                  const SizedBox(height: 12),
                  _detailRow(
                      'Tipo', _friendlyType(certificate.tipoCertificado)),
                  _detailRow('Estado', certificate.estado),
                  _detailRow('Código', certificate.codigoCertificado),
                  if (certificate.fechaEmision != null)
                    _detailRow('Fecha de emisión',
                        _dateTime(certificate.fechaEmision!)),
                  if (certificate.horasAcademicas != null)
                    _detailRow(
                        'Horas académicas', '${certificate.horasAcademicas}'),
                  if (certificate.cargaHoraria != null)
                    _detailRow('Carga horaria del evento',
                        '${certificate.cargaHoraria}'),
                  if (certificate.porcentajeAsistencia != null)
                    _detailRow('Porcentaje de asistencia',
                        '${certificate.porcentajeAsistencia}%'),
                  const SizedBox(height: 8),
                  VidiaStatusChip(
                    label: certificate.estado,
                    kind: _statusKind(certificate.estado),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          FilledButton.icon(
            onPressed: _saving || _opening ? null : _download,
            icon: _saving
                ? const SizedBox.square(
                    dimension: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.download_outlined),
            label: Text(_saving ? 'Guardando…' : 'Descargar PDF'),
          ),
          OutlinedButton.icon(
            onPressed: _saving || _opening ? null : _preview,
            icon: _opening
                ? const SizedBox.square(
                    dimension: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.picture_as_pdf_outlined),
            label: Text(_opening ? 'Abriendo…' : 'Abrir / previsualizar PDF'),
          ),
          TextButton.icon(
            onPressed: () => Navigator.push<void>(
              context,
              MaterialPageRoute(
                builder: (_) => PublicCertificateVerificationScreen(
                  initialCode: certificate.codigoCertificado,
                ),
              ),
            ),
            icon: const Icon(Icons.verified_outlined),
            label: const Text('Verificar código públicamente'),
          ),
        ],
      );

  Widget _detailRow(String label, String value) => Padding(
        padding: const EdgeInsets.only(bottom: 10),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(width: 145, child: Text(label)),
            Expanded(child: Text(value)),
          ],
        ),
      );
}

String _friendlyType(String type) => switch (type) {
      'CURRICULAR' => 'Curricular',
      'NO_CURRICULAR' => 'No curricular',
      _ => type,
    };

String _dateTime(DateTime value) => '${value.day.toString().padLeft(2, '0')}/'
    '${value.month.toString().padLeft(2, '0')}/${value.year} '
    '${value.hour.toString().padLeft(2, '0')}:${value.minute.toString().padLeft(2, '0')}';

VidiaStatusKind _statusKind(String estado) => switch (estado) {
      'GENERADO' || 'DESCARGADO' => VidiaStatusKind.success,
      'ANULADO' => VidiaStatusKind.error,
      _ => VidiaStatusKind.info,
    };
