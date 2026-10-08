import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../services/api_exception.dart';
import '../services/auth_service.dart';
import 'email_auth_screens.dart';

class RegistrationScreen extends StatefulWidget {
  const RegistrationScreen({super.key});

  @override
  State<RegistrationScreen> createState() => _RegistrationScreenState();
}

class _RegistrationScreenState extends State<RegistrationScreen> {
  final _form = GlobalKey<FormState>();
  final _values = List.generate(8, (_) => TextEditingController());
  String _tipoUsuario = 'INTERNO';
  bool _busy = false;
  String? _message;

  TextEditingController get _names => _values[0];
  TextEditingController get _surnames => _values[1];
  TextEditingController get _email => _values[2];
  TextEditingController get _ci => _values[3];
  TextEditingController get _ru => _values[4];
  TextEditingController get _phone => _values[5];
  TextEditingController get _password => _values[6];
  TextEditingController get _confirmation => _values[7];

  @override
  void dispose() {
    for (final controller in _values) {
      controller.dispose();
    }
    super.dispose();
  }

  String? _required(String? value, String label) =>
      value == null || value.trim().isEmpty ? 'Ingresa $label.' : null;

  String? _passwordError(String? value) {
    if (value == null || value.length < 8) return 'Usa al menos 8 caracteres.';
    if (!RegExp(
      r'^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$',
    ).hasMatch(value)) {
      return 'Incluye mayúscula, minúscula, número y símbolo.';
    }
    if (value != _confirmation.text) return 'Las contraseñas no coinciden.';
    return null;
  }

  Future<void> _submit() async {
    if (!_form.currentState!.validate()) return;
    setState(() {
      _busy = true;
      _message = null;
    });
    try {
      await context.read<AuthService>().register({
        'nombres': _names.text.trim(),
        'apellidos': _surnames.text.trim(),
        'correoElectronico': _email.text.trim(),
        'ci': _ci.text.trim(),
        'ru': _tipoUsuario == 'INTERNO' ? _ru.text.trim() : '',
        'celular': _phone.text.trim(),
        'contrasena': _password.text,
        'confirmacionContrasena': _confirmation.text,
        'tipoUsuario': _tipoUsuario,
      });
      if (!mounted) return;
      final email = _email.text.trim();
      Navigator.of(context).pushReplacement(
        MaterialPageRoute<void>(builder: (_) => CheckEmailScreen(email: email)),
      );
    } catch (error) {
      if (mounted) setState(() => _message = _readableError(error));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Crear cuenta')),
    body: SafeArea(
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 520),
          child: Form(
            key: _form,
            child: ListView(
              padding: const EdgeInsets.all(20),
              children: [
                _field(
                  _names,
                  'Nombres',
                  validator: (v) => _required(v, 'tus nombres'),
                ),
                _field(
                  _surnames,
                  'Apellidos',
                  validator: (v) => _required(v, 'tus apellidos'),
                ),
                _field(
                  _email,
                  'Correo electrónico',
                  keyboardType: TextInputType.emailAddress,
                  validator: (v) => v != null && v.trim().contains('@')
                      ? null
                      : 'Ingresa un correo válido.',
                ),
                _field(
                  _ci,
                  'CI',
                  validator: (v) =>
                      v != null &&
                          RegExp(r'^[A-Za-z0-9-]{4,20}$').hasMatch(v.trim())
                      ? null
                      : 'El CI debe tener entre 4 y 20 caracteres válidos.',
                ),
                DropdownButtonFormField<String>(
                  initialValue: _tipoUsuario,
                  decoration: const InputDecoration(
                    labelText: 'Tipo de cuenta',
                  ),
                  items: const [
                    DropdownMenuItem(
                      value: 'INTERNO',
                      child: Text('Comunidad UAJMS'),
                    ),
                    DropdownMenuItem(
                      value: 'EXTERNO',
                      child: Text('Usuario externo'),
                    ),
                  ],
                  onChanged: _busy
                      ? null
                      : (value) =>
                            setState(() => _tipoUsuario = value ?? 'INTERNO'),
                ),
                if (_tipoUsuario == 'INTERNO')
                  _field(
                    _ru,
                    'RU',
                    validator: (v) =>
                        v != null &&
                            RegExp(r'^[A-Za-z0-9-]{4,20}$').hasMatch(v.trim())
                        ? null
                        : 'El RU es obligatorio para usuarios UAJMS.',
                  ),
                _field(
                  _phone,
                  'Celular',
                  keyboardType: TextInputType.phone,
                  validator: (v) =>
                      v != null &&
                          RegExp(r'^[0-9+ -]{7,20}$').hasMatch(v.trim())
                      ? null
                      : 'Ingresa un celular válido.',
                ),
                _field(
                  _password,
                  'Contraseña',
                  obscure: true,
                  validator: _passwordError,
                ),
                _field(
                  _confirmation,
                  'Confirmar contraseña',
                  obscure: true,
                  validator: (v) => v == _password.text
                      ? null
                      : 'Las contraseñas no coinciden.',
                ),
                if (_message != null) ...[
                  const SizedBox(height: 12),
                  Text(
                    _message!,
                    style: TextStyle(
                      color: Theme.of(context).colorScheme.error,
                    ),
                  ),
                ],
                const SizedBox(height: 18),
                FilledButton(
                  onPressed: _busy ? null : _submit,
                  child: Text(_busy ? 'Enviando…' : 'Registrarme'),
                ),
                TextButton(
                  onPressed: _busy
                      ? null
                      : () => Navigator.push(
                          context,
                          MaterialPageRoute<void>(
                            builder: (_) => EmailVerificationPendingScreen(
                              initialEmail: _email.text.trim(),
                            ),
                          ),
                        ),
                  child: const Text('Reenviar verificación'),
                ),
              ],
            ),
          ),
        ),
      ),
    ),
  );
}

Widget _field(
  TextEditingController controller,
  String label, {
  bool obscure = false,
  TextInputType? keyboardType,
  String? Function(String?)? validator,
}) => Padding(
  padding: const EdgeInsets.only(bottom: 12),
  child: TextFormField(
    controller: controller,
    obscureText: obscure,
    keyboardType: keyboardType,
    decoration: InputDecoration(labelText: label),
    validator:
        validator ??
        (value) => value == null || value.trim().isEmpty
            ? 'Este campo es obligatorio.'
            : null,
  ),
);

String _readableError(Object error) => error is ApiException
    ? error.message
    : 'No fue posible conectar con el servidor. Inténtalo nuevamente.';
