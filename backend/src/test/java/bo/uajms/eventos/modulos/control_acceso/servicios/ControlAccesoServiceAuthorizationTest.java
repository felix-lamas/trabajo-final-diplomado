package bo.uajms.eventos.modulos.control_acceso.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.servicios.AsistenciaService;
import bo.uajms.eventos.modulos.codigo_qr.entidades.CodigoQr;
import bo.uajms.eventos.modulos.codigo_qr.repositorios.CodigoQrRepository;
import bo.uajms.eventos.modulos.control_acceso.dtos.AutorizarIngresoRequest;
import bo.uajms.eventos.modulos.control_acceso.entidades.ControlAcceso;
import bo.uajms.eventos.modulos.control_acceso.repositorios.ControlAccesoRepository;
import bo.uajms.eventos.modulos.credenciales.entidades.Credencial;
import bo.uajms.eventos.modulos.credenciales.repositorios.CredencialRepository;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControlAccesoServiceAuthorizationTest {

    @Mock private ControlAccesoRepository controlAccesoRepository;
    @Mock private CredencialRepository credencialRepository;
    @Mock private CodigoQrRepository codigoQrRepository;
    @Mock private InscripcionRepository inscripcionRepository;
    @Mock private EventoRepository eventoRepository;
    @Mock private AsistenciaService asistenciaService;
    @Mock private PagoRepository pagoRepository;
    @Mock private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks private ControlAccesoService service;

    private UUID organizadorId;
    private UUID eventoId;
    private UUID credencialId;
    private Usuario organizador;
    private Usuario participante;
    private Evento evento;
    private Inscripcion inscripcion;
    private Credencial credencial;
    private CodigoQr qr;

    @BeforeEach
    void configurar() {
        organizadorId = UUID.randomUUID();
        eventoId = UUID.randomUUID();
        credencialId = UUID.randomUUID();

        organizador = Usuario.builder().nombres("Organizador").apellidos("A").build();
        asignarId(organizador, organizadorId);
        participante = Usuario.builder().nombres("Usuario").apellidos("A").ci("1234567").build();
        asignarId(participante, UUID.randomUUID());

        evento = Evento.builder()
                .titulo("Evento A")
                .organizador(organizador)
                .tipoInscripcion(TipoInscripcion.GRATUITO)
                .estado(EstadoEvento.EN_CURSO)
                .build();
        asignarId(evento, eventoId);

        inscripcion = Inscripcion.builder()
                .usuario(participante)
                .evento(evento)
                .codigoParticipante("PART-A")
                .estado(EstadoInscripcion.CONFIRMADA)
                .build();
        asignarId(inscripcion, UUID.randomUUID());

        credencial = Credencial.builder()
                .usuario(participante)
                .evento(evento)
                .inscripcion(inscripcion)
                .codigoParticipante("PART-A")
                .estado("ACTIVA")
                .build();
        asignarId(credencial, credencialId);

        qr = CodigoQr.builder()
                .credencial(credencial)
                .contenido(credencialId.toString())
                .estadoQr(CodigoQr.EstadoQr.GENERADO)
                .activo(true)
                .build();
        asignarId(qr, UUID.randomUUID());

        lenient().when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        lenient().when(usuarioAutenticadoService.tieneRol("PERSONAL_CONTROL")).thenReturn(false);
        lenient().when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(organizador);
    }

    @Test
    void organizadorValidaQrDeEventoPropio() {
        when(codigoQrRepository.findByContenidoAndCredencialInscripcionEventoOrganizadorId(qr.getContenido(), organizadorId))
                .thenReturn(Optional.of(qr));

        assertTrue(service.validarQr(qr.getContenido()).isPuedeIngresar());
    }

    @Test
    void organizadorNoValidaQrDeEventoAjeno() {
        when(codigoQrRepository.findByContenidoAndCredencialInscripcionEventoOrganizadorId(qr.getContenido(), organizadorId))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.validarQr(qr.getContenido()));
        verify(pagoRepository, never()).findByInscripcionId(any());
    }

    @Test
    void administradorValidaQrGlobalmente() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(true);
        when(codigoQrRepository.findByContenido(qr.getContenido())).thenReturn(Optional.of(qr));

        assertTrue(service.validarQr(qr.getContenido()).isPuedeIngresar());
    }

    @Test
    void organizadorAutorizaIngresoPropio() {
        prepararAutorizacionVisible();
        when(controlAccesoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals("AUTORIZADO", service.autorizarIngreso(request()).getEstadoIngreso());
        verify(controlAccesoRepository).save(any(ControlAcceso.class));
        verify(asistenciaService).registrarAsistencia(eq(inscripcion), eq(organizador), any());
        verify(codigoQrRepository).save(qr);
        assertEquals(CodigoQr.EstadoQr.UTILIZADO, qr.getEstadoQr());
    }

    @Test
    void organizadorNoAutorizaEventoAjenoNiEscribe() {
        when(credencialRepository.findByIdAndInscripcionEventoOrganizadorId(credencialId, organizadorId))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.autorizarIngreso(request()));
        verificarSinEscrituras();
        assertEquals(CodigoQr.EstadoQr.GENERADO, qr.getEstadoQr());
    }

    @Test
    void organizadorDeniegaIngresoPropio() {
        when(credencialRepository.findByIdAndInscripcionEventoOrganizadorId(credencialId, organizadorId))
                .thenReturn(Optional.of(credencial));
        when(controlAccesoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals("DENEGADO", service.denegarIngreso(request()).getEstadoIngreso());
        verify(asistenciaService, never()).registrarAsistencia(any(), any(), any());
        verify(codigoQrRepository, never()).save(any());
    }

    @Test
    void organizadorNoDeniegaEventoAjenoNiEscribe() {
        when(credencialRepository.findByIdAndInscripcionEventoOrganizadorId(credencialId, organizadorId))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.denegarIngreso(request()));
        verificarSinEscrituras();
    }

    @Test
    void organizadorConsultaHistorialPropio() {
        when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)).thenReturn(Optional.of(evento));
        when(controlAccesoRepository.findByCredencialInscripcionEventoId(eventoId)).thenReturn(List.of());

        assertTrue(service.obtenerHistorial(eventoId).isEmpty());
    }

    @Test
    void organizadorNoConsultaHistorialAjeno() {
        when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerHistorial(eventoId));
        verify(controlAccesoRepository, never()).findByCredencialInscripcionEventoId(any());
    }

    @Test
    void organizadorBuscaCodigoPropio() {
        when(inscripcionRepository.findByCodigoParticipanteAndEventoOrganizadorId("PART-A", organizadorId))
                .thenReturn(Optional.of(inscripcion));
        when(credencialRepository.findByInscripcionId(inscripcion.getId())).thenReturn(Optional.of(credencial));
        when(codigoQrRepository.findByCredencialId(credencialId)).thenReturn(Optional.of(qr));

        assertEquals(credencialId, service.buscarPorCodigoParticipante("PART-A").getCredencialId());
    }

    @Test
    void organizadorNoBuscaCodigoAjeno() {
        when(inscripcionRepository.findByCodigoParticipanteAndEventoOrganizadorId("PART-B", organizadorId))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.buscarPorCodigoParticipante("PART-B"));
        verify(credencialRepository, never()).findByInscripcionId(any());
    }

    @Test
    void organizadorBuscaDocumentoEnEventoPropio() {
        when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)).thenReturn(Optional.of(evento));
        when(inscripcionRepository.findByUsuarioCiAndEventoId("1234567", eventoId)).thenReturn(Optional.of(inscripcion));
        when(credencialRepository.findByInscripcionId(inscripcion.getId())).thenReturn(Optional.of(credencial));
        when(codigoQrRepository.findByCredencialId(credencialId)).thenReturn(Optional.of(qr));

        assertEquals(credencialId, service.buscarPorDocumentoIdentidad("1234567", eventoId).getCredencialId());
    }

    @Test
    void organizadorNoBuscaDocumentoEnEventoAjeno() {
        when(eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.buscarPorDocumentoIdentidad("1234567", eventoId));
        verify(inscripcionRepository, never()).findByUsuarioCiAndEventoId(any(), any());
    }

    @Test
    void inscripcionCanceladaNoRegistraAsistencia() {
        inscripcion.setEstado(EstadoInscripcion.CANCELADA);
        assertIngresoRechazadoSinAsistencia();
    }

    @Test
    void inscripcionRechazadaNoRegistraAsistencia() {
        inscripcion.setEstado(EstadoInscripcion.RECHAZADA);
        assertIngresoRechazadoSinAsistencia();
    }

    @Test
    void eventoPagadoConPagoPendienteNoRegistraAsistencia() {
        evento.setTipoInscripcion(TipoInscripcion.PAGO);
        when(pagoRepository.findByInscripcionId(inscripcion.getId()))
                .thenReturn(Optional.of(Pago.builder().inscripcion(inscripcion).estado(EstadoPago.PENDIENTE).build()));
        assertIngresoRechazadoSinAsistenciaConPagoConfigurado();
    }

    @Test
    void eventoPagadoConPagoRechazadoNoRegistraAsistencia() {
        evento.setTipoInscripcion(TipoInscripcion.PAGO);
        when(pagoRepository.findByInscripcionId(inscripcion.getId()))
                .thenReturn(Optional.of(Pago.builder().inscripcion(inscripcion).estado(EstadoPago.RECHAZADO).build()));
        assertIngresoRechazadoSinAsistenciaConPagoConfigurado();
    }

    @Test
    void eventoPagadoConPagoValidadoPermiteIngreso() {
        evento.setTipoInscripcion(TipoInscripcion.PAGO);
        when(pagoRepository.findByInscripcionId(inscripcion.getId()))
                .thenReturn(Optional.of(Pago.builder().inscripcion(inscripcion).estado(EstadoPago.VALIDADO).build()));
        prepararAutorizacionVisible(false);
        when(controlAccesoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> service.autorizarIngreso(request()));
        verify(asistenciaService).registrarAsistencia(eq(inscripcion), eq(organizador), any());
    }

    @Test
    void eventoGratuitoConfirmadoPermiteIngresoSinPago() {
        prepararAutorizacionVisible();
        when(controlAccesoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> service.autorizarIngreso(request()));
        verify(asistenciaService).registrarAsistencia(eq(inscripcion), eq(organizador), any());
    }

    @Test
    void qrAnuladoNoRegistraAsistenciaNiCambiaEstado() {
        qr.setEstadoQr(CodigoQr.EstadoQr.ANULADO);
        assertIngresoRechazadoSinAsistencia();
        assertEquals(CodigoQr.EstadoQr.ANULADO, qr.getEstadoQr());
    }

    @Test
    void qrUtilizadoNoRegistraAsistenciaNiCambiaEstado() {
        qr.setEstadoQr(CodigoQr.EstadoQr.UTILIZADO);
        assertIngresoRechazadoSinAsistencia();
        assertEquals(CodigoQr.EstadoQr.UTILIZADO, qr.getEstadoQr());
    }

    @Test
    void qrInactivoNoRegistraAsistencia() {
        qr.setActivo(false);
        assertIngresoRechazadoSinAsistencia();
    }

    @Test
    void credencialInactivaNoRegistraAsistencia() {
        credencial.setEstado("INACTIVA");
        assertIngresoRechazadoSinAsistencia();
    }

    @Test
    void eventoCanceladoNoRegistraAsistencia() {
        evento.setEstado(EstadoEvento.CANCELADO);
        assertIngresoRechazadoSinAsistencia();
    }

    private void prepararAutorizacionVisible() {
        prepararAutorizacionVisible(true);
    }

    private void prepararAutorizacionVisible(boolean configurarPagoSinRegistro) {
        when(credencialRepository.findByIdAndInscripcionEventoOrganizadorId(credencialId, organizadorId))
                .thenReturn(Optional.of(credencial));
        when(codigoQrRepository.findByCredencialId(credencialId)).thenReturn(Optional.of(qr));
        if (configurarPagoSinRegistro) {
            when(pagoRepository.findByInscripcionId(inscripcion.getId())).thenReturn(Optional.empty());
        }
    }

    private void assertIngresoRechazadoSinAsistencia() {
        prepararAutorizacionVisible();
        verificarIngresoRechazadoSinAsistencia();
    }

    private void assertIngresoRechazadoSinAsistenciaConPagoConfigurado() {
        when(credencialRepository.findByIdAndInscripcionEventoOrganizadorId(credencialId, organizadorId))
                .thenReturn(Optional.of(credencial));
        when(codigoQrRepository.findByCredencialId(credencialId)).thenReturn(Optional.of(qr));
        verificarIngresoRechazadoSinAsistencia();
    }

    private void verificarIngresoRechazadoSinAsistencia() {
        assertThrows(NegocioException.class, () -> service.autorizarIngreso(request()));
        verify(asistenciaService, never()).registrarAsistencia(any(), any(), any());
        verify(codigoQrRepository, never()).save(any());
    }

    private AutorizarIngresoRequest request() {
        return AutorizarIngresoRequest.builder().credencialId(credencialId).observacion("control").build();
    }

    private void verificarSinEscrituras() {
        verify(controlAccesoRepository, never()).save(any());
        verify(asistenciaService, never()).registrarAsistencia(any(), any(), any());
        verify(codigoQrRepository, never()).save(any());
    }

    private static void asignarId(Object entidad, UUID id) {
        ReflectionTestUtils.setField(entidad, "id", id);
    }
}
