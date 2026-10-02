package bo.uajms.eventos.modulos.pagos.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
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
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceFlowTest {

    @Mock PagoRepository pagoRepository;
    @Mock InscripcionRepository inscripcionRepository;
    @Mock PagoMapper pagoMapper;
    @Mock ArchivoSeguroServicio archivoSeguroServicio;
    @Mock AlmacenamientoArchivos almacenamientoArchivos;
    @Mock UsuarioAutenticadoService usuarioAutenticadoService;
    @Mock Clock clock;
    @InjectMocks PagoService service;

    private Usuario usuario;
    private Usuario organizador;
    private Inscripcion inscripcion;
    private Pago pago;
    private UUID pagoId;

    @BeforeEach
    void setUp() {
        usuario = usuario();
        organizador = usuario();
        Evento evento = Evento.builder().tipoInscripcion(TipoInscripcion.PAGO)
                .costo(new BigDecimal("125.50")).organizador(organizador).build();
        evento.setId(UUID.randomUUID());
        inscripcion = Inscripcion.builder().usuario(usuario).evento(evento)
                .estado(EstadoInscripcion.PENDIENTE_PAGO).build();
        inscripcion.setId(UUID.randomUUID());
        pago = Pago.builder().inscripcion(inscripcion).monto(evento.getCosto())
                .estado(EstadoPago.PENDIENTE_PAGO).intentosComprobante(0).build();
        pagoId = UUID.randomUUID();
        pago.setId(pagoId);
        lenient().when(clock.instant()).thenReturn(Instant.parse("2026-09-23T14:00:00Z"));
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("America/La_Paz"));
    }

    @Test
    void registroHistoricoTomaMontoOficialDelEventoYNoConfirmaInscripcion() {
        autenticarUsuario();
        RegistrarPagoRequest request = new RegistrarPagoRequest();
        request.setInscripcionId(inscripcion.getId());
        when(inscripcionRepository.findByIdAndUsuarioForUpdate(inscripcion.getId(), usuario.getId()))
                .thenReturn(Optional.of(inscripcion));
        when(pagoRepository.findByInscripcionId(inscripcion.getId())).thenReturn(Optional.empty());
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.registrarPago(request);

        verify(pagoRepository).save(argThat(creado -> creado.getMonto().compareTo(new BigDecimal("125.50")) == 0
                && creado.getEstado() == EstadoPago.PENDIENTE_PAGO));
        assertEquals(EstadoInscripcion.PENDIENTE_PAGO, inscripcion.getEstado());
    }

    @Test
    void eventoGratuitoNoPermiteCrearPago() {
        autenticarUsuario();
        inscripcion.getEvento().setTipoInscripcion(TipoInscripcion.GRATUITO);
        RegistrarPagoRequest request = new RegistrarPagoRequest();
        request.setInscripcionId(inscripcion.getId());
        when(inscripcionRepository.findByIdAndUsuarioForUpdate(inscripcion.getId(), usuario.getId()))
                .thenReturn(Optional.of(inscripcion));

        assertThrows(NegocioException.class, () -> service.registrarPago(request));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void cargaValidaMuevePagoEInscripcionAPendienteValidacion() {
        autenticarUsuario();
        MockMultipartFile archivo = archivo();
        when(pagoRepository.findByIdAndUsuarioForUpdate(pagoId, usuario.getId())).thenReturn(Optional.of(pago));
        when(archivoSeguroServicio.procesarComprobante(archivo))
                .thenReturn(new ArchivoSeguroServicio.ArchivoProcesado("pdf".getBytes(), "pdf", "application/pdf"));
        when(pagoRepository.save(pago)).thenReturn(pago);

        service.subirComprobante(pagoId, archivo);

        assertEquals(EstadoPago.PENDIENTE_VALIDACION, pago.getEstado());
        assertEquals(EstadoInscripcion.PENDIENTE_VALIDACION, inscripcion.getEstado());
        assertEquals(1, pago.getIntentosComprobante());
        assertTrue(ArchivoSeguroServicio.esClaveStorage(pago.getComprobanteUrl()));
        verify(almacenamientoArchivos).guardar(eq(pago.getComprobanteUrl()), any(byte[].class), eq("application/pdf"));
        verify(inscripcionRepository).save(inscripcion);
    }

    @Test
    void dobleEnvioPendienteValidacionNoEscribeArchivoNiEntidades() {
        autenticarUsuario();
        pago.setEstado(EstadoPago.PENDIENTE_VALIDACION);
        inscripcion.setEstado(EstadoInscripcion.PENDIENTE_VALIDACION);
        when(pagoRepository.findByIdAndUsuarioForUpdate(pagoId, usuario.getId())).thenReturn(Optional.of(pago));

        assertThrows(NegocioException.class, () -> service.subirComprobante(pagoId, archivo()));
        verify(archivoSeguroServicio, never()).procesarComprobante(any());
        verify(pagoRepository, never()).save(any());
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void inscripcionCanceladaNoPuedeContinuarFlujoPago() {
        autenticarUsuario();
        inscripcion.setEstado(EstadoInscripcion.CANCELADA);
        when(pagoRepository.findByIdAndUsuarioForUpdate(pagoId, usuario.getId())).thenReturn(Optional.of(pago));

        assertThrows(NegocioException.class, () -> service.subirComprobante(pagoId, archivo()));
        verify(archivoSeguroServicio, never()).procesarComprobante(any());
    }

    @Test
    void aprobacionExigeComprobante() {
        autenticarOrganizador();
        pago.setEstado(EstadoPago.PENDIENTE_VALIDACION);
        inscripcion.setEstado(EstadoInscripcion.PENDIENTE_VALIDACION);
        when(pagoRepository.findByIdAndOrganizadorForUpdate(pagoId, organizador.getId())).thenReturn(Optional.of(pago));

        assertThrows(NegocioException.class, () -> service.validarPago(pagoId, new ValidarPagoRequest()));
        verify(pagoRepository, never()).save(any());
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void dobleAprobacionEsRechazadaSinEscrituras() {
        autenticarOrganizador();
        pago.setEstado(EstadoPago.APROBADO);
        inscripcion.setEstado(EstadoInscripcion.CONFIRMADA);
        when(pagoRepository.findByIdAndOrganizadorForUpdate(pagoId, organizador.getId())).thenReturn(Optional.of(pago));

        assertThrows(NegocioException.class, () -> service.validarPago(pagoId, new ValidarPagoRequest()));
        verify(pagoRepository, never()).save(any());
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void rechazoSinMotivoNoConsultaNiEscribePago() {
        autenticarOrganizador();
        ValidarPagoRequest request = new ValidarPagoRequest();
        request.setObservacion("   ");

        assertThrows(NegocioException.class, () -> service.rechazarPago(pagoId, request));
        verifyNoInteractions(pagoRepository, inscripcionRepository);
    }

    @Test
    void usuarioNoPuedeAprobarNiRechazar() {
        autenticarUsuario();
        assertThrows(AccessDeniedException.class, () -> service.validarPago(pagoId, new ValidarPagoRequest()));
        ValidarPagoRequest rechazo = new ValidarPagoRequest();
        rechazo.setObservacion("No corresponde");
        assertThrows(AccessDeniedException.class, () -> service.rechazarPago(pagoId, rechazo));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void reenvioTrasRechazoConservaMismoPagoEIncrementaIntentos() {
        autenticarUsuario();
        pago.setEstado(EstadoPago.RECHAZADO);
        pago.setIntentosComprobante(1);
        pago.setMotivoRechazo("Comprobante ilegible");
        inscripcion.setEstado(EstadoInscripcion.PENDIENTE_PAGO);
        pago.setComprobanteUrl("comprobantes/" + pagoId + "/" + UUID.randomUUID() + ".pdf");
        String referenciaAnterior = pago.getComprobanteUrl();
        when(pagoRepository.findByIdAndUsuarioForUpdate(pagoId, usuario.getId())).thenReturn(Optional.of(pago));
        when(archivoSeguroServicio.procesarComprobante(any()))
                .thenReturn(new ArchivoSeguroServicio.ArchivoProcesado("pdf".getBytes(), "pdf", "application/pdf"));
        when(pagoRepository.save(pago)).thenReturn(pago);

        service.subirComprobante(pagoId, archivo());

        assertEquals(pagoId, pago.getId());
        assertEquals(2, pago.getIntentosComprobante());
        assertEquals(EstadoPago.PENDIENTE_VALIDACION, pago.getEstado());
        assertEquals("Comprobante ilegible", pago.getMotivoRechazo());
        verify(almacenamientoArchivos).eliminar(referenciaAnterior);
    }

    @Test
    void errorAlPersistirCompensaElObjetoNuevo() {
        autenticarUsuario();
        MockMultipartFile archivo = archivo();
        when(pagoRepository.findByIdAndUsuarioForUpdate(pagoId, usuario.getId())).thenReturn(Optional.of(pago));
        when(archivoSeguroServicio.procesarComprobante(archivo))
                .thenReturn(new ArchivoSeguroServicio.ArchivoProcesado("pdf".getBytes(), "pdf", "application/pdf"));
        when(pagoRepository.save(pago)).thenThrow(new IllegalStateException("db failure"));

        assertThrows(IllegalStateException.class, () -> service.subirComprobante(pagoId, archivo));

        verify(almacenamientoArchivos).eliminar(argThat(ArchivoSeguroServicio::esClaveStorage));
    }

    @Test
    void aprobacionPosteriorConservaUltimoMotivoDeRechazoComoHistorial() {
        autenticarOrganizador();
        pago.setEstado(EstadoPago.PENDIENTE_VALIDACION);
        pago.setComprobanteUrl("comprobantes/nuevo.pdf");
        pago.setFechaCargaComprobante(LocalDateTime.of(2026, 9, 23, 9, 30));
        pago.setMotivoRechazo("Comprobante anterior ilegible");
        inscripcion.setEstado(EstadoInscripcion.PENDIENTE_VALIDACION);
        when(pagoRepository.findByIdAndOrganizadorForUpdate(pagoId, organizador.getId()))
                .thenReturn(Optional.of(pago));
        when(pagoRepository.save(pago)).thenReturn(pago);

        service.validarPago(pagoId, new ValidarPagoRequest());

        assertEquals(EstadoPago.APROBADO, pago.getEstado());
        assertEquals(EstadoInscripcion.CONFIRMADA, inscripcion.getEstado());
        assertEquals("Comprobante anterior ilegible", pago.getMotivoRechazo());
    }

    private void autenticarUsuario() {
        lenient().when(usuarioAutenticadoService.tieneRol("USUARIO")).thenReturn(true);
        lenient().when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        lenient().when(usuarioAutenticadoService.tieneRol("ORGANIZADOR")).thenReturn(false);
        lenient().when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
    }

    private void autenticarOrganizador() {
        lenient().when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        lenient().when(usuarioAutenticadoService.tieneRol("ORGANIZADOR")).thenReturn(true);
        lenient().when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(organizador);
    }

    private Usuario usuario() {
        Usuario result = Usuario.builder().correoElectronico(UUID.randomUUID() + "@example.test").build();
        result.setId(UUID.randomUUID());
        return result;
    }

    private MockMultipartFile archivo() {
        return new MockMultipartFile("archivo", "comprobante.pdf", "application/pdf", "%PDF-1.4".getBytes());
    }
}
