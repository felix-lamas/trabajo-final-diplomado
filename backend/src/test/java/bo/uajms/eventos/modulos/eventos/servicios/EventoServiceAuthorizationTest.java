package bo.uajms.eventos.modulos.eventos.servicios;

import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.categorias.entidades.CategoriaEvento;
import bo.uajms.eventos.modulos.categorias.repositorios.CategoriaEventoRepository;
import bo.uajms.eventos.modulos.eventos.dtos.ActualizarEventoRequest;
import bo.uajms.eventos.modulos.eventos.dtos.EventoDetalleResponse;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.Modalidad;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.entidades.PublicoObjetivo;
import bo.uajms.eventos.modulos.eventos.mappers.EventoMapper;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.pagos.servicios.AlmacenamientoArchivos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoServiceAuthorizationTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private CategoriaEventoRepository categoriaRepository;

    @Mock
    private EventoMapper eventoMapper;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private AlmacenamientoArchivos almacenamientoArchivos;

    @InjectMocks
    private EventoService eventoService;

    private UUID eventoId;
    private UUID organizadorAId;
    private UUID categoriaId;
    private Usuario organizadorA;
    private Evento eventoPropio;

    @BeforeEach
    void configurarEscenario() {
        eventoId = UUID.randomUUID();
        organizadorAId = UUID.randomUUID();
        categoriaId = UUID.randomUUID();

        organizadorA = Usuario.builder()
                .correoElectronico("organizador-a@example.test")
                .nombres("Organizador")
                .apellidos("A")
                .build();
        organizadorA.setId(organizadorAId);
        organizadorA.setEstadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.APROBADA);

        eventoPropio = crearEvento(EstadoEvento.BORRADOR, organizadorA);
    }

    @Test
    void publicoPuedeConsultarEventoPublicado() {
        Evento publicado = crearEvento(EstadoEvento.PUBLICADO, organizadorA);
        EventoDetalleResponse response = EventoDetalleResponse.builder().id(eventoId).build();
        when(eventoRepository.findByIdAndEstado(eventoId, EstadoEvento.PUBLICADO))
                .thenReturn(Optional.of(publicado));
        when(eventoMapper.toDetalleResponse(publicado)).thenReturn(response);

        EventoDetalleResponse resultado = eventoService.buscarPorId(eventoId);

        assertSame(response, resultado);
        verify(eventoRepository, never()).findById(eventoId);
    }

    @Test
    void publicoNoPuedeConsultarEventoBorrador() {
        comprobarEventoNoPublicadoInvisible(EstadoEvento.BORRADOR);
    }

    @Test
    void publicoNoPuedeConsultarEventoCancelado() {
        comprobarEventoNoPublicadoInvisible(EstadoEvento.CANCELADO);
    }

    @Test
    void publicoNoPuedeConsultarEventoFinalizado() {
        comprobarEventoNoPublicadoInvisible(EstadoEvento.FINALIZADO);
    }

    @Test
    void publicoNoPuedeConsultarEventoRechazado() {
        comprobarEventoNoPublicadoInvisible(EstadoEvento.RECHAZADO);
    }

    @Test
    void organizadorPuedeConsultarSuEventoNoPublicado() {
        autenticarOrganizadorA();
        EventoDetalleResponse response = EventoDetalleResponse.builder().id(eventoId).build();
        when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorAId)).thenReturn(Optional.of(eventoPropio));
        when(eventoMapper.toDetalleResponse(eventoPropio)).thenReturn(response);

        assertSame(response, eventoService.buscarPorId(eventoId));
    }

    @Test
    void organizadorPuedeModificarSuEventoBorrador() {
        autenticarOrganizadorA();
        ActualizarEventoRequest request = crearSolicitudActualizacion();
        CategoriaEvento categoria = new CategoriaEvento();
        when(eventoRepository.findByIdForUpdate(eventoId)).thenReturn(Optional.of(eventoPropio));
        categoria.setEstado("ACTIVO");
        when(categoriaRepository.findByIdAndEstado(categoriaId, "ACTIVO")).thenReturn(Optional.of(categoria));
        when(eventoRepository.save(eventoPropio)).thenReturn(eventoPropio);

        assertDoesNotThrow(() -> eventoService.actualizar(eventoId, request));
        assertEquals("Evento actualizado", eventoPropio.getTitulo());
        verify(eventoRepository).save(eventoPropio);
    }

    @Test
    void organizadorNoPuedeModificarEventoAjeno() {
        autenticarOrganizadorA();
        String tituloOriginal = eventoPropio.getTitulo();
        Usuario organizadorB = crearOrganizador("organizador-b@example.test");
        Evento ajeno = crearEvento(EstadoEvento.BORRADOR, organizadorB);
        when(eventoRepository.findByIdForUpdate(eventoId)).thenReturn(Optional.of(ajeno));

        assertThrows(
                AccessDeniedException.class,
                () -> eventoService.actualizar(eventoId, crearSolicitudActualizacion())
        );
        assertEquals(tituloOriginal, eventoPropio.getTitulo());
        verify(eventoRepository, never()).save(eventoPropio);
    }

    @Test
    void organizadorNoPuedeEliminarEventoAjeno() {
        autenticarOrganizadorA();
        Usuario organizadorB = crearOrganizador("organizador-b@example.test");
        Evento ajeno = crearEvento(EstadoEvento.BORRADOR, organizadorB);
        when(eventoRepository.findByIdForUpdate(eventoId)).thenReturn(Optional.of(ajeno));

        assertThrows(AccessDeniedException.class, () -> eventoService.eliminar(eventoId));

        verify(eventoRepository, never()).delete(eventoPropio);
    }

    @Test
    void organizadorNoPuedeCancelarEventoAjenoNiCambiarSuEstado() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> eventoService.cancelar(eventoId, "Motivo"));

        assertEquals(EstadoEvento.BORRADOR, eventoPropio.getEstado());
        verify(eventoRepository, never()).save(eventoPropio);
    }

    @Test
    void organizadorNoPuedeFinalizarEventoAjenoNiCambiarSuEstado() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> eventoService.finalizar(eventoId));

        assertEquals(EstadoEvento.BORRADOR, eventoPropio.getEstado());
        verify(eventoRepository, never()).save(eventoPropio);
    }

    @Test
    void organizadorNoPuedePublicarEventoAunqueSeaConocidoPorId() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> eventoService.publicar(eventoId));

        assertEquals(EstadoEvento.BORRADOR, eventoPropio.getEstado());
        verify(eventoRepository, never()).findById(eventoId);
        verify(eventoRepository, never()).save(eventoPropio);
    }

    @Test
    void administradorPuedeOperarEventoDeOtroOrganizador() {
        autenticarAdministrador();
        eventoPropio.setEstado(EstadoEvento.PUBLICADO);
        when(eventoRepository.findByIdForUpdate(eventoId)).thenReturn(Optional.of(eventoPropio));
        when(eventoRepository.save(eventoPropio)).thenReturn(eventoPropio);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(organizadorA);

        assertDoesNotThrow(() -> eventoService.cancelar(eventoId, "Motivo administrativo"));

        assertEquals(EstadoEvento.CANCELADO, eventoPropio.getEstado());
        verify(eventoRepository).save(eventoPropio);
    }

    @Test
    void administradorPuedePublicarEventoEnRevision() {
        autenticarAdministrador();
        eventoPropio.setEstado(EstadoEvento.EN_REVISION);
        completarEvento(eventoPropio);
        when(eventoRepository.findByIdForUpdate(eventoId)).thenReturn(Optional.of(eventoPropio));
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(organizadorA);

        eventoService.publicar(eventoId);

        assertEquals(EstadoEvento.PUBLICADO, eventoPropio.getEstado());
        verify(eventoRepository).save(eventoPropio);
    }

    @Test
    void participanteSoloListaEventosPublicados() {
        when(eventoRepository.findByEstado(EstadoEvento.PUBLICADO)).thenReturn(List.of());

        eventoService.listarTodos();

        verify(eventoRepository).findByEstado(EstadoEvento.PUBLICADO);
        verify(eventoRepository, never()).findAll();
    }

    @Test
    void organizadorListaSusEventosYLosPublicados() {
        autenticarOrganizadorA();
        when(eventoRepository.findByOrganizadorId(organizadorAId))
                .thenReturn(List.of());

        eventoService.listarTodos();

        verify(eventoRepository).findByOrganizadorId(organizadorAId);
    }

    @Test
    void administradorListaTodosLosEventos() {
        autenticarAdministrador();
        when(eventoRepository.findAll()).thenReturn(List.of());

        eventoService.listarTodos();

        verify(eventoRepository).findAll();
    }

    @Test
    void participanteSoloListaPublicadosDeLaCategoria() {
        when(eventoRepository.findByCategoriaIdAndEstado(categoriaId, EstadoEvento.PUBLICADO))
                .thenReturn(List.of());

        eventoService.listarPorCategoria(categoriaId);

        verify(eventoRepository).findByCategoriaIdAndEstado(categoriaId, EstadoEvento.PUBLICADO);
        verify(eventoRepository, never()).findByCategoriaId(categoriaId);
    }

    @Test
    void filtroPublicoPorCategoriaNoDependeDelRolAutenticado() {
        when(eventoRepository.findByCategoriaIdAndEstado(categoriaId, EstadoEvento.PUBLICADO)).thenReturn(List.of());

        eventoService.listarPorCategoria(categoriaId);

        verify(eventoRepository).findByCategoriaIdAndEstado(categoriaId, EstadoEvento.PUBLICADO);
        verify(eventoRepository, never()).findByCategoriaIdAndOrganizadorId(categoriaId, organizadorAId);
        verifyNoInteractions(usuarioAutenticadoService);
    }

    private void comprobarEventoNoPublicadoInvisible(EstadoEvento estado) {
        Evento noPublicado = crearEvento(estado, organizadorA);
        when(eventoRepository.findByIdAndEstado(eventoId, EstadoEvento.PUBLICADO))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> eventoService.buscarPorId(eventoId));

        assertEquals(estado, noPublicado.getEstado());
        verify(eventoRepository, never()).findById(eventoId);
    }

    private void autenticarOrganizadorA() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(usuarioAutenticadoService.tieneRol("ORGANIZADOR")).thenReturn(true);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(organizadorA);
    }

    private void autenticarAdministrador() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(true);
    }

    private Evento crearEvento(EstadoEvento estado, Usuario organizador) {
        Evento evento = Evento.builder()
                .titulo("Evento original")
                .estado(estado)
                .organizador(organizador)
                .cupoMaximo(10)
                .cupoDisponible(10)
                .build();
        evento.setId(eventoId);
        return evento;
    }

    private ActualizarEventoRequest crearSolicitudActualizacion() {
        ActualizarEventoRequest request = new ActualizarEventoRequest();
        request.setTitulo("Evento actualizado");
        request.setDescripcion("DescripciÃ³n");
        request.setObjetivos("Objetivos");
        request.setCategoriaId(categoriaId);
        request.setModalidad(Modalidad.PRESENCIAL);
        request.setTipoInscripcion(TipoInscripcion.GRATUITO);
        request.setCosto(BigDecimal.ZERO);
        request.setFechaInicio(LocalDate.now().plusDays(1));
        request.setFechaFin(LocalDate.now().plusDays(2));
        request.setHoraInicio(LocalTime.of(8, 0));
        request.setHoraFin(LocalTime.of(10, 0));
        request.setUbicacion("Campus universitario");
        request.setDireccion("Av. Las Americas");
        request.setLatitud(new BigDecimal("-21.5350000"));
        request.setLongitud(new BigDecimal("-64.7290000"));
        request.setRadioMetros(100);
        request.setRequiereInscripcion(true);
        request.setCupoLimitado(true);
        request.setCupoMaximo(20);
        request.setEmiteCertificado(false);
        request.setPublicoObjetivo(PublicoObjetivo.AMBOS);
        return request;
    }

    private void completarEvento(Evento evento) {
        CategoriaEvento categoria = CategoriaEvento.builder().nombre("Academico").estado("ACTIVO").build();
        evento.setCategoria(categoria);
        evento.setDescripcion("Descripcion completa");
        evento.setObjetivos("Objetivos completos");
        evento.setModalidad(Modalidad.PRESENCIAL);
        evento.setTipoInscripcion(TipoInscripcion.GRATUITO);
        evento.setCosto(BigDecimal.ZERO);
        evento.setFechaInicio(LocalDate.now().plusDays(1));
        evento.setFechaFin(LocalDate.now().plusDays(1));
        evento.setHoraInicio(LocalTime.of(8, 0));
        evento.setHoraFin(LocalTime.of(10, 0));
        evento.setUbicacion("Campus universitario");
        evento.setDireccion("Av. Las Americas");
        evento.setLatitud(new BigDecimal("-21.5350000"));
        evento.setLongitud(new BigDecimal("-64.7290000"));
        evento.setRadioMetros(100);
        evento.setRequiereInscripcion(false);
        evento.setCupoLimitado(false);
        evento.setEmiteCertificado(false);
        evento.setPublicoObjetivo(PublicoObjetivo.AMBOS);
    }

    private Usuario crearOrganizador(String correo) {
        Usuario usuario = Usuario.builder().correoElectronico(correo).nombres("Organizador").apellidos("B").build();
        usuario.setId(UUID.randomUUID());
        usuario.setEstadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.APROBADA);
        return usuario;
    }
}
