package bo.uajms.eventos.modulos.asistencias.servicios;

import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.entidades.Asistencia;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsistenciaServiceAuthorizationTest {

    @Mock private AsistenciaRepository asistenciaRepository;
    @Mock private EventoRepository eventoRepository;
    @Mock private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks private AsistenciaService service;

    private UUID organizadorId;
    private UUID eventoId;
    private Usuario organizador;
    private Evento evento;

    @BeforeEach
    void configurar() {
        organizadorId = UUID.randomUUID();
        eventoId = UUID.randomUUID();
        organizador = Usuario.builder().nombres("Organizador").build();
        ReflectionTestUtils.setField(organizador, "id", organizadorId);
        evento = Evento.builder().titulo("Evento").organizador(organizador).build();
        ReflectionTestUtils.setField(evento, "id", eventoId);

        lenient().when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        lenient().when(usuarioAutenticadoService.tieneRol("PERSONAL_CONTROL")).thenReturn(false);
        lenient().when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(organizador);
    }

    @Test
    void organizadorListaAsistenciasDeEventoPropio() {
        List<Asistencia> asistencias = List.of(Asistencia.builder().build());
        when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)).thenReturn(Optional.of(evento));
        when(asistenciaRepository.findByInscripcionEventoId(eventoId)).thenReturn(asistencias);

        assertSame(asistencias, service.obtenerAsistenciasPorEvento(eventoId));
    }

    @Test
    void organizadorNoListaAsistenciasDeEventoAjeno() {
        when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.obtenerAsistenciasPorEvento(eventoId));
        verify(asistenciaRepository, never()).findByInscripcionEventoId(any());
    }

    @Test
    void administradorListaAsistenciasGlobalmente() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(true);
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(asistenciaRepository.findByInscripcionEventoId(eventoId)).thenReturn(List.of());

        assertTrue(service.obtenerAsistenciasPorEvento(eventoId).isEmpty());
    }
}
