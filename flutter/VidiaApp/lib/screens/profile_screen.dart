import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../controllers/session_controller.dart';
import '../services/api_exception.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) context.read<SessionController>().refreshProfile();
    });
  }

  Future<void> _changePassword() async {
    final form = GlobalKey<FormState>();
    final current = TextEditingController();
    final next = TextEditingController();
    final confirm = TextEditingController();
    String? error;
    final changed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: const Text('Cambiar contraseña'),
          content: Form(
            key: form,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                _passwordField(current, 'Contraseña actual'),
                _passwordField(next, 'Nueva contraseña', validator: (value) {
                  if (value == null ||
                      !RegExp(r'^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$')
                          .hasMatch(value)) {
                    return 'Usa 8 caracteres con mayúscula, minúscula, número y símbolo.';
                  }
                  return null;
                }),
                _passwordField(confirm, 'Confirmar contraseña',
                    validator: (value) => value == next.text
                        ? null
                        : 'Las contraseñas no coinciden.'),
                if (error != null)
                  Text(error!,
                      style: TextStyle(
                          color: Theme.of(context).colorScheme.error)),
              ],
            ),
          ),
          actions: [
            TextButton(
                onPressed: () => Navigator.pop(dialogContext, false),
                child: const Text('Cancelar')),
            FilledButton(
              onPressed: () async {
                if (!form.currentState!.validate()) return;
                try {
                  await context.read<SessionController>().changePassword(
                        currentPassword: current.text,
                        newPassword: next.text,
                        confirmation: confirm.text,
                      );
                  if (dialogContext.mounted) Navigator.pop(dialogContext, true);
                } catch (exception) {
                  setDialogState(() => error = exception is ApiException
                      ? exception.message
                      : 'No se pudo cambiar la contraseña.');
                }
              },
              child: const Text('Cambiar'),
            ),
          ],
        ),
      ),
    );
    current.dispose();
    next.dispose();
    confirm.dispose();
    if (changed == true && mounted) {
      Navigator.of(context).popUntil((route) => route.isFirst);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
            content: Text('Contraseña cambiada. Inicia sesión nuevamente.')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final session = context.watch<SessionController>();
    final user = session.user;
    if (user == null) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    return Scaffold(
      appBar: AppBar(title: const Text('Mi perfil')),
      body: RefreshIndicator(
        onRefresh: session.refreshProfile,
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            if (session.busy) const LinearProgressIndicator(),
            _detail('Nombre', user.nombreCompleto),
            _detail('Correo', user.correoElectronico),
            _detail('Correo verificado', user.correoVerificado ? 'Sí' : 'No'),
            _detail('Tipo de usuario', user.tipoUsuario ?? '—'),
            _detail('Roles', user.roles.join(', ')),
            _detail('Estado de solicitud de organizador',
                user.estadoSolicitudOrganizador),
            if (user.ci != null) _detail('CI', user.ci!),
            if (user.ru != null && user.ru!.isNotEmpty) _detail('RU', user.ru!),
            if (user.celular != null) _detail('Celular', user.celular!),
            if (user.organizadorPendiente)
              const Card(
                child: ListTile(
                  leading: Icon(Icons.hourglass_top_rounded),
                  title: Text('Solicitud pendiente'),
                  subtitle: Text(
                      'La cuenta conserva los permisos de USUARIO hasta la aprobación del administrador.'),
                ),
              ),
            if (session.message != null)
              Padding(
                  padding: const EdgeInsets.symmetric(vertical: 12),
                  child: Text(session.message!)),
            const SizedBox(height: 12),
            OutlinedButton.icon(
              onPressed: session.busy ? null : _changePassword,
              icon: const Icon(Icons.password_rounded),
              label: const Text('Cambiar contraseña'),
            ),
          ],
        ),
      ),
    );
  }
}

Widget _detail(String label, String value) => Card(
      child: ListTile(
          title: Text(label), subtitle: Text(value.isEmpty ? '—' : value)),
    );

Widget _passwordField(
  TextEditingController controller,
  String label, {
  String? Function(String?)? validator,
}) =>
    Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: TextFormField(
        controller: controller,
        obscureText: true,
        decoration: InputDecoration(labelText: label),
        validator: validator ??
            (value) =>
                value == null || value.isEmpty ? 'Campo obligatorio.' : null,
      ),
    );
