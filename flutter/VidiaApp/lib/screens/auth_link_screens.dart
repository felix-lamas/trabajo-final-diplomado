import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../controllers/session_controller.dart';
import '../services/auth_link_coordinator.dart';
import '../services/auth_service.dart';
import 'email_auth_screens.dart';

Route<void> authLinkRoute(AuthLink link) => MaterialPageRoute<void>(
  // Never put the query string or credential in RouteSettings.
  settings: RouteSettings(name: link.path),
  builder: (_) => link.action == AuthLinkAction.verifyEmail
      ? VerifyEmailLinkScreen(token: link.token)
      : ResetPasswordScreen(token: link.token),
);

class VerifyEmailLinkScreen extends StatefulWidget {
  const VerifyEmailLinkScreen({super.key, required this.token});
  final String token;
  @override
  State<VerifyEmailLinkScreen> createState() => _VerifyEmailLinkState();
}

class _VerifyEmailLinkState extends State<VerifyEmailLinkScreen> {
  late String _token;
  bool _busy = false;
  bool _success = false;
  bool _retry = false;
  String? _message;
  @override
  void initState() {
    super.initState();
    _token = widget.token;
    _verify();
  }

  @override
  void dispose() {
    _token = '';
    super.dispose();
  }

  Future<void> _verify() async {
    if (_busy || _success) return;
    if (_token.trim().isEmpty) {
      setState(
        () => _message =
            'El enlace no contiene un token válido. Solicita otro correo.',
      );
      return;
    }
    setState(() {
      _busy = true;
      _retry = false;
      _message = null;
    });
    try {
      await context.read<AuthService>().verifyEmail(_token);
      if (mounted) {
        setState(() {
          _success = true;
          _token = '';
          _message = 'Correo verificado. Ya puedes iniciar sesión.';
        });
      }
    } catch (error) {
      if (mounted) {
        setState(() {
          _message = authLinkError(error);
          _retry = canRetryAuthLink(error);
          if (!_retry) _token = '';
        });
      }
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Verificar correo')),
    body: ListView(
      padding: const EdgeInsets.all(24),
      children: [
        if (_busy) ...[
          const CircularProgressIndicator(),
          const Text('Verificando correo…'),
        ],
        if (_message != null) Text(_message!),
        if (_retry)
          FilledButton(
            onPressed: _busy ? null : _verify,
            child: const Text('Reintentar'),
          ),
        if (!_busy && !_success)
          TextButton(
            onPressed: () => Navigator.of(context).pushReplacement(
              MaterialPageRoute<void>(
                builder: (_) => const EmailVerificationPendingScreen(),
              ),
            ),
            child: const Text('Solicitar otro enlace'),
          ),
        TextButton(
          onPressed: () => returnToLogin(context),
          child: const Text('Volver al login'),
        ),
      ],
    ),
  );
}

class ResetPasswordScreen extends StatefulWidget {
  const ResetPasswordScreen({super.key, required this.token});
  final String token;
  @override
  State<ResetPasswordScreen> createState() => _ResetPasswordState();
}

class _ResetPasswordState extends State<ResetPasswordScreen> {
  final _form = GlobalKey<FormState>();
  final _password = TextEditingController();
  final _confirmation = TextEditingController();
  late String _token;
  bool _busy = false;
  bool _success = false;
  String? _message;
  @override
  void initState() {
    super.initState();
    _token = widget.token;
    if (_token.trim().isEmpty) {
      _token = '';
      _message = 'El enlace no contiene un token válido. Solicita otro correo.';
    }
  }

  @override
  void dispose() {
    _token = '';
    _password.dispose();
    _confirmation.dispose();
    super.dispose();
  }

  Future<void> _reset() async {
    if (_busy ||
        _success ||
        _token.isEmpty ||
        !_form.currentState!.validate()) {
      return;
    }
    setState(() {
      _busy = true;
      _message = null;
    });
    try {
      await context.read<AuthService>().resetPassword(
        token: _token,
        password: _password.text,
        confirmation: _confirmation.text,
      );
      if (!mounted) return;
      context.read<SessionController>().accountPasswordReset();
      setState(() {
        _success = true;
        _token = '';
        _password.clear();
        _confirmation.clear();
        _message =
            'Contraseña restablecida. Inicia sesión con tu nueva contraseña.';
      });
    } catch (error) {
      if (mounted) {
        setState(() {
          _message = authLinkError(error);
          if (!canRetryAuthLink(error)) _token = '';
        });
      }
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Nueva contraseña')),
    body: Form(
      key: _form,
      child: ListView(
        padding: const EdgeInsets.all(24),
        children: [
          if (!_success && _token.isNotEmpty) ...[
            TextFormField(
              controller: _password,
              enabled: !_busy,
              obscureText: true,
              decoration: const InputDecoration(labelText: 'Nueva contraseña'),
              validator: (v) =>
                  v != null &&
                      RegExp(
                        r'^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$',
                      ).hasMatch(v)
                  ? null
                  : 'Usa 8 caracteres con mayúscula, minúscula, número y símbolo.',
            ),
            TextFormField(
              controller: _confirmation,
              enabled: !_busy,
              obscureText: true,
              decoration: const InputDecoration(
                labelText: 'Confirmar nueva contraseña',
              ),
              validator: (v) =>
                  v == _password.text ? null : 'Las contraseñas no coinciden.',
            ),
            FilledButton(
              onPressed: _busy ? null : _reset,
              child: Text(_busy ? 'Restableciendo…' : 'Restablecer contraseña'),
            ),
          ],
          if (_message != null) Text(_message!),
          if (!_success && _token.isEmpty)
            TextButton(
              onPressed: () => Navigator.of(context).pushReplacement(
                MaterialPageRoute<void>(
                  builder: (_) => const PasswordRecoveryScreen(),
                ),
              ),
              child: const Text('Solicitar recuperación'),
            ),
          TextButton(
            onPressed: _busy ? null : () => returnToLogin(context),
            child: const Text('Volver al login'),
          ),
        ],
      ),
    ),
  );
}
