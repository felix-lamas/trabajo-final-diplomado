import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../controllers/session_controller.dart';
import '../widgets/vidia_logo.dart';
import '../widgets/vidia_section_title.dart';
import 'events_screen.dart';
import 'history_screen.dart';
import 'my_certificates_screen.dart';
import 'my_registrations_screen.dart';
import 'profile_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final session = context.watch<SessionController>();
    return Scaffold(
      appBar: AppBar(
        title: const VidiaLogo(height: 38),
        actions: [
          IconButton(
            tooltip: 'Mi perfil',
            onPressed: () => Navigator.push(
              context,
              MaterialPageRoute<void>(builder: (_) => const ProfileScreen()),
            ),
            icon: const Icon(Icons.person_outline_rounded),
          ),
          IconButton(
            tooltip: 'Cerrar sesión',
            onPressed: session.busy ? null : session.logout,
            icon: const Icon(Icons.logout_rounded),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 24, 20, 32),
        children: [
          Container(
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [
                  Color(0xFF302C78),
                  Color(0xFF7451D9),
                  Color(0xFFD44BC8)
                ],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(28),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Donde los eventos cobran vida',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 22,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: 10),
                Text(
                  'Hola, ${session.user?.nombres ?? 'participante'}.',
                  style: const TextStyle(color: Color(0xFFE6E0FF)),
                ),
              ],
            ),
          ),
          if (session.user?.organizadorPendiente ?? false) ...[
            const SizedBox(height: 20),
            const Card(
              child: ListTile(
                leading: Icon(Icons.hourglass_top_rounded),
                title: Text('Solicitud de organizador pendiente'),
                subtitle: Text(
                  'Tu cuenta conserva el rol Usuario hasta que el administrador resuelva la solicitud.',
                ),
              ),
            ),
          ],
          if (session.user?.isUsuario ?? false) ...[
            const SizedBox(height: 28),
            const VidiaSectionTitle(
              title: 'Explora Vidia',
              subtitle: 'Consulta eventos e inscripciones de tu cuenta.',
            ),
            const SizedBox(height: 16),
            _MenuCard(
              icon: Icons.event_outlined,
              title: 'Eventos publicados',
              subtitle: 'Descubre los eventos disponibles.',
              onTap: () => Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const EventsScreen()),
              ),
            ),
            _MenuCard(
              icon: Icons.how_to_reg_outlined,
              title: 'Mis inscripciones',
              subtitle: 'Revisa tus inscripciones reales.',
              onTap: () => Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => const MyRegistrationsScreen(),
                ),
              ),
            ),
            _MenuCard(
              icon: Icons.workspace_premium_outlined,
              title: 'Mis certificados',
              subtitle: 'Consulta y descarga tus certificados.',
              onTap: () => Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => const MyCertificatesScreen(),
                ),
              ),
            ),
            _MenuCard(
              icon: Icons.history_rounded,
              title: 'Historial',
              subtitle: 'Revisa inscripciones, pagos, asistencias y certificados.',
              onTap: () => Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const HistoryScreen()),
              ),
            ),
          ] else ...[
            const SizedBox(height: 28),
            const Card(
              child: ListTile(
                leading: Icon(Icons.info_outline_rounded),
                title: Text('Canal móvil para participantes'),
                subtitle: Text(
                  'Las funciones móviles de administración y organización no forman parte de esta aplicación.',
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _MenuCard extends StatelessWidget {
  const _MenuCard({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(bottom: 12),
        child: Card(
          child: ListTile(
            leading: CircleAvatar(child: Icon(icon)),
            title: Text(title),
            subtitle: Text(subtitle),
            trailing: const Icon(Icons.chevron_right_rounded),
            onTap: onTap,
          ),
        ),
      );
}
