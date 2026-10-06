import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/certificado.dart';
import '../repositories/certificado_repository.dart';
import '../services/api_exception.dart';
import '../widgets/load_error.dart';
import '../widgets/vidia_empty_state.dart';
import '../widgets/vidia_status_chip.dart';
import 'certificate_detail_screen.dart';
import 'public_certificate_verification_screen.dart';

class MyCertificatesScreen extends StatefulWidget {
  const MyCertificatesScreen({super.key});

  @override
  State<MyCertificatesScreen> createState() => _MyCertificatesScreenState();
}

class _MyCertificatesScreenState extends State<MyCertificatesScreen> {
  bool _loading = true;
  String? _error;
  List<Certificado> _certificados = const [];

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
      final certificados =
          await context.read<CertificadoRepository>().fetchMine();
      if (mounted) setState(() => _certificados = certificados);
    } catch (error) {
      if (mounted) {
        setState(() => _error = error is ApiException
            ? error.message
            : 'No se pudieron cargar tus certificados.');
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(
          title: const Text('Mis certificados'),
          actions: [
            IconButton(
              tooltip: 'Verificar certificado',
              icon: const Icon(Icons.verified_outlined),
              onPressed: () => Navigator.push<void>(
                context,
                MaterialPageRoute(
                  builder: (_) => const PublicCertificateVerificationScreen(),
                ),
              ),
            ),
          ],
        ),
        body: _loading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
                ? LoadError(message: _error!, onRetry: _load)
                : RefreshIndicator(
                    onRefresh: _load,
                    child: _certificados.isEmpty
                        ? ListView(
                            children: const [
                              SizedBox(height: 140),
                              VidiaEmptyState(
                                icon: Icons.workspace_premium_outlined,
                                message: 'Sin certificados todavía.',
                              ),
                            ],
                          )
                        : ListView.separated(
                            padding: const EdgeInsets.all(16),
                            itemCount: _certificados.length,
                            separatorBuilder: (_, __) =>
                                const SizedBox(height: 10),
                            itemBuilder: (context, index) {
                              final item = _certificados[index];
                              return Card(
                                child: ListTile(
                                  contentPadding: const EdgeInsets.all(16),
                                  leading: const CircleAvatar(
                                    child:
                                        Icon(Icons.workspace_premium_outlined),
                                  ),
                                  title: Text(item.evento),
                                  subtitle: Padding(
                                    padding: const EdgeInsets.only(top: 8),
                                    child: Column(
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        Text(_friendlyType(
                                            item.tipoCertificado)),
                                        if (item.fechaEmision != null)
                                          Text(
                                              'Emisión: ${_date(item.fechaEmision!)}'),
                                        if (item.horasAcademicas != null)
                                          Text(
                                              'Horas académicas: ${item.horasAcademicas}'),
                                        Text(
                                            'Código: ${item.codigoCertificado}'),
                                        const SizedBox(height: 8),
                                        VidiaStatusChip(
                                          label: item.estado,
                                          kind: _statusKind(item.estado),
                                        ),
                                      ],
                                    ),
                                  ),
                                  trailing:
                                      const Icon(Icons.chevron_right_rounded),
                                  onTap: () => Navigator.push<void>(
                                    context,
                                    MaterialPageRoute(
                                      builder: (_) => CertificateDetailScreen(
                                        certificadoId: item.id,
                                      ),
                                    ),
                                  ).then((_) => _load()),
                                ),
                              );
                            },
                          ),
                  ),
      );
}

String _date(DateTime value) => '${value.day.toString().padLeft(2, '0')}/'
    '${value.month.toString().padLeft(2, '0')}/${value.year}';

String _friendlyType(String type) => switch (type) {
      'CURRICULAR' => 'Certificado curricular',
      'NO_CURRICULAR' => 'Certificado no curricular',
      _ => type,
    };

VidiaStatusKind _statusKind(String estado) => switch (estado) {
      'GENERADO' || 'DESCARGADO' => VidiaStatusKind.success,
      'ANULADO' => VidiaStatusKind.error,
      _ => VidiaStatusKind.info,
    };
