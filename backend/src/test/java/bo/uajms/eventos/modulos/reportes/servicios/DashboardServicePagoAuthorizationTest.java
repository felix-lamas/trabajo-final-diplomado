package bo.uajms.eventos.modulos.reportes.servicios;

import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.certificados.repositorios.CertificadoRepository;
import bo.uajms.eventos.modulos.codigo_qr.repositorios.CodigoQrRepository;
import bo.uajms.eventos.modulos.encuestas.repositorios.EncuestaRepository;
import bo.uajms.eventos.modulos.encuestas.repositorios.RespuestaEncuestaRepository;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.reportes.dtos.DashboardEjecutivoResponse;
import bo.uajms.eventos.modulos.reportes.dtos.DashboardOperativoResponse;
import bo.uajms.eventos.modulos.reportes.dtos.ReporteDataResponse;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServicePagoAuthorizationTest {

    @Mock private EventoRepository eventoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private InscripcionRepository inscripcionRepository;
    @Mock private CertificadoRepository certificadoRepository;
    @Mock private PagoRepository pagoRepository;
    @Mock private AsistenciaRepository asistenciaRepository;
    @Mock private CodigoQrRepository codigoQrRepository;
    @Mock private EncuestaRepository encuestaRepository;
    @Mock private RespuestaEncuestaRepository respuestaEncuestaRepository;
    @Mock private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private DashboardService dashboardService;

    private Usuario organizadorA;
    private Pago pagoAValidado;
    private Pago pagoAPendiente;
    private Pago pagoBValidado;

    @BeforeEach
    void configurarEscenario() {
        organizadorA = usuario();
        Usuario organizadorB = usuario();
        pagoAValidado = pago(organizadorA, EstadoPago.VALIDADO, new BigDecimal("100.00"));
        pagoAPendiente = pago(organizadorA, EstadoPago.PENDIENTE, new BigDecimal("50.00"));
        pagoBValidado = pago(organizadorB, EstadoPago.VALIDADO, new BigDecimal("900.00"));
    }

    @Test
    void organizadorObtieneReporteSoloDePagosDeSusEventos() {
        autenticarOrganizadorA();
        when(pagoRepository.findByInscripcionEventoOrganizadorId(organizadorA.getId()))
                .thenReturn(List.of(pagoAValidado, pagoAPendiente));

        ReporteDataResponse reporte = dashboardService.generarReporte("pagos");

        assertEquals(2, reporte.getTotalRegistros());
        assertEquals(pagoAValidado.getId().toString(), reporte.getFilas().get(0).get("referencia"));
        verify(pagoRepository, never()).findAll();
    }

    @Test
    void administradorObtieneReporteGlobalDePagos() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(true);
        when(pagoRepository.findAll()).thenReturn(List.of(pagoAValidado, pagoBValidado));

        ReporteDataResponse reporte = dashboardService.generarReporte("pagos");

        assertEquals(2, reporte.getTotalRegistros());
        verify(pagoRepository).findAll();
    }

    @Test
    void dashboardEjecutivoDelOrganizadorExcluyeIngresosDeOtrosEventos() {
        autenticarOrganizadorA();
        when(pagoRepository.findByInscripcionEventoOrganizadorId(organizadorA.getId()))
                .thenReturn(List.of(pagoAValidado, pagoAPendiente));

        DashboardEjecutivoResponse dashboard = dashboardService.obtenerDashboardEjecutivo();

        assertEquals(new BigDecimal("100.00"), dashboard.getIngresosGenerados());
        verify(pagoRepository, never()).findAll();
    }

    @Test
    void dashboardOperativoDelOrganizadorExcluyeConteosDeOtrosEventos() {
        autenticarOrganizadorA();
        when(pagoRepository.findByInscripcionEventoOrganizadorId(organizadorA.getId()))
                .thenReturn(List.of(pagoAValidado, pagoAPendiente));

        DashboardOperativoResponse dashboard = dashboardService.obtenerDashboardOperativo();

        assertEquals(1, dashboard.getPagosPendientes());
        assertEquals(1, dashboard.getPagosValidados());
        verify(pagoRepository, never()).findAll();
    }

    @Test
    void personalControlConservaConteosGlobalesExistentes() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(usuarioAutenticadoService.tieneRol("PERSONAL_CONTROL")).thenReturn(true);
        when(pagoRepository.findAll()).thenReturn(List.of(pagoAValidado, pagoAPendiente, pagoBValidado));

        DashboardOperativoResponse dashboard = dashboardService.obtenerDashboardOperativo();

        assertEquals(1, dashboard.getPagosPendientes());
        assertEquals(2, dashboard.getPagosValidados());
        verify(pagoRepository).findAll();
    }

    private void autenticarOrganizadorA() {
        lenient().when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        lenient().when(usuarioAutenticadoService.tieneRol("PERSONAL_CONTROL")).thenReturn(false);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(organizadorA);
    }

    private Usuario usuario() {
        Usuario usuario = Usuario.builder()
                .nombres("Nombre")
                .apellidos("Apellido")
                .correoElectronico(UUID.randomUUID() + "@example.test")
                .build();
        usuario.setId(UUID.randomUUID());
        return usuario;
    }

    private Pago pago(Usuario organizador, EstadoPago estado, BigDecimal monto) {
        Evento evento = Evento.builder().titulo("Evento").organizador(organizador).build();
        evento.setId(UUID.randomUUID());
        Inscripcion inscripcion = Inscripcion.builder().evento(evento).usuario(usuario()).build();
        inscripcion.setId(UUID.randomUUID());
        Pago pago = Pago.builder().inscripcion(inscripcion).estado(estado).monto(monto).build();
        pago.setId(UUID.randomUUID());
        return pago;
    }
}
