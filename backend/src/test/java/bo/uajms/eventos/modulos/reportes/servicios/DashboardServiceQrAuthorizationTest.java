package bo.uajms.eventos.modulos.reportes.servicios;

import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.certificados.repositorios.CertificadoRepository;
import bo.uajms.eventos.modulos.codigo_qr.entidades.CodigoQr;
import bo.uajms.eventos.modulos.codigo_qr.repositorios.CodigoQrRepository;
import bo.uajms.eventos.modulos.encuestas.repositorios.EncuestaRepository;
import bo.uajms.eventos.modulos.encuestas.repositorios.RespuestaEncuestaRepository;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.reportes.dtos.DashboardOperativoResponse;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceQrAuthorizationTest {

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

    @BeforeEach
    void configurarEscenario() {
        organizadorA = Usuario.builder().build();
        organizadorA.setId(UUID.randomUUID());
    }

    @Test
    void organizadorObtieneConteoQrSoloDeSusEventos() {
        autenticarOrganizador();
        when(codigoQrRepository.countByEstadoQrAndCredencialEventoOrganizadorId(
                CodigoQr.EstadoQr.UTILIZADO, organizadorA.getId())).thenReturn(2L);

        DashboardOperativoResponse dashboard = dashboardService.obtenerDashboardOperativo();

        assertEquals(2, dashboard.getQrUtilizados());
        verify(codigoQrRepository, never()).findAll();
    }

    @Test
    void administradorConservaConteoQrGlobal() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(true);
        when(codigoQrRepository.findAll()).thenReturn(List.of(qrUtilizado(), qrGenerado()));

        DashboardOperativoResponse dashboard = dashboardService.obtenerDashboardOperativo();

        assertEquals(1, dashboard.getQrUtilizados());
        verify(codigoQrRepository).findAll();
    }

    @Test
    void personalControlConservaConteoQrGlobalExistente() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(usuarioAutenticadoService.tieneRol("PERSONAL_CONTROL")).thenReturn(true);
        when(codigoQrRepository.findAll()).thenReturn(List.of(qrUtilizado(), qrUtilizado()));

        DashboardOperativoResponse dashboard = dashboardService.obtenerDashboardOperativo();

        assertEquals(2, dashboard.getQrUtilizados());
        verify(codigoQrRepository).findAll();
    }

    private void autenticarOrganizador() {
        lenient().when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        lenient().when(usuarioAutenticadoService.tieneRol("PERSONAL_CONTROL")).thenReturn(false);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(organizadorA);
    }

    private CodigoQr qrUtilizado() {
        return CodigoQr.builder().estadoQr(CodigoQr.EstadoQr.UTILIZADO).build();
    }

    private CodigoQr qrGenerado() {
        return CodigoQr.builder().estadoQr(CodigoQr.EstadoQr.GENERADO).build();
    }
}
