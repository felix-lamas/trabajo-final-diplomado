package bo.uajms.eventos.modulos.reportes.servicios;

import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.categorias.entidades.CategoriaEvento;
import bo.uajms.eventos.modulos.certificados.repositorios.CertificadoRepository;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.reportes.dtos.DashboardAcademicoResponse;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceScopeTest {
    @Mock EventoRepository eventoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock InscripcionRepository inscripcionRepository;
    @Mock CertificadoRepository certificadoRepository;
    @Mock PagoRepository pagoRepository;
    @Mock AsistenciaRepository asistenciaRepository;
    @Mock UsuarioAutenticadoService auth;
    @InjectMocks DashboardService service;

    private Usuario organizador;

    @BeforeEach
    void setup() {
        organizador = Usuario.builder().nombres("Org").apellidos("A").build();
        organizador.setId(UUID.randomUUID());
        lenient().when(auth.tieneRol("ADMINISTRADOR")).thenReturn(false);
        lenient().when(auth.obtenerUsuario()).thenReturn(organizador);
        lenient().when(pagoRepository.findByInscripcionEventoOrganizadorId(organizador.getId())).thenReturn(List.of());
        lenient().when(certificadoRepository.countByEventoOrganizadorId(organizador.getId())).thenReturn(0L);
    }

    @Test
    void ejecutivoOrganizadorUsaSoloConteosPropios() {
        when(eventoRepository.countByOrganizadorId(organizador.getId())).thenReturn(2L);
        when(inscripcionRepository.countUsuariosDistintosByEventoOrganizadorId(organizador.getId())).thenReturn(3L);
        when(inscripcionRepository.countByEventoOrganizadorId(organizador.getId())).thenReturn(4L);

        DashboardEjecutivoResponse response = service.obtenerDashboardEjecutivo();

        assertEquals(2, response.getTotalEventos());
        assertEquals(3, response.getTotalUsuarios());
        assertEquals(4, response.getTotalInscripciones());
        verify(eventoRepository, never()).count();
        verify(inscripcionRepository, never()).count();
        verify(usuarioRepository, never()).count();
    }

    @Test
    void academicoOrganizadorAgrupaSoloEventosPropios() {
        when(eventoRepository.findByOrganizadorId(organizador.getId()))
                .thenReturn(List.of(evento("Propio", "Academico", EstadoEvento.PUBLICADO)));

        DashboardAcademicoResponse response = service.obtenerDashboardAcademico();

        assertEquals(1, response.getParticipacionPorCategoria().size());
        verify(eventoRepository, never()).findAll();
    }

    @Test
    void operativoOrganizadorCuentaSoloEstadosPropios() {
        when(eventoRepository.countByOrganizadorIdAndEstado(organizador.getId(), EstadoEvento.PUBLICADO)).thenReturn(2L);
        when(eventoRepository.countByOrganizadorIdAndEstado(organizador.getId(), EstadoEvento.FINALIZADO)).thenReturn(1L);

        DashboardOperativoResponse response = service.obtenerDashboardOperativo();

        assertEquals(2, response.getEventosActivos());
        assertEquals(1, response.getEventosFinalizados());
        verify(eventoRepository, never()).findAll();
    }

    @Test
    void reporteEventosOrganizadorNoConsultaGlobal() {
        when(eventoRepository.findByOrganizadorId(organizador.getId()))
                .thenReturn(List.of(evento("Propio", "Academico", EstadoEvento.PUBLICADO)));

        ReporteDataResponse response = service.generarReporte("eventos");

        assertEquals(1, response.getTotalRegistros());
        assertEquals("Propio", response.getFilas().getFirst().get("titulo"));
        verify(eventoRepository, never()).findAll();
    }

    @Test
    void reporteParticipantesOrganizadorNoConsultaInscripcionesGlobales() {
        Usuario participante = Usuario.builder().nombres("Participante").apellidos("Propio").ci("CI-TEST").build();
        Evento evento = evento("Propio", "Academico", EstadoEvento.PUBLICADO);
        Inscripcion inscripcion = Inscripcion.builder().usuario(participante).evento(evento).codigoParticipante("COD-1").build();
        when(inscripcionRepository.findByEventoOrganizadorId(organizador.getId())).thenReturn(List.of(inscripcion));

        ReporteDataResponse response = service.generarReporte("participantes");

        assertEquals(1, response.getTotalRegistros());
        assertEquals("CI-TEST", response.getFilas().getFirst().get("ci"));
        verify(inscripcionRepository, never()).findAll();
    }

    @Test
    void administradorConservaConsultaGlobalDeEventos() {
        when(auth.tieneRol("ADMINISTRADOR")).thenReturn(true);
        when(eventoRepository.findAll()).thenReturn(List.of(evento("Global", "Academico", EstadoEvento.PUBLICADO)));

        assertEquals(1, service.generarReporte("eventos").getTotalRegistros());
        verify(eventoRepository).findAll();
    }

    private Evento evento(String titulo, String categoria, EstadoEvento estado) {
        CategoriaEvento categoriaEvento = CategoriaEvento.builder().nombre(categoria).build();
        Evento evento = Evento.builder().titulo(titulo).categoria(categoriaEvento).estado(estado)
                .organizador(organizador).fechaInicio(LocalDate.of(2026, 9, 23)).cupoMaximo(100).build();
        evento.setId(UUID.randomUUID());
        return evento;
    }
}
