import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../services/api_exception.dart';
import '../services/auth_service.dart';

void returnToLogin(BuildContext context) =>
    Navigator.of(context).popUntil((route) => route.isFirst);

class CheckEmailScreen extends StatelessWidget {
  const CheckEmailScreen({
    super.key,
    required this.email,
    this.recovery = false,
  });
  final String email;
  final bool recovery;

  @override
  Widget build(BuildContext context) => recovery
      ? Scaffold(
          appBar: AppBar(title: const Text('Revisa tu correo')),
          body: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              children: [
                Text(email),
                const Text(
                  'Si el correo corresponde a una cuenta, se enviaron instrucciones. Abre el enlace recibido para establecer tu nueva contraseña.',
                ),
                TextButton(
                  onPressed: () => returnToLogin(context),
                  child: const Text('Volver al login'),
                ),
              ],
            ),
          ),
        )
      : EmailVerificationPendingScreen(initialEmail: email);
}

class EmailVerificationPendingScreen extends StatefulWidget {
  const EmailVerificationPendingScreen({super.key, this.initialEmail = ''});
  final String initialEmail;
  @override
  State<EmailVerificationPendingScreen> createState() =>
      _EmailVerificationPendingState();
}

class _EmailVerificationPendingState
    extends State<EmailVerificationPendingScreen> {
  final _form = GlobalKey<FormState>();
  late final _email = TextEditingController(text: widget.initialEmail);
  bool _busy = false;
  String? _message;
  @override
  void dispose() {
    _email.dispose();
    super.dispose();
  }

  Future<void> _resend() async {
    if (_busy || !_form.currentState!.validate()) return;
    setState(() {
      _busy = true;
      _message = null;
    });
    try {
      await context.read<AuthService>().resendVerification(_email.text);
      if (mounted) {
        setState(
          () => _message =
              'Si la cuenta requiere verificación, se procesó el reenvío.',
        );
      }
    } catch (_) {
      if (mounted) {
        setState(
          () => _message =
              'No fue posible reenviar el correo. Inténtalo nuevamente.',
        );
      }
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Revisa tu correo')),
    body: Form(
      key: _form,
      child: ListView(
        padding: const EdgeInsets.all(24),
        children: [
          const Text(
            'Tu correo todavía no está verificado. Abre el enlace recibido para verificarlo y después inicia sesión.',
          ),
          if (widget.initialEmail.isNotEmpty)
            Text('Correo destino: ${widget.initialEmail}'),
          TextFormField(
            controller: _email,
            enabled: !_busy,
            keyboardType: TextInputType.emailAddress,
            decoration: const InputDecoration(labelText: 'Correo electrónico'),
            validator: emailError,
          ),
          FilledButton(
            onPressed: _busy ? null : _resend,
            child: Text(_busy ? 'Enviando…' : 'Reenviar verificación'),
          ),
          if (_message != null) Text(_message!),
          TextButton(
            onPressed: () => returnToLogin(context),
            child: const Text('Volver al login'),
          ),
        ],
      ),
    ),
  );
}

class PasswordRecoveryScreen extends StatefulWidget {
  const PasswordRecoveryScreen({super.key, this.initialEmail = ''});
  final String initialEmail;
  @override
  State<PasswordRecoveryScreen> createState() => _PasswordRecoveryState();
}

class _PasswordRecoveryState extends State<PasswordRecoveryScreen> {
  final _form = GlobalKey<FormState>();
  late final _email = TextEditingController(text: widget.initialEmail);
  bool _busy = false;
  String? _error;
  @override
  void dispose() {
    _email.dispose();
    super.dispose();
  }

  Future<void> _request() async {
    if (_busy || !_form.currentState!.validate()) return;
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      await context.read<AuthService>().requestPasswordReset(_email.text);
      if (!mounted) return;
      final email = _email.text.trim();
      Navigator.of(context).pushReplacement(
        MaterialPageRoute<void>(
          builder: (_) => CheckEmailScreen(email: email, recovery: true),
        ),
      );
    } catch (_) {
      if (mounted) {
        setState(
          () => _error =
              'No fue posible solicitar la recuperación. Inténtalo nuevamente.',
        );
      }
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Recuperar contraseña')),
    body: Form(
      key: _form,
      child: ListView(
        padding: const EdgeInsets.all(24),
        children: [
          TextFormField(
            controller: _email,
            enabled: !_busy,
            keyboardType: TextInputType.emailAddress,
            decoration: const InputDecoration(labelText: 'Correo electrónico'),
            validator: emailError,
          ),
          FilledButton(
            onPressed: _busy ? null : _request,
            child: Text(_busy ? 'Enviando…' : 'Solicitar recuperación'),
          ),
          if (_error != null) Text(_error!),
        ],
      ),
    ),
  );
}

String? emailError(String? value) =>
    value != null &&
        RegExp(r'^[^\s@]+@[^\s@]+\.[^\s@]+$').hasMatch(value.trim())
    ? null
    : 'Ingresa un correo válido.';

String authLinkError(Object error) => error is ApiException
    ? switch (error.code) {
        'EMAIL_VERIFICATION_TOKEN_EXPIRED' || 'PASSWORD_RESET_TOKEN_EXPIRED' =>
          'El enlace expiró. Solicita otro correo.',
        'EMAIL_VERIFICATION_TOKEN_USED' || 'PASSWORD_RESET_TOKEN_USED' =>
          'El enlace ya fue utilizado. Inicia sesión o solicita otro correo.',
        'EMAIL_VERIFICATION_TOKEN_INVALID' || 'PASSWORD_RESET_TOKEN_INVALID' =>
          'El enlace no es válido. Solicita otro correo.',
        _ =>
          error.statusCode == 0 || error.statusCode >= 500
              ? 'No fue posible conectar con el servidor. Inténtalo nuevamente.'
              : 'No fue posible completar la operación. Solicita otro correo.',
      }
    : 'No fue posible conectar con el servidor. Inténtalo nuevamente.';

bool canRetryAuthLink(Object error) =>
    error is ApiException && (error.statusCode == 0 || error.statusCode >= 500);
