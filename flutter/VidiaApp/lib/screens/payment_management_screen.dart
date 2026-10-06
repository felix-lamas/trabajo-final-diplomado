import 'dart:typed_data';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/archivo_comprobante.dart';
import '../models/evento.dart';
import '../models/inscripcion.dart';
import '../models/pago.dart';
import '../repositories/evento_repository.dart';
import '../repositories/inscripcion_repository.dart';
import '../repositories/pago_repository.dart';
import '../services/api_exception.dart';
import '../widgets/load_error.dart';
import '../widgets/vidia_status_chip.dart';

class PaymentManagementScreen extends StatefulWidget {
  const PaymentManagementScreen({super.key, required this.registration});

  final Inscripcion registration;

  @override
  State<PaymentManagementScreen> createState() =>
      _PaymentManagementScreenState();
}

class _PaymentManagementScreenState extends State<PaymentManagementScreen> {
  bool _loading = true;
  bool _uploading = false;
  bool _loadingQr = false;
  bool _loadingReceipt = false;
  String? _error;
  String? _qrError;
  String _registrationStatus = '';
  Pago? _payment;
  Evento? _event;
  ArchivoComprobante? _selectedFile;
  Uint8List? _qrBytes;

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
        context.read<PagoRepository>().fetchMine(),
        context
            .read<EventoRepository>()
            .fetchById(widget.registration.eventoId),
        context.read<InscripcionRepository>().fetchMine(),
      ]);
      final payments = results[0] as List<Pago>;
      final registrations = results[2] as List<Inscripcion>;
      final registration = registrations
          .where((item) => item.id == widget.registration.id)
          .firstOrNull;
      if (registration == null) {
        throw const ApiException(
          statusCode: 404,
          message: 'No se encontró esta inscripción en tu cuenta.',
        );
      }
      final payment = payments
          .where((item) => item.inscripcionId == widget.registration.id)
          .firstOrNull;
      if (payment == null) {
        throw const ApiException(
          statusCode: 404,
          message: 'No se encontró un pago asociado a esta inscripción.',
        );
      }
      if (!mounted) return;
      setState(() {
        _payment = payment;
        _event = results[1] as Evento;
        _registrationStatus = registration.estado;
      });
      await _loadQr();
    } catch (error) {
      if (mounted) {
        setState(() => _error = error is ApiException
            ? error.message
            : 'No se pudo cargar la información del pago.');
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _loadQr() async {
    final event = _event;
    if (event?.qrPagoUrl == null) return;
    setState(() {
      _loadingQr = true;
      _qrError = null;
    });
    try {
      final file = await context
          .read<PagoRepository>()
          .downloadEventPaymentQr(event!.id);
      if (file.contentType != 'image/png' && file.contentType != 'image/jpeg') {
        throw const FormatException(
            'El QR no tiene un formato de imagen válido.');
      }
      if (mounted) setState(() => _qrBytes = file.bytes);
    } catch (_) {
      if (mounted) {
        setState(() => _qrError = 'No se pudo cargar el QR de pago.');
      }
    } finally {
      if (mounted) setState(() => _loadingQr = false);
    }
  }

  Future<void> _pickFile() async {
    try {
      final files = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: const ['jpg', 'jpeg', 'png', 'pdf'],
        allowMultiple: false,
        withReadStream: true,
      );
      if (files == null || files.files.isEmpty || !mounted) return;
      final file = files.files.single;
      if (file.size > ArchivoComprobante.maxBytes) {
        throw const ArchivoComprobanteException(
            ArchivoComprobanteError.demasiadoGrande);
      }
      final stream = file.readStream;
      if (stream == null) {
        _showMessage('No se pudo leer el archivo seleccionado.');
        return;
      }
      final builder = BytesBuilder(copy: false);
      var total = 0;
      await for (final chunk in stream) {
        total += chunk.length;
        if (total > ArchivoComprobante.maxBytes) {
          throw const ArchivoComprobanteException(
              ArchivoComprobanteError.demasiadoGrande);
        }
        builder.add(chunk);
      }
      final selected = ArchivoComprobante.validar(
        nombre: file.name,
        bytes: builder.takeBytes(),
      );
      setState(() => _selectedFile = selected);
    } on ArchivoComprobanteException catch (error) {
      _showMessage(switch (error.error) {
        ArchivoComprobanteError.tipoNoPermitido =>
          'Selecciona un archivo JPG, PNG o PDF.',
        ArchivoComprobanteError.demasiadoGrande =>
          'El archivo no puede superar 5 MiB.',
        ArchivoComprobanteError.archivoVacio =>
          'El archivo seleccionado está vacío.',
      });
    } catch (_) {
      _showMessage('No se pudo abrir o leer el archivo seleccionado.');
    }
  }

  Future<void> _upload() async {
    final payment = _payment;
    final file = _selectedFile;
    if (payment == null || file == null || _uploading || !_canUpload(payment)) {
      return;
    }
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Enviar comprobante'),
        content:
            Text('¿Deseas enviar “${file.nombre}” para validación manual?'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('Volver')),
          FilledButton(
              onPressed: () => Navigator.pop(context, true),
              child: const Text('Enviar')),
        ],
      ),
    );
    if (confirmed != true || !mounted) return;
    setState(() => _uploading = true);
    try {
      final updated = await context.read<PagoRepository>().uploadReceipt(
            pagoId: payment.id,
            fileName: file.nombre,
            contentType: file.contentType,
            bytes: file.bytes,
          );
      if (!mounted) return;
      setState(() {
        _payment = updated;
        _selectedFile = null;
      });
      _showMessage(updated.estado == 'PENDIENTE_VALIDACION'
          ? 'Comprobante enviado. Pendiente de validación.'
          : 'El servidor recibió el comprobante. Estado: ${paymentStatusLabel(updated.estado)}.');
    } catch (error) {
      _showMessage(error is ApiException
          ? error.message
          : 'No se pudo enviar el comprobante.');
    } finally {
      if (mounted) setState(() => _uploading = false);
    }
  }

  Future<void> _showPaymentReceipt() async {
    final payment = _payment;
    if (payment == null || payment.comprobante?.disponible != true) return;
    setState(() => _loadingReceipt = true);
    try {
      final file =
          await context.read<PagoRepository>().downloadReceipt(payment.id);
      if (file.contentType == 'image/jpeg' || file.contentType == 'image/png') {
        if (!mounted) return;
        await showDialog<void>(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Comprobante de pago'),
            content: SingleChildScrollView(
              child: Image.memory(file.bytes,
                  errorBuilder: (_, __, ___) =>
                      const Text('No se pudo mostrar la imagen descargada.')),
            ),
            actions: [
              TextButton(
                onPressed: () async {
                  final saved = await _saveFile(
                    payment.id,
                    file.contentType!,
                    file.bytes,
                  );
                  if (context.mounted && saved) Navigator.pop(context);
                },
                child: const Text('Guardar copia'),
              ),
              TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Cerrar')),
            ],
          ),
        );
      } else if (file.contentType == 'application/pdf') {
        final saved =
            await _saveFile(payment.id, file.contentType!, file.bytes);
        if (saved) _showMessage('Comprobante PDF guardado en el dispositivo.');
      } else {
        _showMessage('El servidor devolvió un formato de archivo no admitido.');
      }
    } catch (error) {
      _showMessage(error is ApiException
          ? error.message
          : 'No se pudo descargar el comprobante.');
    } finally {
      if (mounted) setState(() => _loadingReceipt = false);
    }
  }

  Future<bool> _saveFile(
      String paymentId, String contentType, Uint8List bytes) async {
    final extension = switch (contentType) {
      'image/jpeg' => 'jpg',
      'image/png' => 'png',
      'application/pdf' => 'pdf',
      _ => null,
    };
    if (extension == null) return false;
    final saved = await FilePicker.platform.saveFile(
      dialogTitle: 'Guardar comprobante de pago',
      fileName: 'comprobante-pago-$paymentId.$extension',
      type: FileType.custom,
      allowedExtensions: [extension],
      bytes: bytes,
    );
    return saved != null;
  }

  Future<void> _showRegistrationReceipt() async {
    setState(() => _loadingReceipt = true);
    try {
      final receipt = await context
          .read<PagoRepository>()
          .fetchRegistrationReceipt(widget.registration.id);
      if (!mounted) return;
      await showDialog<void>(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('Constancia de inscripción'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                  'Constancia no tributaria; no es factura ni certificado.'),
              const SizedBox(height: 14),
              _ReceiptLine('Evento', receipt.eventoTitulo),
              _ReceiptLine('Código', receipt.codigoInscripcion),
              _ReceiptLine('Monto', 'Bs. ${receipt.monto.toStringAsFixed(2)}'),
              _ReceiptLine('Inscripción', receipt.estadoInscripcion),
              _ReceiptLine('Pago', receipt.estadoPago),
              _ReceiptLine('Verificación', receipt.codigoVerificacion),
            ],
          ),
          actions: [
            TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('Cerrar')),
          ],
        ),
      );
    } catch (error) {
      _showMessage(error is ApiException
          ? error.message
          : 'No se pudo consultar la constancia de inscripción.');
    } finally {
      if (mounted) setState(() => _loadingReceipt = false);
    }
  }

  bool _canUpload(Pago payment) =>
      payment.puedePresentarComprobante &&
      _registrationStatus == 'PENDIENTE_PAGO';

  void _showMessage(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context)
        .showSnackBar(SnackBar(content: Text(message)));
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Gestión de pago')),
        body: _loading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
                ? LoadError(message: _error!, onRetry: _load)
                : _payment == null || _event == null
                    ? const Center(child: Text('No hay información del pago.'))
                    : _body(_payment!, _event!),
      );

  Widget _body(Pago payment, Evento event) => ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Text(event.titulo, style: Theme.of(context).textTheme.headlineSmall),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Monto informado por el backend'),
                  const SizedBox(height: 4),
                  Text('Bs. ${payment.monto.toStringAsFixed(2)}',
                      style: Theme.of(context).textTheme.headlineMedium),
                  const SizedBox(height: 12),
                  VidiaStatusChip(
                    label: paymentStatusLabel(payment.estado),
                    kind: paymentStatusKind(payment.estado),
                  ),
                  if (payment.fechaResolucion != null) ...[
                    const SizedBox(height: 8),
                    Text(
                        'Fecha de resolución: ${formatPaymentDate(payment.fechaResolucion)}'),
                  ],
                ],
              ),
            ),
          ),
          if (payment.estado == 'PENDIENTE_VALIDACION')
            const Card(
              child: ListTile(
                leading: Icon(Icons.hourglass_top_rounded),
                title: Text('Comprobante enviado. Pendiente de validación.'),
              ),
            ),
          if (payment.estado == 'APROBADO')
            const Card(
              child: ListTile(
                leading: Icon(Icons.check_circle_outline),
                title: Text('Pago aprobado'),
                subtitle: Text(
                    'La inscripción refleja el estado definido por el sistema.'),
              ),
            ),
          if (payment.estado == 'RECHAZADO')
            Card(
              child: ListTile(
                leading: const Icon(Icons.error_outline),
                title: const Text('Pago rechazado'),
                subtitle: payment.motivoRechazo == null
                    ? const Text('El backend no informó un motivo.')
                    : Text(payment.motivoRechazo!),
              ),
            ),
          if (!event.esGratuito && event.instruccionesPago != null) ...[
            const SizedBox(height: 12),
            Text('Instrucciones de pago',
                style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 6),
            Card(
                child: Padding(
              padding: const EdgeInsets.all(16),
              child: Text(event.instruccionesPago!),
            )),
          ],
          if (!event.esGratuito && event.qrPagoUrl != null) ...[
            const SizedBox(height: 12),
            Text('QR de pago del evento',
                style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 8),
            if (_loadingQr)
              const Center(child: CircularProgressIndicator())
            else if (_qrBytes != null)
              Center(
                child: ConstrainedBox(
                  constraints:
                      const BoxConstraints(maxWidth: 300, maxHeight: 300),
                  child: Image.memory(_qrBytes!, fit: BoxFit.contain),
                ),
              )
            else if (_qrError != null)
              LoadError(message: _qrError!, onRetry: _loadQr)
          ],
          if (payment.comprobante?.disponible == true) ...[
            const SizedBox(height: 16),
            OutlinedButton.icon(
              onPressed: _loadingReceipt ? null : _showPaymentReceipt,
              icon: const Icon(Icons.download_outlined),
              label: Text(_loadingReceipt
                  ? 'Cargando comprobante...'
                  : 'Consultar / guardar comprobante de pago'),
            ),
          ],
          if (_canUpload(payment)) ...[
            const SizedBox(height: 20),
            Text(payment.estado == 'RECHAZADO'
                ? 'Selecciona un nuevo archivo para reenviar.'
                : 'Después de realizar el pago externamente, puedes enviar el comprobante para revisión.'),
            const SizedBox(height: 10),
            OutlinedButton.icon(
              onPressed: _uploading ? null : _pickFile,
              icon: const Icon(Icons.attach_file_rounded),
              label: const Text('Seleccionar comprobante'),
            ),
            if (_selectedFile != null) ...[
              Card(
                child: ListTile(
                  leading: const Icon(Icons.insert_drive_file_outlined),
                  title: Text(_selectedFile!.nombre),
                  subtitle: Text(
                      '${(_selectedFile!.bytes.length / 1024).toStringAsFixed(1)} KiB · ${_selectedFile!.contentType}'),
                  trailing: IconButton(
                    tooltip: 'Quitar archivo seleccionado',
                    onPressed: _uploading
                        ? null
                        : () => setState(() => _selectedFile = null),
                    icon: const Icon(Icons.close_rounded),
                  ),
                ),
              ),
              FilledButton.icon(
                onPressed: _uploading ? null : _upload,
                icon: _uploading
                    ? const SizedBox.square(
                        dimension: 18,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Icon(Icons.cloud_upload_outlined),
                label: Text(_uploading ? 'Enviando...' : 'Enviar comprobante'),
              ),
            ],
          ],
          const SizedBox(height: 20),
          OutlinedButton.icon(
            onPressed: _loadingReceipt ? null : _showRegistrationReceipt,
            icon: const Icon(Icons.receipt_long_outlined),
            label: const Text('Consultar constancia de inscripción'),
          ),
          const SizedBox(height: 12),
          const Text(
            'El pago se realiza externamente. Vidia no procesa ni confirma transferencias.',
            textAlign: TextAlign.center,
          ),
        ],
      );
}

String paymentStatusLabel(String status) => switch (status) {
      'PENDIENTE_PAGO' => 'Pendiente de pago',
      'PENDIENTE_VALIDACION' => 'Pendiente de validación',
      'APROBADO' => 'Aprobado',
      'RECHAZADO' => 'Rechazado',
      _ => status,
    };

VidiaStatusKind paymentStatusKind(String status) => switch (status) {
      'APROBADO' => VidiaStatusKind.success,
      'RECHAZADO' => VidiaStatusKind.error,
      'PENDIENTE_PAGO' || 'PENDIENTE_VALIDACION' => VidiaStatusKind.warning,
      _ => VidiaStatusKind.info,
    };

String formatPaymentDate(DateTime? date) {
  if (date == null) return 'No disponible';
  return '${date.day.toString().padLeft(2, '0')}/'
      '${date.month.toString().padLeft(2, '0')}/${date.year}';
}

class _ReceiptLine extends StatelessWidget {
  const _ReceiptLine(this.label, this.value);

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(bottom: 6),
        child: Text('$label: $value'),
      );
}
