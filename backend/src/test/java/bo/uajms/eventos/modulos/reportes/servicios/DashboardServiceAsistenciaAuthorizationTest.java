package bo.uajms.eventos.modulos.reportes.servicios;

import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.certificados.repositorios.CertificadoRepository;
import bo.uajms.eventos.modulos.codigo_qr.repositorios.CodigoQrRepository;
import bo.uajms.eventos.modulos.encuestas.repositorios.EncuestaRepository;
import bo.uajms.eventos.modulos.encuestas.repositorios.RespuestaEncuestaRepository;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceAsistenciaAuthorizationTest {

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

    @InjectMocks private DashboardService service;

    private UUID organizadorId;
    private Usuario organizador;

    @BeforeEach
    void configurar() {
        organizadorId = UUID.randomUUID();
        organizador = Usuario.builder().nombres("Organizador").build();
        ReflectionTestUtils.setField(organizador, "id", organizadorId);
        lenient().when(eventoRepository.findAll()).thenReturn(List.of());
    }

    @Test
    void organizadorCuentaSoloAsistenciasDeSusEventos() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(usuarioAutenticadoService.tieneRol("PERSONAL_CONTROL")).thenReturn(false);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(organizador);
        when(pagoRepository.findByInscripcionEventoOrganizadorId(organizadorId)).thenReturn(List.of());
        when(codigoQrRepository.countByEstadoQrAndCredencialEventoOrganizadorId(any(), eq(organizadorId)))
                .thenReturn(2L);
        when(asistenciaRepository.countByInscripcionEventoOrganizadorId(organizadorId)).thenReturn(7L);

        assertEquals(7L, service.obtenerDashboardOperativo().getAsistenciasRegistradas());
        verify(asistenciaRepository, never()).count();
    }

    @Test
    void administradorConservaConteoGlobalDeAsistencias() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(true);
        when(pagoRepository.findAll()).thenReturn(List.of());
        when(codigoQrRepository.findAll()).thenReturn(List.of());
        when(asistenciaRepository.count()).thenReturn(11L);

        assertEquals(11L, service.obtenerDashboardOperativo().getAsistenciasRegistradas());
        verify(asistenciaRepository, never()).countByInscripcionEventoOrganizadorId(any());
    }

    @Test
    void personalControlConservaConteoGlobalPendienteDeDecision() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(usuarioAutenticadoService.tieneRol("PERSONAL_CONTROL")).thenReturn(true);
        when(pagoRepository.findAll()).thenReturn(List.of());
        when(codigoQrRepository.findAll()).thenReturn(List.of());
        when(asistenciaRepository.count()).thenReturn(13L);

        assertEquals(13L, service.obtenerDashboardOperativo().getAsistenciasRegistradas());
        verify(asistenciaRepository, never()).countByInscripcionEventoOrganizadorId(any());
    }
}
