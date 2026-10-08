import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../controllers/session_controller.dart';
import '../widgets/vidia_logo.dart';
import 'auth_flow_screens.dart';
import 'email_auth_screens.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _email = TextEditingController();
  final _password = TextEditingController();
  bool _hidePassword = true;

  @override
  void dispose() {
    _email.dispose();
    _password.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    FocusScope.of(context).unfocus();
    final session = context.read<SessionController>();
    final success = await session.login(
      email: _email.text,
      password: _password.text,
    );
    if (!mounted || success || session.unverifiedEmail == null) return;
    Navigator.of(context).push(MaterialPageRoute<void>(
        builder: (_) => EmailVerificationPendingScreen(
            initialEmail: session.unverifiedEmail!)));
  }

  @override
  Widget build(BuildContext context) {
    final session = context.watch<SessionController>();
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            colors: [Color(0xFFF8F7FC), Color(0xFFEDEBFF), Color(0xFFFDF5FB)],
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
          ),
        ),
        child: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(24),
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 440),
                child: Card(
                  child: Padding(
                    padding: const EdgeInsets.all(28),
                    child: Form(
                      key: _formKey,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          const Center(child: VidiaLogo(height: 150)),
                          const SizedBox(height: 12),
                          Text(
                            'Donde los eventos cobran vida',
                            textAlign: TextAlign.center,
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                          const SizedBox(height: 28),
                          TextFormField(
                            controller: _email,
                            enabled: !session.busy,
                            keyboardType: TextInputType.emailAddress,
                            autofillHints: const [AutofillHints.username],
                            decoration: const InputDecoration(
                              labelText: 'Correo electrónico',
                              prefixIcon: Icon(Icons.alternate_email_rounded),
                            ),
                            validator: (value) =>
                                value == null || !value.trim().contains('@')
                                    ? 'Ingresa un correo válido.'
                                    : null,
                          ),
                          const SizedBox(height: 14),
                          TextFormField(
                            controller: _password,
                            enabled: !session.busy,
                            obscureText: _hidePassword,
                            autofillHints: const [AutofillHints.password],
                            decoration: InputDecoration(
                              labelText: 'Contraseña',
                              prefixIcon:
                                  const Icon(Icons.lock_outline_rounded),
                              suffixIcon: IconButton(
                                onPressed: () => setState(
                                  () => _hidePassword = !_hidePassword,
                                ),
                                icon: Icon(
                                  _hidePassword
                                      ? Icons.visibility_outlined
                                      : Icons.visibility_off_outlined,
                                ),
                              ),
                            ),
                            validator: (value) => value == null || value.isEmpty
                                ? 'Ingresa tu contraseña.'
                                : null,
                            onFieldSubmitted: (_) =>
                                session.busy ? null : _submit(),
                          ),
                          if (session.message != null) ...[
                            const SizedBox(height: 16),
                            Text(
                              session.message!,
                              textAlign: TextAlign.center,
                              style: TextStyle(
                                color: Theme.of(context).colorScheme.error,
                              ),
                            ),
                          ],
                          const SizedBox(height: 24),
                          FilledButton.icon(
                            onPressed: session.busy ? null : _submit,
                            icon: session.busy
                                ? const SizedBox.square(
                                    dimension: 18,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                    ),
                                  )
                                : const Icon(Icons.login_rounded),
                            label: Text(
                              session.busy ? 'Iniciando sesión...' : 'Ingresar',
                            ),
                          ),
                          const SizedBox(height: 8),
                          Wrap(
                            alignment: WrapAlignment.center,
                            children: [
                              TextButton(
                                onPressed: session.busy
                                    ? null
                                    : () => Navigator.of(context).push(
                                        MaterialPageRoute<void>(
                                            builder: (_) =>
                                                EmailVerificationPendingScreen(
                                                    initialEmail:
                                                        _email.text.trim()))),
                                child: const Text('Reenviar verificación'),
                              ),
                              TextButton(
                                onPressed: session.busy
                                    ? null
                                    : () => Navigator.push(
                                          context,
                                          MaterialPageRoute<void>(
                                            builder: (_) =>
                                                const RegistrationScreen(),
                                          ),
                                        ),
                                child: const Text('Crear cuenta'),
                              ),
                              TextButton(
                                onPressed: session.busy
                                    ? null
                                    : () => Navigator.push(
                                          context,
                                          MaterialPageRoute<void>(
                                            builder: (_) =>
                                                PasswordRecoveryScreen(
                                                    initialEmail:
                                                        _email.text.trim()),
                                          ),
                                        ),
                                child: const Text('¿Olvidaste tu contraseña?'),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
