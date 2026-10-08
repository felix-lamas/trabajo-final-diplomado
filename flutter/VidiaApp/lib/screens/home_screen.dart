import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../controllers/account_overview_controller.dart';
import '../controllers/session_controller.dart';
import '../repositories/asistencia_repository.dart';
import '../repositories/certificado_repository.dart';
import '../repositories/evento_repository.dart';
import '../repositories/inscripcion_repository.dart';
import '../repositories/pago_repository.dart';
import 'activity_screen.dart';
import 'dashboard_screen.dart';
import 'events_screen.dart';
import 'profile_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});
  @override
  Widget build(BuildContext context) {
    final participant =
        context.watch<SessionController>().user?.isUsuario ?? false;
    if (!participant) return const _HomeNavigation(participant: false);
    return ChangeNotifierProvider(
      create: (context) => AccountOverviewController(
        eventsRepository: context.read<EventoRepository>(),
        registrationsRepository: context.read<InscripcionRepository>(),
        paymentsRepository: context.read<PagoRepository>(),
        attendanceRepository: context.read<AsistenciaRepository>(),
        certificatesRepository: context.read<CertificadoRepository>(),
      )..refresh(),
      child: const _HomeNavigation(participant: true),
    );
  }
}

class _HomeNavigation extends StatefulWidget {
  const _HomeNavigation({required this.participant});
  final bool participant;
  @override
  State<_HomeNavigation> createState() => _HomeNavigationState();
}

class _HomeNavigationState extends State<_HomeNavigation> {
  int _selected = 0;
  final _visited = <int>{0};
  void _select(int index) {
    FocusManager.instance.primaryFocus?.unfocus();
    setState(() {
      _selected = index;
      _visited.add(index);
    });
    if (widget.participant && (index == 0 || index == 2)) {
      context.read<AccountOverviewController>().refresh();
    }
  }

  @override
  Widget build(BuildContext context) => PopScope(
    canPop: _selected == 0,
    onPopInvokedWithResult: (didPop, result) {
      if (!didPop && _selected != 0) _select(0);
    },
    child: Scaffold(
      body: IndexedStack(
        index: _selected,
        children:
            <Widget>[
                  widget.participant
                      ? DashboardScreen(onDiscover: () => _select(1))
                      : const _ParticipantNotice(),
                  _visited.contains(1)
                      ? widget.participant
                            ? const EventsScreen(embedded: true)
                            : const _ParticipantNotice()
                      : const SizedBox.shrink(),
                  _visited.contains(2)
                      ? widget.participant
                            ? const ActivityScreen()
                            : const _ParticipantNotice()
                      : const SizedBox.shrink(),
                  _visited.contains(3)
                      ? const ProfileScreen(embedded: true)
                      : const SizedBox.shrink(),
                ].indexed
                .map(
                  (entry) => Offstage(
                    offstage: entry.$1 != _selected,
                    child: TickerMode(
                      enabled: entry.$1 == _selected,
                      child: entry.$2,
                    ),
                  ),
                )
                .toList(),
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _selected,
        onDestinationSelected: _select,
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.home_outlined),
            selectedIcon: Icon(Icons.home_rounded),
            label: 'Inicio',
          ),
          NavigationDestination(
            icon: Icon(Icons.event_outlined),
            selectedIcon: Icon(Icons.event_rounded),
            label: 'Eventos',
          ),
          NavigationDestination(
            icon: Icon(Icons.space_dashboard_outlined),
            selectedIcon: Icon(Icons.space_dashboard_rounded),
            label: 'Actividad',
          ),
          NavigationDestination(
            icon: Icon(Icons.person_outline_rounded),
            selectedIcon: Icon(Icons.person_rounded),
            label: 'Perfil',
          ),
        ],
      ),
    ),
  );
}

class _ParticipantNotice extends StatelessWidget {
  const _ParticipantNotice();
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(
      title: const Text('Vidia'),
      automaticallyImplyLeading: false,
    ),
    body: const SafeArea(
      child: Center(
        child: Padding(
          padding: EdgeInsets.all(24),
          child: Text(
            'Canal móvil para participantes. Las funciones de administración y organización no forman parte de esta aplicación.',
          ),
        ),
      ),
    ),
  );
}
