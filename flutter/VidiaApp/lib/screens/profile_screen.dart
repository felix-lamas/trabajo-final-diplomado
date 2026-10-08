import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../controllers/session_controller.dart';
import '../services/api_exception.dart';
import 'app_information_screen.dart';
import 'email_auth_screens.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key, this.embedded = false});
  final bool embedded;

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
          content: SingleChildScrollView(
            child: Form(
              key: form,
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  _passwordField(current, 'Contraseña actual'),
                  _passwordField(
                    next,
                    'Nueva contraseña',
                    validator: (value) {
                      if (value == null ||
                          !RegExp(
                            r'^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$',
                          ).hasMatch(value)) {
                        return 'Usa 8 caracteres con mayúscula, minúscula, número y símbolo.';
                      }
                      return null;
                    },
                  ),
                  _passwordField(
                    confirm,
                    'Confirmar contraseña',
                    validator: (value) => value == next.text
                        ? null
                        : 'Las contraseñas no coinciden.',
                  ),
                  if (error != null)
                    Text(
                      error!,
                      style: TextStyle(
                        color: Theme.of(context).colorScheme.error,
                      ),
                    ),
                ],
              ),
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext, false),
              child: const Text('Cancelar'),
            ),
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
                  setDialogState(
                    () => error = exception is ApiException
                        ? exception.message
                        : 'No se pudo cambiar la contraseña.',
                  );
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
          content: Text('Contraseña cambiada. Inicia sesión nuevamente.'),
        ),
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
    void information(AppInformation section) => Navigator.push(
      context,
      MaterialPageRoute<void>(
        builder: (_) => AppInformationScreen(section: section),
      ),
    );
    return Scaffold(
      appBar: AppBar(
        title: const Text('Perfil'),
        automaticallyImplyLeading: !widget.embedded,
      ),
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: session.refreshProfile,
          child: ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.all(20),
            children: [
              if (session.busy) const LinearProgressIndicator(),
              const CircleAvatar(
                radius: 36,
                child: Icon(Icons.person_outline_rounded, size: 40),
              ),
              const SizedBox(height: 16),
              Text(
                user.nombreCompleto,
                textAlign: TextAlign.center,
                style: Theme.of(context).textTheme.headlineSmall,
              ),
              const SizedBox(height: 4),
              Text(user.correoElectronico, textAlign: TextAlign.center),
              if (user.ci != null)
                Text('CI: ${user.ci}', textAlign: TextAlign.center),
              if (user.ru?.isNotEmpty ?? false)
                Text('RU: ${user.ru}', textAlign: TextAlign.center),
              const SizedBox(height: 24),
              Text('Cuenta', style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 8),
              Card(
                child: Column(
                  children: [
                    if (user.celular != null)
                      ListTile(
                        leading: const Icon(Icons.phone_outlined),
                        title: const Text('Celular'),
                        subtitle: Text(user.celular!),
                      ),
                    ListTile(
                      leading: Icon(
                        user.correoVerificado
                            ? Icons.verified_outlined
                            : Icons.mark_email_unread_outlined,
                      ),
                      title: const Text('Verificación de correo'),
                      subtitle: Text(
                        user.correoVerificado
                            ? 'Correo verificado'
                            : 'Correo no verificado',
                      ),
                      trailing: user.correoVerificado
                          ? null
                          : const Icon(Icons.chevron_right_rounded),
                      onTap: user.correoVerificado
                          ? null
                          : () => Navigator.push(
                              context,
                              MaterialPageRoute<void>(
                                builder: (_) => EmailVerificationPendingScreen(
                                  initialEmail: user.correoElectronico,
                                ),
                              ),
                            ),
                    ),
                    const Divider(height: 1),
                    ListTile(
                      leading: const Icon(Icons.password_rounded),
                      title: const Text('Cambiar contraseña'),
                      trailing: const Icon(Icons.chevron_right_rounded),
                      onTap: session.busy ? null : _changePassword,
                    ),
                    ExpansionTile(
                      title: const Text('Información de la cuenta'),
                      children: [
                        _detail('Tipo de usuario', user.tipoUsuario ?? '—'),
                        _detail('Roles', user.roles.join(', ')),
                        _detail(
                          'Estado de solicitud de organizador',
                          user.estadoSolicitudOrganizador,
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              if (user.organizadorPendiente)
                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 12),
                  child: Text(
                    'Solicitud pendiente: tu cuenta conserva los permisos de Usuario hasta la aprobación del administrador.',
                  ),
                ),
              if (session.message != null)
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 12),
                  child: Text(session.message!),
                ),
              const SizedBox(height: 20),
              Text(
                'Ayuda e información',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 8),
              Card(
                child: Column(
                  children: [
                    ListTile(
                      leading: const Icon(Icons.help_outline_rounded),
                      title: const Text('Ayuda'),
                      trailing: const Icon(Icons.chevron_right_rounded),
                      onTap: () => information(AppInformation.help),
                    ),
                    ListTile(
                      leading: const Icon(Icons.contact_support_outlined),
                      title: const Text('Contacto'),
                      trailing: const Icon(Icons.chevron_right_rounded),
                      onTap: () => information(AppInformation.contact),
                    ),
                    ListTile(
                      leading: const Icon(Icons.info_outline_rounded),
                      title: const Text('Acerca de Vidia'),
                      trailing: const Icon(Icons.chevron_right_rounded),
                      onTap: () => information(AppInformation.about),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              OutlinedButton.icon(
                onPressed: session.busy
                    ? null
                    : () async {
                        await session.logout();
                        if (context.mounted) {
                          Navigator.of(
                            context,
                          ).popUntil((route) => route.isFirst);
                        }
                      },
                icon: const Icon(Icons.logout_rounded),
                label: const Text('Cerrar sesión'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

Widget _detail(String label, String value) => Card(
  child: ListTile(
    title: Text(label),
    subtitle: Text(value.isEmpty ? '—' : value),
  ),
);

Widget _passwordField(
  TextEditingController controller,
  String label, {
  String? Function(String?)? validator,
}) => Padding(
  padding: const EdgeInsets.only(bottom: 12),
  child: TextFormField(
    controller: controller,
    obscureText: true,
    decoration: InputDecoration(labelText: label),
    validator:
        validator ??
        (value) => value == null || value.isEmpty ? 'Campo obligatorio.' : null,
  ),
);
