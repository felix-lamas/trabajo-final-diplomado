package bo.uajms.eventos.modulos.pagos.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.dtos.PagoResponse;
import bo.uajms.eventos.modulos.pagos.dtos.RegistrarPagoRequest;
import bo.uajms.eventos.modulos.pagos.dtos.ValidarPagoRequest;
import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import bo.uajms.eventos.modulos.pagos.mappers.PagoMapper;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceAuthorizationTest {

    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private InscripcionRepository inscripcionRepository;
    @Mock
    private PagoMapper pagoMapper;
    @Mock
    private ArchivoSeguroServicio archivoSeguroServicio;
    @Mock
    private AlmacenamientoArchivos almacenamientoArchivos;
    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;
    @Mock
    private Clock clock;

    @InjectMocks
    private PagoService pagoService;

    private Usuario usuarioA;
    private Usuario usuarioB;
    private Usuario organizadorA;
    private Usuario organizadorB;
    private Inscripcion inscripcionA;
    private Inscripcion inscripcionB;
    private Pago pagoA;
    private Pago pagoB;
    private UUID pagoAId;
    private UUID pagoBId;

    @BeforeEach
    void configurarEscenario() {
        usuarioA = crearUsuario("usuario-a@example.test");
        usuarioB = crearUsuario("usuario-b@example.test");
        organizadorA = crearUsuario("organizador-a@example.test");
        organizadorB = crearUsuario("organizador-b@example.test");

        Evento eventoA = crearEvento(organizadorA);
        Evento eventoB = crearEvento(organizadorB);
        inscripcionA = crearInscripcion(usuarioA, eventoA);
        inscripcionB = crearInscripcion(usuarioB, eventoB);
        pagoA = crearPago(inscripcionA, EstadoPago.PENDIENTE_VALIDACION);
        pagoB = crearPago(inscripcionB, EstadoPago.PENDIENTE_VALIDACION);
        pagoAId = pagoA.getId();
        pagoBId = pagoB.getId();
        lenient().when(clock.instant()).thenReturn(Instant.parse("2026-09-23T14:00:00Z"));
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("America/La_Paz"));
    }

    @Test
    void usuarioPuedeConsultarSuPago() {
        autenticarParticipante(usuarioA);
        PagoResponse response = respuesta(pagoAId);
        when(pagoRepository.findByIdAndInscripcionUsuarioId(pagoAId, usuarioA.getId()))
                .thenReturn(Optional.of(pagoA));
        when(pagoMapper.toResponse(pagoA)).thenReturn(response);

        assertSame(response, pagoService.obtenerPorId(pagoAId));
        verify(pagoRepository, never()).findById(pagoAId);
    }

    @Test
    void usuarioNoPuedeConsultarPagoAjeno() {
        autenticarParticipante(usuarioA);
        when(pagoRepository.findByIdAndInscripcionUsuarioId(pagoBId, usuarioA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> pagoService.obtenerPorId(pagoBId));
        verify(pagoMapper, never()).toResponse(any());
        verify(pagoRepository, never()).findById(pagoBId);
    }

    @Test
    void pagoInexistenteYPagoFueraDeScopeTienenRespuestaUniforme() {
        autenticarParticipante(usuarioA);
        UUID inexistente = UUID.randomUUID();
        when(pagoRepository.findByIdAndInscripcionUsuarioId(pagoBId, usuarioA.getId()))
                .thenReturn(Optional.empty());
        when(pagoRepository.findByIdAndInscripcionUsuarioId(inexistente, usuarioA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> pagoService.obtenerPorId(pagoBId));
        assertThrows(RecursoNoEncontradoException.class, () -> pagoService.obtenerPorId(inexistente));
    }

    @Test
    void usuarioCargaComprobanteEnPagoPropioPendiente() {
        autenticarParticipante(usuarioA);
        pagoA.setEstado(EstadoPago.PENDIENTE_PAGO);
        inscripcionA.setEstado(EstadoInscripcion.PENDIENTE_PAGO);
        MockMultipartFile archivo = archivoValido();
        PagoResponse response = respuesta(pagoAId);
        when(pagoRepository.findByIdAndUsuarioForUpdate(pagoAId, usuarioA.getId()))
                .thenReturn(Optional.of(pagoA));
        when(archivoSeguroServicio.procesarComprobante(archivo))
                .thenReturn(new ArchivoSeguroServicio.ArchivoProcesado("pdf".getBytes(), "pdf", "application/pdf"));
        when(pagoRepository.save(pagoA)).thenReturn(pagoA);
        when(pagoMapper.toResponse(pagoA)).thenReturn(response);

        assertSame(response, pagoService.subirComprobante(pagoAId, archivo));
        assertTrue(ArchivoSeguroServicio.esClaveStorage(pagoA.getComprobanteUrl()));
        assertTrue(pagoA.getComprobanteNombreArchivo().startsWith("comprobante-"));
        assertEquals(EstadoPago.PENDIENTE_VALIDACION, pagoA.getEstado());
        verify(archivoSeguroServicio).procesarComprobante(archivo);
        verify(almacenamientoArchivos).guardar(eq(pagoA.getComprobanteUrl()), any(byte[].class), eq("application/pdf"));
    }

    @Test
    void usuarioNoCargaComprobanteEnPagoAjenoNiInvocaStorage() {
        autenticarParticipante(usuarioA);
        MockMultipartFile archivo = archivoValido();
        when(pagoRepository.findByIdAndUsuarioForUpdate(pagoBId, usuarioA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> pagoService.subirComprobante(pagoBId, archivo));

        verify(archivoSeguroServicio, never()).procesarComprobante(any());
        verifyNoInteractions(almacenamientoArchivos);
        verify(pagoRepository, never()).save(any());
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void usuarioNoReemplazaComprobanteValidado() {
        autenticarParticipante(usuarioA);
        pagoA.setEstado(EstadoPago.APROBADO);
        when(pagoRepository.findByIdAndUsuarioForUpdate(pagoAId, usuarioA.getId()))
                .thenReturn(Optional.of(pagoA));

        assertThrows(NegocioException.class,
                () -> pagoService.subirComprobante(pagoAId, archivoValido()));

        verify(archivoSeguroServicio, never()).procesarComprobante(any());
        verifyNoInteractions(almacenamientoArchivos);
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void usuarioPuedeRecargarComprobanteRechazadoYQuedaPendienteDeValidacion() {
        autenticarParticipante(usuarioA);
        pagoA.setEstado(EstadoPago.RECHAZADO);
        inscripcionA.setEstado(EstadoInscripcion.PENDIENTE_PAGO);
        MockMultipartFile archivo = archivoValido();
        when(pagoRepository.findByIdAndUsuarioForUpdate(pagoAId, usuarioA.getId()))
                .thenReturn(Optional.of(pagoA));
        when(archivoSeguroServicio.procesarComprobante(archivo))
                .thenReturn(new ArchivoSeguroServicio.ArchivoProcesado("pdf".getBytes(), "pdf", "application/pdf"));
        when(pagoRepository.save(pagoA)).thenReturn(pagoA);

        pagoService.subirComprobante(pagoAId, archivo);

        assertEquals(EstadoPago.PENDIENTE_VALIDACION, pagoA.getEstado());
        verify(pagoRepository).save(pagoA);
    }

    @Test
    void misPagosDevuelveUnicamentePagosPropios() {
        autenticarUsuario(usuarioA);
        PagoResponse response = respuesta(pagoAId);
        when(pagoRepository.findByUsuarioId(usuarioA.getId())).thenReturn(List.of(pagoA));
        when(pagoMapper.toResponse(pagoA)).thenReturn(response);

        assertEquals(List.of(response), pagoService.listarMisPagos());
        verify(pagoRepository).findByUsuarioId(usuarioA.getId());
    }

    @Test
    void usuarioNoRegistraPagoSobreInscripcionAjena() {
        autenticarUsuario(usuarioA);
        RegistrarPagoRequest request = new RegistrarPagoRequest();
        request.setInscripcionId(inscripcionB.getId());
        when(inscripcionRepository.findByIdAndUsuarioForUpdate(inscripcionB.getId(), usuarioA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> pagoService.registrarPago(request));

        verify(inscripcionRepository, never()).findById(inscripcionB.getId());
        verify(inscripcionRepository, never()).save(any());
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void organizadorListaSoloPendientesDeSusEventos() {
        autenticarOrganizador(organizadorA);
        PagoResponse response = respuesta(pagoAId);
        when(pagoRepository.findByEstadoAndInscripcionEventoOrganizadorId(
                EstadoPago.PENDIENTE_VALIDACION, organizadorA.getId())).thenReturn(List.of(pagoA));
        when(pagoMapper.toResponse(pagoA)).thenReturn(response);

        assertEquals(List.of(response), pagoService.listarPendientes());
        verify(pagoRepository, never()).findByEstado(EstadoPago.PENDIENTE_VALIDACION);
    }

    @Test
    void organizadorNoConsultaPagoDeEventoAjeno() {
        autenticarOrganizador(organizadorA);
        when(pagoRepository.findByIdAndInscripcionEventoOrganizadorId(pagoBId, organizadorA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> pagoService.obtenerPorId(pagoBId));
        verify(pagoRepository, never()).findById(pagoBId);
    }

    @Test
    void organizadorNoValidaPagoAjenoNiGeneraEfectosSecundarios() {
        autenticarOrganizador(organizadorA);
        EstadoPago estadoPago = pagoB.getEstado();
        EstadoInscripcion estadoInscripcion = inscripcionB.getEstado();
        when(pagoRepository.findByIdAndOrganizadorForUpdate(pagoBId, organizadorA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> pagoService.validarPago(pagoBId, new ValidarPagoRequest()));

        assertEquals(estadoPago, pagoB.getEstado());
        assertEquals(estadoInscripcion, inscripcionB.getEstado());
        verify(pagoRepository, never()).save(any());
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void organizadorNoRechazaPagoAjenoNiGeneraEfectosSecundarios() {
        autenticarOrganizador(organizadorA);
        EstadoPago estadoPago = pagoB.getEstado();
        EstadoInscripcion estadoInscripcion = inscripcionB.getEstado();
        when(pagoRepository.findByIdAndOrganizadorForUpdate(pagoBId, organizadorA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> pagoService.rechazarPago(pagoBId, rechazo("Motivo")));

        assertEquals(estadoPago, pagoB.getEstado());
        assertEquals(estadoInscripcion, inscripcionB.getEstado());
        verify(pagoRepository, never()).save(any());
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void administradorConsultaPagoGlobalmente() {
        autenticarAdministrador();
        PagoResponse response = respuesta(pagoBId);
        when(pagoRepository.findById(pagoBId)).thenReturn(Optional.of(pagoB));
        when(pagoMapper.toResponse(pagoB)).thenReturn(response);

        assertSame(response, pagoService.obtenerPorId(pagoBId));
    }

    @Test
    void administradorValidaPagoGlobalmente() {
        autenticarAdministrador();
        when(pagoRepository.findByIdForUpdate(pagoBId)).thenReturn(Optional.of(pagoB));
        when(pagoRepository.save(pagoB)).thenReturn(pagoB);

        pagoService.validarPago(pagoBId, new ValidarPagoRequest());

        assertEquals(EstadoPago.APROBADO, pagoB.getEstado());
        assertEquals(EstadoInscripcion.CONFIRMADA, inscripcionB.getEstado());
        verify(inscripcionRepository).save(inscripcionB);
    }

    @Test
    void administradorRechazaPagoGlobalmente() {
        autenticarAdministrador();
        when(pagoRepository.findByIdForUpdate(pagoBId)).thenReturn(Optional.of(pagoB));
        when(pagoRepository.save(pagoB)).thenReturn(pagoB);

        pagoService.rechazarPago(pagoBId, rechazo("Comprobante ilegible"));

        assertEquals(EstadoPago.RECHAZADO, pagoB.getEstado());
        assertEquals(EstadoInscripcion.PENDIENTE_PAGO, inscripcionB.getEstado());
        verify(inscripcionRepository).save(inscripcionB);
    }

    private void autenticarParticipante(Usuario usuario) {
        lenient().when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        lenient().when(usuarioAutenticadoService.tieneRol("ORGANIZADOR")).thenReturn(false);
        autenticarUsuario(usuario);
    }

    private void autenticarOrganizador(Usuario usuario) {
        lenient().when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        lenient().when(usuarioAutenticadoService.tieneRol("ORGANIZADOR")).thenReturn(true);
        autenticarUsuario(usuario);
    }

    private void autenticarAdministrador() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(true);
        lenient().when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(organizadorA);
    }

    private void autenticarUsuario(Usuario usuario) {
        lenient().when(usuarioAutenticadoService.tieneRol("USUARIO")).thenReturn(true);
        lenient().when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
    }

    private Usuario crearUsuario(String correo) {
        Usuario usuario = Usuario.builder()
                .correoElectronico(correo)
                .nombres("Nombre")
                .apellidos("Apellido")
                .build();
        usuario.setId(UUID.randomUUID());
        return usuario;
    }

    private Evento crearEvento(Usuario organizador) {
        Evento evento = Evento.builder()
                .titulo("Evento")
                .tipoInscripcion(TipoInscripcion.PAGO)
                .organizador(organizador)
                .build();
        evento.setId(UUID.randomUUID());
        return evento;
    }

    private Inscripcion crearInscripcion(Usuario usuario, Evento evento) {
        Inscripcion inscripcion = Inscripcion.builder()
                .usuario(usuario)
                .evento(evento)
                .estado(EstadoInscripcion.PENDIENTE_VALIDACION)
                .build();
        inscripcion.setId(UUID.randomUUID());
        return inscripcion;
    }

    private Pago crearPago(Inscripcion inscripcion, EstadoPago estado) {
        Pago pago = Pago.builder()
                .inscripcion(inscripcion)
                .monto(BigDecimal.TEN)
                .estado(estado)
                .build();
        if (estado == EstadoPago.PENDIENTE_VALIDACION) {
            pago.setComprobanteUrl("comprobantes/archivo.pdf");
            pago.setFechaCargaComprobante(LocalDateTime.of(2026, 9, 23, 9, 0));
        }
        pago.setId(UUID.randomUUID());
        return pago;
    }

    private PagoResponse respuesta(UUID id) {
        return PagoResponse.builder().id(id).build();
    }

    private MockMultipartFile archivoValido() {
        return new MockMultipartFile("archivo", "comprobante.pdf", "application/pdf", "pdf".getBytes());
    }

    private ValidarPagoRequest rechazo(String motivo) {
        ValidarPagoRequest request = new ValidarPagoRequest();
        request.setObservacion(motivo);
        return request;
    }
}
