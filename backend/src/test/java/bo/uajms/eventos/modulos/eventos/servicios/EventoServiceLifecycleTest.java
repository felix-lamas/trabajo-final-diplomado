package bo.uajms.eventos.modulos.eventos.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.categorias.entidades.CategoriaEvento;
import bo.uajms.eventos.modulos.categorias.repositorios.CategoriaEventoRepository;
import bo.uajms.eventos.modulos.eventos.dtos.ActualizarEventoRequest;
import bo.uajms.eventos.modulos.eventos.dtos.CrearEventoRequest;
import bo.uajms.eventos.modulos.eventos.entidades.*;
import bo.uajms.eventos.modulos.eventos.mappers.EventoMapper;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceLifecycleTest {

    @Mock EventoRepository eventoRepository;
    @Mock CategoriaEventoRepository categoriaRepository;
    @Mock EventoMapper eventoMapper;
    @Mock UsuarioAutenticadoService usuarioAutenticadoService;
    @InjectMocks EventoService service;

    private UUID eventoId;
    private UUID categoriaId;
    private Usuario organizador;
    private Usuario administrador;
    private CategoriaEvento categoria;

    @BeforeEach
    void setUp() {
        eventoId = UUID.randomUUID();
        categoriaId = UUID.randomUUID();
        organizador = usuario("organizador@test.local");
        administrador = usuario("admin@test.local");
        categoria = CategoriaEvento.builder().nombre("Academico").estado("ACTIVO").build();
        categoria.setId(categoriaId);
    }

    @Test
    void organizadorCreaEventoComoBorradorYEsPropietario() {
        autenticarOrganizador();
        when(categoriaRepository.findByIdAndEstado(categoriaId, "ACTIVO")).thenReturn(Optional.of(categoria));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(i -> i.getArgument(0));

        service.crear(solicitudValida(new CrearEventoRequest()));

        Evento guardado = capturarGuardado();
        assertEquals(EstadoEvento.BORRADOR, guardado.getEstado());
        assertSame(organizador, guardado.getOrganizador());
    }

    @Test
    void usuarioNoPuedeCrearEvento() {
        when(usuarioAutenticadoService.tieneRol("ORGANIZADOR")).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.crear(solicitudValida(new CrearEventoRequest())));
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void organizadorEditaEventoRechazadoPropio() {
        autenticarOrganizador();
        Evento evento = eventoCompleto(EstadoEvento.RECHAZADO);
        when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizador.getId())).thenReturn(Optional.of(evento));
        when(categoriaRepository.findByIdAndEstado(categoriaId, "ACTIVO")).thenReturn(Optional.of(categoria));
        when(eventoRepository.save(evento)).thenReturn(evento);

        ActualizarEventoRequest request = solicitudValida(new ActualizarEventoRequest());
        request.setTitulo("Titulo corregido");
        service.actualizar(eventoId, request);

        assertEquals("Titulo corregido", evento.getTitulo());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoEvento.class, names = {"EN_REVISION", "PUBLICADO", "FINALIZADO", "CANCELADO"})
    void noEditaEstadosBloqueados(EstadoEvento estado) {
        autenticarOrganizador();
        Evento evento = eventoCompleto(estado);
        when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizador.getId())).thenReturn(Optional.of(evento));
        assertThrows(NegocioException.class,
                () -> service.actualizar(eventoId, solicitudValida(new ActualizarEventoRequest())));
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void borradorCompletoPasaARevision() {
        autenticarOrganizador();
        Evento evento = eventoCompleto(EstadoEvento.BORRADOR);
        when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizador.getId())).thenReturn(Optional.of(evento));
        service.enviarARevision(eventoId);
        assertEquals(EstadoEvento.EN_REVISION, evento.getEstado());
        assertNotNull(evento.getFechaEnvioRevision());
    }

    @Test
    void borradorIncompletoNoPasaARevision() {
        autenticarOrganizador();
        Evento evento = eventoCompleto(EstadoEvento.BORRADOR);
        evento.setObjetivos(null);
        when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizador.getId())).thenReturn(Optional.of(evento));
        assertThrows(NegocioException.class, () -> service.enviarARevision(eventoId));
        assertEquals(EstadoEvento.BORRADOR, evento.getEstado());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void administradorPublicaEventoEnRevision() {
        autenticarAdministrador();
        Evento evento = eventoCompleto(EstadoEvento.EN_REVISION);
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        service.publicar(eventoId);
        assertEquals(EstadoEvento.PUBLICADO, evento.getEstado());
        assertSame(administrador, evento.getResueltoPor());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoEvento.class, names = {"BORRADOR", "RECHAZADO", "PUBLICADO", "FINALIZADO", "CANCELADO"})
    void administradorNoPublicaEstadoInvalido(EstadoEvento estado) {
        autenticarAdministradorSinUsuario();
        Evento evento = eventoCompleto(estado);
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        assertThrows(NegocioException.class, () -> service.publicar(eventoId));
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void organizadorNoPublica() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.publicar(eventoId));
        verify(eventoRepository, never()).findById(any());
    }

    @Test
    void administradorRechazaConMotivo() {
        autenticarAdministrador();
        Evento evento = eventoCompleto(EstadoEvento.EN_REVISION);
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        service.rechazar(eventoId, "Falta respaldo institucional");
        assertEquals(EstadoEvento.RECHAZADO, evento.getEstado());
        assertEquals("Falta respaldo institucional", evento.getMotivoRechazo());
    }

    @Test
    void rechazoSinMotivoNoEscribe() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(true);
        assertThrows(NegocioException.class, () -> service.rechazar(eventoId, " "));
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void rechazadoVuelveABorradorPorSuOrganizador() {
        autenticarOrganizador();
        Evento evento = eventoCompleto(EstadoEvento.RECHAZADO);
        when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizador.getId())).thenReturn(Optional.of(evento));
        service.volverABorrador(eventoId);
        assertEquals(EstadoEvento.BORRADOR, evento.getEstado());
    }

    @Test
    void publicadoSeCancelaConMotivo() {
        autenticarOrganizador();
        Evento evento = eventoCompleto(EstadoEvento.PUBLICADO);
        when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizador.getId())).thenReturn(Optional.of(evento));
        service.cancelar(eventoId, "Fuerza mayor");
        assertEquals(EstadoEvento.CANCELADO, evento.getEstado());
        assertEquals("Fuerza mayor", evento.getMotivoCancelacion());
    }

    @Test
    void canceladoNoPuedeReactivarseNiCancelarseOtraVez() {
        autenticarOrganizador();
        Evento evento = eventoCompleto(EstadoEvento.CANCELADO);
        when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizador.getId())).thenReturn(Optional.of(evento));
        assertThrows(NegocioException.class, () -> service.cancelar(eventoId, "Otra vez"));
        assertEquals(EstadoEvento.CANCELADO, evento.getEstado());
    }

    @Test
    void administradorFinalizaEventoYaTerminado() {
        autenticarAdministrador();
        Evento evento = eventoCompleto(EstadoEvento.PUBLICADO);
        evento.setFechaInicio(LocalDate.now().minusDays(2));
        evento.setFechaFin(LocalDate.now().minusDays(1));
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        service.finalizar(eventoId);
        assertEquals(EstadoEvento.FINALIZADO, evento.getEstado());
    }

    @Test
    void administradorNoFinalizaAntesDelFin() {
        autenticarAdministradorSinUsuario();
        Evento evento = eventoCompleto(EstadoEvento.PUBLICADO);
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        assertThrows(NegocioException.class, () -> service.finalizar(eventoId));
        assertEquals(EstadoEvento.PUBLICADO, evento.getEstado());
    }

    @Test
    void eventoVirtualRequiereEnlace() {
        autenticarOrganizador();
        CrearEventoRequest request = solicitudValida(new CrearEventoRequest());
        request.setModalidad(Modalidad.VIRTUAL);
        request.setUbicacion(null);
        request.setEnlaceVirtual(null);
        when(categoriaRepository.findByIdAndEstado(categoriaId, "ACTIVO")).thenReturn(Optional.of(categoria));
        assertThrows(NegocioException.class, () -> service.crear(request));
    }

    @Test
    void eventoPagadoRequiereMontoPositivo() {
        autenticarOrganizador();
        CrearEventoRequest request = solicitudValida(new CrearEventoRequest());
        request.setTipoInscripcion(TipoInscripcion.PAGO);
        request.setCosto(BigDecimal.ZERO);
        when(categoriaRepository.findByIdAndEstado(categoriaId, "ACTIVO")).thenReturn(Optional.of(categoria));
        assertThrows(NegocioException.class, () -> service.crear(request));
    }

    @Test
    void cupoLimitadoRequiereCapacidad() {
        autenticarOrganizador();
        CrearEventoRequest request = solicitudValida(new CrearEventoRequest());
        request.setCupoMaximo(null);
        when(categoriaRepository.findByIdAndEstado(categoriaId, "ACTIVO")).thenReturn(Optional.of(categoria));
        assertThrows(NegocioException.class, () -> service.crear(request));
    }

    @Test
    void certificadoCurricularRequiereHoras() {
        autenticarOrganizador();
        CrearEventoRequest request = solicitudValida(new CrearEventoRequest());
        request.setEmiteCertificado(true);
        request.setTipoCertificado(TipoCertificadoEvento.CURRICULAR);
        request.setHorasAcademicas(null);
        when(categoriaRepository.findByIdAndEstado(categoriaId, "ACTIVO")).thenReturn(Optional.of(categoria));
        assertThrows(NegocioException.class, () -> service.crear(request));
    }

    @Test
    void categoriaInactivaSeTrataComoNoDisponible() {
        autenticarOrganizador();
        when(categoriaRepository.findByIdAndEstado(categoriaId, "ACTIVO")).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.crear(solicitudValida(new CrearEventoRequest())));
        verify(eventoRepository, never()).save(any());
    }

    private <T extends bo.uajms.eventos.modulos.eventos.dtos.EventoDatosRequest> T solicitudValida(T request) {
        request.setTitulo("Jornadas universitarias");
        request.setDescripcion("Descripcion completa");
        request.setObjetivos("Objetivos completos");
        request.setCategoriaId(categoriaId);
        request.setModalidad(Modalidad.PRESENCIAL);
        request.setTipoInscripcion(TipoInscripcion.GRATUITO);
        request.setCosto(BigDecimal.ZERO);
        request.setFechaInicio(LocalDate.now().plusDays(2));
        request.setFechaFin(LocalDate.now().plusDays(2));
        request.setHoraInicio(LocalTime.of(8, 0));
        request.setHoraFin(LocalTime.of(12, 0));
        request.setUbicacion("Campus UAJMS");
        request.setRequiereInscripcion(true);
        request.setCupoLimitado(true);
        request.setCupoMaximo(100);
        request.setEmiteCertificado(false);
        request.setPublicoObjetivo(PublicoObjetivo.AMBOS);
        return request;
    }

    private Evento eventoCompleto(EstadoEvento estado) {
        Evento evento = Evento.builder()
                .titulo("Jornadas universitarias").descripcion("Descripcion completa")
                .objetivos("Objetivos completos").categoria(categoria).modalidad(Modalidad.PRESENCIAL)
                .tipoInscripcion(TipoInscripcion.GRATUITO).costo(BigDecimal.ZERO)
                .fechaInicio(LocalDate.now().plusDays(2)).fechaFin(LocalDate.now().plusDays(2))
                .horaInicio(LocalTime.of(8, 0)).horaFin(LocalTime.of(12, 0))
                .ubicacion("Campus UAJMS").requiereInscripcion(true).cupoLimitado(true)
                .cupoMaximo(100).cupoDisponible(100).emiteCertificado(false)
                .publicoObjetivo(PublicoObjetivo.AMBOS).estado(estado).organizador(organizador).build();
        evento.setId(eventoId);
        return evento;
    }

    private Usuario usuario(String correo) {
        Usuario usuario = Usuario.builder().correoElectronico(correo).nombres("Nombre").apellidos("Apellido").build();
        usuario.setId(UUID.randomUUID());
        return usuario;
    }

    private void autenticarOrganizador() {
        lenient().when(usuarioAutenticadoService.tieneRol("ORGANIZADOR")).thenReturn(true);
        lenient().when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        lenient().when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(organizador);
    }

    private void autenticarAdministrador() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(true);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(administrador);
    }

    private void autenticarAdministradorSinUsuario() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(true);
    }

    private Evento capturarGuardado() {
        var captor = org.mockito.ArgumentCaptor.forClass(Evento.class);
        verify(eventoRepository).save(captor.capture());
        return captor.getValue();
    }
}
