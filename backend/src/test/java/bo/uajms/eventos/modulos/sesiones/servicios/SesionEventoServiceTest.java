package bo.uajms.eventos.modulos.sesiones.servicios;

import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.core.excepciones.*;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.eventos.entidades.*;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.sesiones.dtos.SesionEventoRequest;
import bo.uajms.eventos.modulos.sesiones.entidades.SesionEvento;
import bo.uajms.eventos.modulos.sesiones.repositorios.SesionEventoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SesionEventoServiceTest {
    @Mock SesionEventoRepository sesionRepository;
    @Mock EventoRepository eventoRepository;
    @Mock InscripcionRepository inscripcionRepository;
    @Mock UsuarioAutenticadoService auth;
    @Mock Clock clock;
    @InjectMocks SesionEventoService service;

    private final LocalDateTime ahora = LocalDateTime.of(2026, 9, 23, 10, 0);
    private UUID eventoId, sesionId, organizadorId;
    private Usuario organizador;
    private Evento evento;

    @BeforeEach void setUp() {
        eventoId = UUID.randomUUID(); sesionId = UUID.randomUUID(); organizadorId = UUID.randomUUID();
        organizador = Usuario.builder().correoElectronico("org@test.local").build(); organizador.setId(organizadorId);
        evento = Evento.builder().estado(EstadoEvento.PUBLICADO).modalidad(Modalidad.PRESENCIAL).organizador(organizador)
                .fechaInicio(ahora.toLocalDate()).fechaFin(ahora.toLocalDate().plusDays(2))
                .horaInicio(LocalTime.of(8, 0)).horaFin(LocalTime.of(18, 0)).build(); evento.setId(eventoId);
        lenient().when(clock.instant()).thenReturn(ahora.atZone(ZoneId.of("America/La_Paz")).toInstant());
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("America/La_Paz"));
    }

    @Test void organizadorPropietarioCreaSesion() {
        autenticarOrganizador(); when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)).thenReturn(Optional.of(evento));
        when(sesionRepository.save(any())).thenAnswer(i -> { SesionEvento s=i.getArgument(0); s.setId(sesionId); return s; });
        var response = service.crear(eventoId, requestValido());
        assertEquals(sesionId, response.getId()); assertSame(evento, capturarSesion().getEvento());
    }

    @Test void usuarioNoCreaSesion() {
        when(auth.tieneRol("ADMINISTRADOR")).thenReturn(false); when(auth.tieneRol("ORGANIZADOR")).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.crear(eventoId, requestValido()));
    }

    @Test void organizadorNoCreaSesionEnEventoAjeno() {
        autenticarOrganizador(); when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(eventoId, requestValido()));
        verify(sesionRepository, never()).save(any());
    }

    @Test void eventoCanceladoNoAdmiteSesion() { comprobarEstadoBloqueado(EstadoEvento.CANCELADO); }
    @Test void eventoFinalizadoNoAdmiteSesion() { comprobarEstadoBloqueado(EstadoEvento.FINALIZADO); }

    @Test void horarioValidoEsAceptado() {
        autenticarOrganizador(); when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)).thenReturn(Optional.of(evento));
        when(sesionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertDoesNotThrow(() -> service.crear(eventoId, requestValido()));
    }

    @Test void horaFinIgualAInicioEsRechazada() {
        var r=requestValido(); r.setHoraFin(r.getHoraInicio()); prepararCreacion();
        assertThrows(NegocioException.class, () -> service.crear(eventoId, r));
    }

    @Test void sesionQueCruzaMedianocheEsRechazada() {
        var r=requestValido(); r.setHoraInicio(LocalTime.of(22,0)); r.setHoraFin(LocalTime.of(1,0)); prepararCreacion();
        assertThrows(NegocioException.class, () -> service.crear(eventoId, r));
    }

    @Test void radioMayorA500EsRechazado() {
        var r=requestValido(); r.setRadioMetros(501); prepararCreacion();
        assertThrows(NegocioException.class, () -> service.crear(eventoId, r));
    }

    @Test void radio500EsAceptado() {
        var r=requestValido(); r.setRadioMetros(500); prepararCreacion(); when(sesionRepository.save(any())).thenAnswer(i->i.getArgument(0));
        assertDoesNotThrow(() -> service.crear(eventoId, r));
    }

    @Test void sesionNoPuedeComenzarAntesDelEvento() {
        var r=requestValido(); r.setFecha(evento.getFechaInicio()); r.setHoraInicio(LocalTime.of(7,59)); prepararCreacion();
        assertThrows(NegocioException.class, () -> service.crear(eventoId, r));
    }

    @Test void sesionNoPuedeTerminarDespuesDelEvento() {
        var r=requestValido(); r.setFecha(evento.getFechaFin()); r.setHoraFin(LocalTime.of(18,1)); prepararCreacion();
        assertThrows(NegocioException.class, () -> service.crear(eventoId, r));
    }

    @Test void coordenadasInvalidasSonRechazadasPorElServicio() {
        var r=requestValido(); r.setLatitud(new BigDecimal("90.01")); prepararCreacion();
        assertThrows(NegocioException.class, () -> service.crear(eventoId, r));
    }

    @Test void sesionIniciadaNoModificaHorario() {
        SesionEvento s=sesion(ahora.toLocalDate(), LocalTime.of(9,0), LocalTime.of(11,0)); prepararGestion(s);
        assertThrows(NegocioException.class, () -> service.actualizar(sesionId, requestValido())); verify(sesionRepository, never()).save(any());
    }

    @Test void sesionIniciadaNoModificaUbicacion() {
        SesionEvento s=sesion(ahora.toLocalDate(), LocalTime.of(9,0), LocalTime.of(11,0)); prepararGestion(s);
        var r=requestValido(); r.setLatitud(new BigDecimal("-21.6000000"));
        assertThrows(NegocioException.class, () -> service.actualizar(sesionId, r));
    }

    @Test void sesionIniciadaNoPuedeDesactivarse() {
        SesionEvento s=sesion(ahora.toLocalDate(), LocalTime.of(9,0), LocalTime.of(11,0)); prepararGestion(s);
        assertThrows(NegocioException.class, () -> service.cambiarEstado(sesionId, false)); assertTrue(s.getActiva());
    }

    @Test void eventoCanceladoNoPermiteActivarSesion() {
        SesionEvento s=sesion(ahora.toLocalDate().plusDays(1), LocalTime.of(9,0), LocalTime.of(11,0));
        evento.setEstado(EstadoEvento.CANCELADO); prepararGestion(s);
        assertThrows(NegocioException.class, () -> service.cambiarEstado(sesionId, true));
    }

    private void comprobarEstadoBloqueado(EstadoEvento estado) {
        autenticarOrganizador(); evento.setEstado(estado); when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)).thenReturn(Optional.of(evento));
        assertThrows(NegocioException.class, () -> service.crear(eventoId, requestValido()));
    }
    private void prepararCreacion() { autenticarOrganizador(); when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)).thenReturn(Optional.of(evento)); }
    private void prepararGestion(SesionEvento s) { autenticarOrganizador(); when(sesionRepository.findByIdAndEventoOrganizadorIdForUpdate(sesionId, organizadorId)).thenReturn(Optional.of(s)); }
    private void autenticarOrganizador() { lenient().when(auth.tieneRol("ADMINISTRADOR")).thenReturn(false); lenient().when(auth.tieneRol("ORGANIZADOR")).thenReturn(true); lenient().when(auth.obtenerUsuario()).thenReturn(organizador); }
    private SesionEventoRequest requestValido() { var r=new SesionEventoRequest(); r.setNombre("Sesion 1"); r.setFecha(ahora.toLocalDate().plusDays(1)); r.setHoraInicio(LocalTime.of(9,0)); r.setHoraFin(LocalTime.of(11,0)); r.setRequiereAsistencia(true); r.setLatitud(new BigDecimal("-21.5354900")); r.setLongitud(new BigDecimal("-64.7295600")); r.setRadioMetros(100); r.setActiva(true); return r; }
    private SesionEvento sesion(LocalDate f, LocalTime i, LocalTime fin) { var s=SesionEvento.builder().evento(evento).nombre("Sesion").fecha(f).horaInicio(i).horaFin(fin).requiereAsistencia(true).latitud(new BigDecimal("-21.53549")).longitud(new BigDecimal("-64.72956")).radioMetros(100).activa(true).historica(false).build(); s.setId(sesionId); return s; }
    private SesionEvento capturarSesion() { var c=ArgumentCaptor.forClass(SesionEvento.class); verify(sesionRepository).save(c.capture()); return c.getValue(); }
    @Test
    void cuentaDualConsultaSesionesComoParticipanteConfirmadoDeEventoAjeno() {
        autenticarOrganizador();
        when(auth.tieneRol("USUARIO")).thenReturn(true);
        var existente = sesion(ahora.toLocalDate(), LocalTime.of(9, 0), LocalTime.of(11, 0));
        var inscripcion = Inscripcion.builder()
                .usuario(organizador).evento(evento)
                .estado(EstadoInscripcion.CONFIRMADA).build();
        evento.setOrganizador(Usuario.builder().build());
        when(inscripcionRepository.findByUsuarioIdAndEventoIdAndEstado(organizadorId, eventoId,
                EstadoInscripcion.CONFIRMADA)).thenReturn(Optional.of(inscripcion));
        when(sesionRepository.findByEventoIdOrderByFechaAscHoraInicioAsc(eventoId)).thenReturn(List.of(existente));
        when(sesionRepository.findById(sesionId)).thenReturn(Optional.of(existente));
        assertEquals(1, service.listarPorEvento(eventoId).size());
        assertEquals(sesionId, service.obtener(sesionId).getId());
        verify(sesionRepository, never()).save(any());
    }

    @Test
    void cuentaDualSinInscripcionConfirmadaNoConsultaSesionesAjenas() {
        autenticarOrganizador();
        when(auth.tieneRol("USUARIO")).thenReturn(true);
        var existente = sesion(ahora.toLocalDate(), LocalTime.of(9, 0), LocalTime.of(11, 0));
        when(sesionRepository.findById(sesionId)).thenReturn(Optional.of(existente));
        assertThrows(RecursoNoEncontradoException.class, () -> service.listarPorEvento(eventoId));
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(sesionId));
        verify(sesionRepository, never()).findByEventoIdOrderByFechaAscHoraInicioAsc(any());
    }

    @Test
    void organizadorSinUsuarioNoConsultaSesionAjenaPorInscripcion() {
        autenticarOrganizador();
        assertThrows(RecursoNoEncontradoException.class, () -> service.listarPorEvento(eventoId));
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(sesionId));
        verify(inscripcionRepository, never()).findByUsuarioIdAndEventoIdAndEstado(any(), any(), any());
        verify(sesionRepository, never()).findById(any());
    }

}
