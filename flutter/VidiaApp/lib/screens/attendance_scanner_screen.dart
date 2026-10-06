import 'dart:async';

import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:mobile_scanner/mobile_scanner.dart';

import '../models/asistencia.dart';
import '../models/sesion_evento.dart';
import '../repositories/asistencia_repository.dart';
import '../services/api_exception.dart';
import '../services/asistencia_error_message.dart';
import '../services/asistencia_flow.dart';
import '../services/attendance_location_source.dart';

typedef AttendanceScannerBuilder = Widget Function(
  BuildContext context,
  ValueChanged<String> onToken,
);

class AttendanceScannerScreen extends StatefulWidget {
  const AttendanceScannerScreen({
    super.key,
    required this.session,
    required this.requiresGps,
    required this.repository,
    required this.locationSource,
    this.scannerBuilder,
  });

  final SesionEvento session;
  final bool requiresGps;
  final AsistenciaRepository repository;
  final AttendanceLocationSource locationSource;

  /// Reemplaza la cámara solo en pruebas de widget.
  final AttendanceScannerBuilder? scannerBuilder;

  @override
  State<AttendanceScannerScreen> createState() =>
      _AttendanceScannerScreenState();
}

class _AttendanceScannerScreenState extends State<AttendanceScannerScreen> {
  late final MobileScannerController _controller;
  bool _cameraStarted = false;
  bool _processing = false;
  bool _submitted = false;
  String? _error;
  Asistencia? _result;

  @override
  void initState() {
    super.initState();
    _controller = MobileScannerController(
      autoStart: false,
      detectionSpeed: DetectionSpeed.noDuplicates,
      formats: const [BarcodeFormat.qrCode],
    );
  }

  @override
  void dispose() {
    unawaited(_controller.dispose());
    super.dispose();
  }

  void _startCamera() {
    setState(() {
      _cameraStarted = true;
      _error = null;
    });
    if (widget.scannerBuilder != null) return;
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      if (!mounted) return;
      try {
        await _controller.start();
      } on MobileScannerException catch (error) {
        if (mounted) {
          setState(() {
            _error = _cameraErrorMessage(error);
            _cameraStarted = false;
          });
        }
      } catch (_) {
        if (mounted) {
          setState(() {
            _error =
                'No se pudo abrir la cámara. Revisa el permiso e inténtalo otra vez.';
            _cameraStarted = false;
          });
        }
      }
    });
  }

  Future<void> _resumeCamera() async {
    final wasCameraAttached = _cameraStarted;
    setState(() {
      _error = null;
      _submitted = false;
      _result = null;
      _cameraStarted = true;
    });
    if (widget.scannerBuilder != null) return;
    if (!wasCameraAttached) {
      WidgetsBinding.instance.addPostFrameCallback((_) async {
        if (!mounted) return;
        await _tryStartCamera();
      });
      return;
    }
    await _tryStartCamera();
  }

  Future<void> _tryStartCamera() async {
    try {
      await _controller.start();
    } on MobileScannerException catch (error) {
      if (mounted) {
        setState(() {
          _error = _cameraErrorMessage(error);
          _cameraStarted = false;
        });
      }
    } catch (_) {
      if (mounted) {
        setState(() {
          _error =
              'No se pudo abrir la cámara. Revisa el permiso e inténtalo otra vez.';
          _cameraStarted = false;
        });
      }
    }
  }

  String _cameraErrorMessage(MobileScannerException error) => error.errorCode ==
          MobileScannerErrorCode.permissionDenied
      ? 'Vidia necesita permiso de cámara para escanear el código de asistencia.'
      : error.errorCode == MobileScannerErrorCode.unsupported
          ? 'Este dispositivo no permite abrir una cámara compatible.'
          : 'No se pudo iniciar el escáner. Inténtalo nuevamente.';

  void _onCapture(BarcodeCapture capture) {
    final qr = capture.barcodes
        .where((barcode) => barcode.format == BarcodeFormat.qrCode)
        .firstOrNull;
    final token = qr?.rawValue;
    if (token == null || token.isEmpty || _processing || _submitted) return;
    unawaited(_submit(token));
  }

  Future<void> _submit(String rawToken) async {
    if (_processing || _submitted) return;
    setState(() {
      _processing = true;
      _error = null;
    });
    if (widget.scannerBuilder == null) {
      try {
        await _controller.stop();
      } catch (_) {
        // La cámara puede estar deteniéndose por el ciclo de vida de Android.
      }
    }

    try {
      if (widget.requiresGps && !await _confirmLocationUse()) {
        throw const AttendanceLocationException(
          AttendanceLocationFailure.permissionDenied,
        );
      }
      final result = await AsistenciaFlow(
        repository: widget.repository,
        locationSource: widget.locationSource,
      ).submit(
        session: widget.session,
        rawToken: rawToken,
        requiresGps: widget.requiresGps,
      );
      if (mounted) {
        setState(() {
          _result = result;
          _submitted = true;
          _cameraStarted = false;
        });
      }
    } catch (error) {
      if (mounted) {
        setState(() {
          _error = asistenciaErrorMessage(error);
          _cameraStarted = false;
          _submitted =
              error is ApiException && error.code == 'ATTENDANCE_DUPLICATED';
        });
      }
    } finally {
      if (mounted) setState(() => _processing = false);
    }
  }

  Future<bool> _confirmLocationUse() async =>
      await showDialog<bool>(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('Ubicación para validar asistencia'),
          content: const Text(
            'Esta sesión requiere GPS. Tu ubicación actual y su precisión se enviarán al servidor para validar el área permitida.',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('Cancelar'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(context, true),
              child: const Text('Continuar'),
            ),
          ],
        ),
      ) ??
      false;

  Future<void> _openSettings({bool location = false}) async {
    if (location) {
      await Geolocator.openLocationSettings();
    } else {
      await Geolocator.openAppSettings();
    }
  }

  bool get _needsLocationSettings =>
      _error?.startsWith('Activa la ubicación') == true;

  bool get _needsAppSettings =>
      _error?.startsWith('Habilita el permiso') == true ||
      _error?.startsWith('Vidia necesita permiso de cámara') == true;

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Registrar asistencia')),
        body: SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: _result != null
                ? _successContent(context, _result!)
                : Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Text(
                        widget.session.nombre,
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      const SizedBox(height: 8),
                      Text(widget.requiresGps
                          ? 'Escanea el QR temporal. Se solicitará ubicación para validarla con las reglas del servidor.'
                          : 'Escanea el QR temporal mostrado para esta sesión.'),
                      const SizedBox(height: 16),
                      Expanded(child: _cameraArea(context)),
                      if (_processing) ...[
                        const SizedBox(height: 12),
                        const LinearProgressIndicator(),
                        const SizedBox(height: 8),
                        const Text('Validando código y asistencia…',
                            textAlign: TextAlign.center),
                      ],
                      if (_error != null) ...[
                        const SizedBox(height: 12),
                        _errorContent(context),
                      ],
                    ],
                  ),
          ),
        ),
      );

  Widget _cameraArea(BuildContext context) {
    if (_error != null && !_cameraStarted) return const SizedBox.shrink();
    if (!_cameraStarted) {
      return Card(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.qr_code_scanner_rounded, size: 64),
              const SizedBox(height: 16),
              const Text(
                'La cámara se usará únicamente para leer el QR temporal de esta sesión.',
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 20),
              FilledButton.icon(
                onPressed: _processing ? null : _startCamera,
                icon: const Icon(Icons.camera_alt_outlined),
                label: const Text('Activar cámara'),
              ),
            ],
          ),
        ),
      );
    }

    final injectedScanner = widget.scannerBuilder;
    return ClipRRect(
      borderRadius: BorderRadius.circular(18),
      child: injectedScanner != null
          ? injectedScanner(context, (token) => unawaited(_submit(token)))
          : MobileScanner(
              controller: _controller,
              onDetect: _onCapture,
              errorBuilder: (context, error) => ColoredBox(
                color: Colors.black87,
                child: Center(
                  child: Padding(
                    padding: const EdgeInsets.all(20),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const Icon(Icons.no_photography_outlined,
                            color: Colors.white, size: 44),
                        const SizedBox(height: 12),
                        Text(_cameraErrorMessage(error),
                            textAlign: TextAlign.center,
                            style: const TextStyle(color: Colors.white)),
                        const SizedBox(height: 12),
                        if (error.errorCode ==
                            MobileScannerErrorCode.permissionDenied)
                          OutlinedButton(
                            onPressed: _openSettings,
                            child: const Text('Abrir ajustes de la aplicación'),
                          )
                        else
                          OutlinedButton(
                            onPressed: _resumeCamera,
                            child: const Text('Reintentar cámara'),
                          ),
                      ],
                    ),
                  ),
                ),
              ),
              overlayBuilder: (context, constraints) => IgnorePointer(
                child: Center(
                  child: Container(
                    width: constraints.maxWidth * .72,
                    height: constraints.maxWidth * .72,
                    constraints: const BoxConstraints(
                      maxWidth: 300,
                      maxHeight: 300,
                    ),
                    decoration: BoxDecoration(
                      border: Border.all(color: Colors.white, width: 3),
                      borderRadius: BorderRadius.circular(22),
                    ),
                  ),
                ),
              ),
            ),
    );
  }

  Widget _errorContent(BuildContext context) {
    final duplicate = _submitted;
    return Card(
      color: Theme.of(context).colorScheme.errorContainer,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(_error!, textAlign: TextAlign.center),
            const SizedBox(height: 10),
            if (_needsAppSettings || _needsLocationSettings)
              OutlinedButton.icon(
                onPressed: () =>
                    _openSettings(location: _needsLocationSettings),
                icon: const Icon(Icons.settings_outlined),
                label: Text(_needsLocationSettings
                    ? 'Abrir ajustes de ubicación'
                    : 'Abrir ajustes de la aplicación'),
              ),
            if (!duplicate)
              FilledButton.tonalIcon(
                onPressed: _processing ? null : _resumeCamera,
                icon: const Icon(Icons.qr_code_scanner_rounded),
                label: const Text('Intentar de nuevo'),
              ),
            TextButton(
              onPressed: () => Navigator.pop(context, _result),
              child: const Text('Volver a sesiones'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _successContent(BuildContext context, Asistencia attendance) => Center(
        child: SingleChildScrollView(
          child: Card(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(Icons.check_circle_outline,
                      color: Colors.green, size: 68),
                  const SizedBox(height: 18),
                  Text(
                    'Asistencia registrada correctamente',
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 12),
                  Text(attendance.evento, textAlign: TextAlign.center),
                  const SizedBox(height: 4),
                  Text(attendance.sesion, textAlign: TextAlign.center),
                  if (attendance.fechaHoraRegistro != null) ...[
                    const SizedBox(height: 8),
                    Text(_formatDateTime(attendance.fechaHoraRegistro!)),
                  ],
                  const SizedBox(height: 20),
                  FilledButton(
                    onPressed: () => Navigator.pop(context, attendance),
                    child: const Text('Volver a sesiones'),
                  ),
                ],
              ),
            ),
          ),
        ),
      );
}

String _formatDateTime(DateTime value) =>
    '${value.day.toString().padLeft(2, '0')}/'
    '${value.month.toString().padLeft(2, '0')}/${value.year} '
    '${value.hour.toString().padLeft(2, '0')}:${value.minute.toString().padLeft(2, '0')}';
