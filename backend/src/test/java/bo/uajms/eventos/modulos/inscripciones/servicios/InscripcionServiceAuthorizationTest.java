package bo.uajms.eventos.modulos.inscripciones.servicios;

import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.dtos.CrearInscripcionRequest;
import bo.uajms.eventos.modulos.inscripciones.dtos.ComprobanteInscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.dtos.DetalleInscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.dtos.InscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.mappers.InscripcionMapper;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class InscripcionServiceAuthorizationTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private InscripcionMapper inscripcionMapper;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock private PagoRepository pagoRepository;
    @Mock private Clock clock;

    @InjectMocks
    private InscripcionService inscripcionService;

    private UUID inscripcionAId;
    private UUID inscripcionBId;
    private UUID eventoAId;
    private UUID eventoBId;
    private Usuario usuarioA;
    private Usuario usuarioB;
    private Usuario organizadorA;
    private Usuario organizadorB;
    private Evento eventoA;
    private Evento eventoB;
    private Inscripcion inscripcionA;
    private Inscripcion inscripcionB;

    @BeforeEach
    void configurarEscenario() {
        inscripcionAId = UUID.randomUUID();
        inscripcionBId = UUID.randomUUID();
        eventoAId = UUID.randomUUID();
        eventoBId = UUID.randomUUID();

        usuarioA = crearUsuario(UUID.randomUUID(), "usuario-a@example.test");
        usuarioB = crearUsuario(UUID.randomUUID(), "usuario-b@example.test");
        organizadorA = crearUsuario(UUID.randomUUID(), "organizador-a@example.test");
        organizadorB = crearUsuario(UUID.randomUUID(), "organizador-b@example.test");

        eventoA = crearEvento(eventoAId, organizadorA);
        eventoB = crearEvento(eventoBId, organizadorB);
        inscripcionA = crearInscripcion(inscripcionAId, usuarioA, eventoA);
        inscripcionB = crearInscripcion(inscripcionBId, usuarioB, eventoB);
        org.mockito.Mockito.lenient().when(clock.instant()).thenReturn(Instant.parse("2026-09-23T14:00:00Z"));
        org.mockito.Mockito.lenient().when(clock.getZone()).thenReturn(ZoneId.of("America/La_Paz"));
    }

    @Test
    void usuarioPuedeConsultarSuInscripcion() {
        autenticarParticipante(usuarioA);
        DetalleInscripcionResponse response = DetalleInscripcionResponse.builder().id(inscripcionAId).build();
        when(inscripcionRepository.findByIdAndUsuarioId(inscripcionAId, usuarioA.getId()))
                .thenReturn(Optional.of(inscripcionA));
        when(inscripcionMapper.toDetalleResponse(inscripcionA)).thenReturn(response);

        assertSame(response, inscripcionService.obtenerPorId(inscripcionAId));
        verify(inscripcionRepository, never()).findById(inscripcionAId);
    }

    @Test
    void usuarioNoPuedeConsultarInscripcionAjena() {
        autenticarParticipante(usuarioA);
        when(inscripcionRepository.findByIdAndUsuarioId(inscripcionBId, usuarioA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> inscripcionService.obtenerPorId(inscripcionBId));

        verify(inscripcionMapper, never()).toDetalleResponse(any());
    }

    @Test
    void listarMisInscripcionesDevuelveSoloLasPropias() {
        autenticarUsuario(usuarioA);
        InscripcionResponse response = InscripcionResponse.builder().id(inscripcionAId).build();
        when(inscripcionRepository.findByUsuarioId(usuarioA.getId())).thenReturn(List.of(inscripcionA));
        when(inscripcionMapper.toResponse(inscripcionA)).thenReturn(response);

        List<InscripcionResponse> resultado = inscripcionService.listarMisInscripciones();

        assertEquals(List.of(response), resultado);
        verify(inscripcionRepository).findByUsuarioId(usuarioA.getId());
    }

    @Test
    void inscripcionNuevaPerteneceAlUsuarioAutenticado() {
        autenticarUsuario(usuarioA);
        CrearInscripcionRequest request = new CrearInscripcionRequest(eventoAId);
        DetalleInscripcionResponse response = DetalleInscripcionResponse.builder().build();
        when(eventoRepository.findByIdForUpdate(eventoAId)).thenReturn(Optional.of(eventoA));
        when(inscripcionRepository.existsByUsuarioIdAndEventoId(usuarioA.getId(), eventoAId)).thenReturn(false);
        when(inscripcionRepository.save(any(Inscripcion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(inscripcionMapper.toDetalleResponse(any(Inscripcion.class))).thenReturn(response);

        assertSame(response, inscripcionService.inscribir(request));

        ArgumentCaptor<Inscripcion> captor = ArgumentCaptor.forClass(Inscripcion.class);
        verify(inscripcionRepository).save(captor.capture());
        assertSame(usuarioA, captor.getValue().getUsuario());
    }

    @Test
    void usuarioPuedeCancelarSuInscripcion() {
        autenticarUsuario(usuarioA);
        when(inscripcionRepository.findEventoIdByIdAndUsuarioId(inscripcionAId, usuarioA.getId()))
                .thenReturn(Optional.of(eventoAId));
        when(eventoRepository.findByIdForUpdate(eventoAId)).thenReturn(Optional.of(eventoA));
        when(inscripcionRepository.findByIdAndUsuarioForUpdate(inscripcionAId, usuarioA.getId()))
                .thenReturn(Optional.of(inscripcionA));

        assertDoesNotThrow(() -> inscripcionService.cancelar(inscripcionAId));

        assertEquals(EstadoInscripcion.CANCELADA, inscripcionA.getEstado());
        assertEquals(6, eventoA.getCupoDisponible());
        verify(eventoRepository).save(eventoA);
        verify(inscripcionRepository).save(inscripcionA);
        InOrder bloqueos = inOrder(pagoRepository, eventoRepository, inscripcionRepository);
        bloqueos.verify(pagoRepository).findByInscripcionIdAndUsuarioIdForUpdate(inscripcionAId, usuarioA.getId());
        bloqueos.verify(eventoRepository).findByIdForUpdate(eventoAId);
        bloqueos.verify(inscripcionRepository).findByIdAndUsuarioForUpdate(inscripcionAId, usuarioA.getId());
    }

    @Test
    void usuarioNoPuedeCancelarInscripcionAjenaNiGenerarEfectosSecundarios() {
        autenticarUsuario(usuarioA);
        EstadoInscripcion estadoOriginal = inscripcionB.getEstado();
        int cupoOriginal = eventoB.getCupoDisponible();
        when(inscripcionRepository.findEventoIdByIdAndUsuarioId(inscripcionBId, usuarioA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> inscripcionService.cancelar(inscripcionBId));

        assertEquals(estadoOriginal, inscripcionB.getEstado());
        assertEquals(cupoOriginal, eventoB.getCupoDisponible());
        verify(eventoRepository, never()).save(any());
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void organizadorPuedeConsultarInscripcionDeSuEvento() {
        autenticarOrganizador(organizadorA);
        DetalleInscripcionResponse response = DetalleInscripcionResponse.builder().id(inscripcionAId).build();
        when(inscripcionRepository.findByIdAndEventoOrganizadorId(inscripcionAId, organizadorA.getId()))
                .thenReturn(Optional.of(inscripcionA));
        when(inscripcionMapper.toDetalleResponse(inscripcionA)).thenReturn(response);

        assertSame(response, inscripcionService.obtenerPorId(inscripcionAId));
    }

    @Test
    void organizadorNoPuedeConsultarInscripcionDeEventoAjeno() {
        autenticarOrganizador(organizadorA);
        when(inscripcionRepository.findByIdAndEventoOrganizadorId(inscripcionBId, organizadorA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> inscripcionService.obtenerPorId(inscripcionBId));

        verify(inscripcionMapper, never()).toDetalleResponse(any());
    }

    @Test
    void organizadorPuedeListarInscritosDeSuEvento() {
        autenticarOrganizador(organizadorA);
        when(eventoRepository.findByIdAndOrganizadorId(eventoAId, organizadorA.getId()))
                .thenReturn(Optional.of(eventoA));
        when(inscripcionRepository.findByEventoId(eventoAId)).thenReturn(List.of(inscripcionA));

        assertDoesNotThrow(() -> inscripcionService.listarInscritosEvento(eventoAId));

        verify(inscripcionRepository).findByEventoId(eventoAId);
    }

    @Test
    void organizadorNoPuedeListarInscritosDeEventoAjeno() {
        autenticarOrganizador(organizadorA);
        when(eventoRepository.findByIdAndOrganizadorId(eventoBId, organizadorA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> inscripcionService.listarInscritosEvento(eventoBId));

        verify(inscripcionRepository, never()).findByEventoId(eventoBId);
        verify(inscripcionMapper, never()).toResponse(any());
    }

    @Test
    void administradorPuedeConsultarCualquierInscripcion() {
        autenticarAdministrador();
        DetalleInscripcionResponse response = DetalleInscripcionResponse.builder().id(inscripcionBId).build();
        when(inscripcionRepository.findById(inscripcionBId)).thenReturn(Optional.of(inscripcionB));
        when(inscripcionMapper.toDetalleResponse(inscripcionB)).thenReturn(response);

        assertSame(response, inscripcionService.obtenerPorId(inscripcionBId));
    }

    @Test
    void administradorPuedeListarInscritosDeCualquierEvento() {
        autenticarAdministrador();
        when(inscripcionRepository.findByEventoId(eventoBId)).thenReturn(List.of(inscripcionB));

        assertDoesNotThrow(() -> inscripcionService.listarInscritosEvento(eventoBId));

        verify(eventoRepository, never()).findByIdAndOrganizadorId(any(), any());
        verify(inscripcionRepository).findByEventoId(eventoBId);
    }

    @Test
    void administradorNoPuedeUsarFlujoPersonalDeInscripcion() {
        autenticarAdministrador();

        assertThrows(AccessDeniedException.class,
                () -> inscripcionService.inscribir(new CrearInscripcionRequest(eventoAId)));
        assertThrows(AccessDeniedException.class, () -> inscripcionService.listarMisInscripciones());
        assertThrows(AccessDeniedException.class, () -> inscripcionService.cancelar(inscripcionAId));
    }

    @Test
    void comprobantePropioUsaMontoYEstadoOficialDelPago() {
        autenticarUsuario(usuarioA);
        usuarioA.setNombres("Ana");
        usuarioA.setApellidos("Mendez");
        usuarioA.setCi("1234567");
        usuarioA.setRu("20260001");
        eventoA.setTipoInscripcion(TipoInscripcion.PAGO);
        eventoA.setCosto(new BigDecimal("80.00"));
        inscripcionA.setCodigoParticipante("INS-ABC123");
        Pago pago = Pago.builder().inscripcion(inscripcionA).monto(new BigDecimal("80.00"))
                .estado(EstadoPago.PENDIENTE_VALIDACION).build();
        when(inscripcionRepository.findByIdAndUsuarioId(inscripcionAId, usuarioA.getId()))
                .thenReturn(Optional.of(inscripcionA));
        when(pagoRepository.findByInscripcionId(inscripcionAId)).thenReturn(Optional.of(pago));

        ComprobanteInscripcionResponse comprobante =
                inscripcionService.obtenerComprobantePropio(inscripcionAId);

        assertEquals("Ana Mendez", comprobante.getParticipante());
        assertEquals("1234567", comprobante.getCi());
        assertEquals("20260001", comprobante.getRu());
        assertEquals("INS-ABC123", comprobante.getCodigoVerificacion());
        assertEquals("PENDIENTE_VALIDACION", comprobante.getEstadoPago());
        assertEquals(0, new BigDecimal("80.00").compareTo(comprobante.getMonto()));
    }

    @Test
    void comprobanteAjenoNoSeExpone() {
        autenticarUsuario(usuarioA);
        when(inscripcionRepository.findByIdAndUsuarioId(inscripcionBId, usuarioA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> inscripcionService.obtenerComprobantePropio(inscripcionBId));
        verify(pagoRepository, never()).findByInscripcionId(any());
    }

    private void autenticarParticipante(Usuario usuario) {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(usuarioAutenticadoService.tieneRol("ORGANIZADOR")).thenReturn(false);
        autenticarUsuario(usuario);
    }

    private void autenticarOrganizador(Usuario usuario) {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(usuarioAutenticadoService.tieneRol("ORGANIZADOR")).thenReturn(true);
        autenticarUsuario(usuario);
    }

    private void autenticarAdministrador() {
        org.mockito.Mockito.lenient().when(usuarioAutenticadoService.tieneRol("USUARIO")).thenReturn(false);
        org.mockito.Mockito.lenient().when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(true);
    }

    private void autenticarUsuario(Usuario usuario) {
        org.mockito.Mockito.lenient().when(usuarioAutenticadoService.tieneRol("USUARIO")).thenReturn(true);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
    }

    private Usuario crearUsuario(UUID id, String correo) {
        Usuario usuario = Usuario.builder()
                .correoElectronico(correo)
                .nombres("Nombre")
                .apellidos("Apellido")
                .build();
        usuario.setId(id);
        return usuario;
    }

    private Evento crearEvento(UUID id, Usuario organizador) {
        Evento evento = Evento.builder()
                .titulo("Evento")
                .estado(EstadoEvento.PUBLICADO)
                .tipoInscripcion(TipoInscripcion.GRATUITO)
                .organizador(organizador)
                .cupoMaximo(10)
                .cupoDisponible(5)
                .build();
        evento.setId(id);
        return evento;
    }

    private Inscripcion crearInscripcion(UUID id, Usuario usuario, Evento evento) {
        Inscripcion inscripcion = Inscripcion.builder()
                .usuario(usuario)
                .evento(evento)
                .estado(EstadoInscripcion.CONFIRMADA)
                .build();
        inscripcion.setId(id);
        return inscripcion;
    }
}
