package bo.uajms.eventos.modulos.certificados.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.certificados.dtos.VerificacionCertificadoResponse;
import bo.uajms.eventos.modulos.certificados.entidades.Certificado;
import bo.uajms.eventos.modulos.certificados.repositorios.CertificadoRepository;
import bo.uajms.eventos.modulos.eventos.entidades.*;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.sesiones.repositorios.SesionEventoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import jakarta.persistence.JoinColumn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificadoServiceTest {

    @Mock CertificadoRepository certificadoRepository;
    @Mock InscripcionRepository inscripcionRepository;
    @Mock EventoRepository eventoRepository;
    @Mock SesionEventoRepository sesionEventoRepository;
    @Mock AsistenciaRepository asistenciaRepository;
    @Mock PagoRepository pagoRepository;
    @Mock UsuarioAutenticadoService auth;
    @Mock CertificadoDocumentoService documentoService;
    @Mock Clock clock;
    @InjectMocks CertificadoService service;

    private Usuario participante;
    private Usuario organizador;
    private Evento evento;
    private Inscripcion inscripcion;
    private UUID inscripcionId;

    @BeforeEach
    void setUp() {
        participante = usuario("Participante");
        organizador = usuario("Organizador");
        evento = Evento.builder().titulo("Congreso UAJMS").estado(EstadoEvento.FINALIZADO)
                .emiteCertificado(true).tipoCertificado(TipoCertificadoEvento.NO_CURRICULAR)
                .tipoInscripcion(TipoInscripcion.GRATUITO).costo(BigDecimal.ZERO)
                .organizador(organizador).build();
        evento.setId(UUID.randomUUID());
        inscripcion = Inscripcion.builder().usuario(participante).evento(evento)
                .estado(EstadoInscripcion.CONFIRMADA).build();
        inscripcionId = UUID.randomUUID();
        inscripcion.setId(inscripcionId);
        lenient().when(clock.instant()).thenReturn(Instant.parse("2026-09-23T16:00:00Z"));
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("America/La_Paz"));
        lenient().when(certificadoRepository.findByInscripcionId(inscripcionId)).thenReturn(Optional.empty());
        lenient().when(certificadoRepository.existsByCodigoCertificado(anyString())).thenReturn(false);
        lenient().when(sesionEventoRepository.countByEventoIdAndRequiereAsistenciaTrue(evento.getId())).thenReturn(0L);
        lenient().when(asistenciaRepository.countSesionesRequeridasAsistidas(inscripcionId, evento.getId())).thenReturn(0L);
        lenient().when(certificadoRepository.save(any(Certificado.class))).thenAnswer(invocation -> {
            Certificado certificado = invocation.getArgument(0);
            if (certificado.getId() == null) certificado.setId(UUID.randomUUID());
            return certificado;
        });
        autenticarOrganizador();
        lenient().when(inscripcionRepository.findByIdAndEventoOrganizadorForUpdate(inscripcionId, organizador.getId()))
                .thenReturn(Optional.of(inscripcion));
        lenient().when(eventoRepository.findByIdAndOrganizadorId(evento.getId(), organizador.getId()))
                .thenReturn(Optional.of(evento));
    }

    @Test
    void certificadoNoCurricularValidoExigeTodasLasSesionesRequeridas() {
        when(sesionEventoRepository.countByEventoIdAndRequiereAsistenciaTrue(evento.getId())).thenReturn(3L);
        when(asistenciaRepository.countSesionesRequeridasAsistidas(inscripcionId, evento.getId())).thenReturn(3L);

        var respuesta = service.generarCertificado(inscripcionId);

        assertEquals("NO_CURRICULAR", respuesta.getTipoCertificado());
        assertEquals(new BigDecimal("100.00"), respuesta.getPorcentajeAsistencia());
        assertTrue(respuesta.getUrlVerificacion().startsWith(
                "http://localhost:8080/api/v1/certificados/verificar/UAJMS-"));
    }

    @Test
    void certificadoCurricularValidoUsaTipoYHorasDelEvento() {
        evento.setTipoCertificado(TipoCertificadoEvento.CURRICULAR);
        evento.setHorasAcademicas(40);
        when(sesionEventoRepository.countByEventoIdAndRequiereAsistenciaTrue(evento.getId())).thenReturn(7L);
        when(asistenciaRepository.countSesionesRequeridasAsistidas(inscripcionId, evento.getId())).thenReturn(6L);

        var respuesta = service.generarCertificado(inscripcionId);

        assertEquals("CURRICULAR", respuesta.getTipoCertificado());
        assertEquals(40, respuesta.getHorasAcademicas());
        assertEquals(new BigDecimal("85.71"), respuesta.getPorcentajeAsistencia());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoEvento.class, names = {"BORRADOR", "EN_REVISION", "PUBLICADO", "RECHAZADO", "CANCELADO"})
    void eventoNoFinalizadoEsRechazado(EstadoEvento estado) {
        evento.setEstado(estado);
        assertThrows(NegocioException.class, () -> service.generarCertificado(inscripcionId));
        verify(certificadoRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoInscripcion.class, names = {"PENDIENTE_PAGO", "PENDIENTE_VALIDACION", "CANCELADA"})
    void inscripcionNoConfirmadaEsRechazada(EstadoInscripcion estado) {
        inscripcion.setEstado(estado);
        assertThrows(NegocioException.class, () -> service.generarCertificado(inscripcionId));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPago.class, names = {"PENDIENTE_PAGO", "PENDIENTE_VALIDACION", "RECHAZADO"})
    void eventoPagadoRechazaPagoNoAprobado(EstadoPago estado) {
        evento.setTipoInscripcion(TipoInscripcion.PAGO);
        Pago pago = Pago.builder().inscripcion(inscripcion).estado(estado).build();
        when(pagoRepository.findByInscripcionId(inscripcionId)).thenReturn(Optional.of(pago));
        assertThrows(NegocioException.class, () -> service.generarCertificado(inscripcionId));
    }

    @Test
    void eventoPagadoConPagoAprobadoEsPermitido() {
        evento.setTipoInscripcion(TipoInscripcion.PAGO);
        when(pagoRepository.findByInscripcionId(inscripcionId))
                .thenReturn(Optional.of(Pago.builder().inscripcion(inscripcion).estado(EstadoPago.APROBADO).build()));
        assertDoesNotThrow(() -> service.generarCertificado(inscripcionId));
    }

    @Test
    void eventoGratuitoNoConsultaPago() {
        assertDoesNotThrow(() -> service.generarCertificado(inscripcionId));
        verify(pagoRepository, never()).findByInscripcionId(any());
    }

    @Test
    void curricularExigeHorasAcademicasPositivas() {
        evento.setTipoCertificado(TipoCertificadoEvento.CURRICULAR);
        evento.setHorasAcademicas(0);
        assertThrows(NegocioException.class, () -> service.generarCertificado(inscripcionId));
    }

    @Test
    void curricularConMenosDelOchentaPorCientoEsRechazadoSinRedondear() {
        evento.setTipoCertificado(TipoCertificadoEvento.CURRICULAR);
        evento.setHorasAcademicas(20);
        when(sesionEventoRepository.countByEventoIdAndRequiereAsistenciaTrue(evento.getId())).thenReturn(5L);
        when(asistenciaRepository.countSesionesRequeridasAsistidas(inscripcionId, evento.getId())).thenReturn(3L);
        assertThrows(NegocioException.class, () -> service.generarCertificado(inscripcionId));
    }

    @Test
    void curricularConOchentaExactoEsPermitido() {
        evento.setTipoCertificado(TipoCertificadoEvento.CURRICULAR);
        evento.setHorasAcademicas(20);
        when(sesionEventoRepository.countByEventoIdAndRequiereAsistenciaTrue(evento.getId())).thenReturn(5L);
        when(asistenciaRepository.countSesionesRequeridasAsistidas(inscripcionId, evento.getId())).thenReturn(4L);
        assertDoesNotThrow(() -> service.generarCertificado(inscripcionId));
    }

    @Test
    void eventoSinSesionesRequeridasCumpleSinDividirPorCero() {
        evento.setTipoCertificado(TipoCertificadoEvento.CURRICULAR);
        evento.setHorasAcademicas(10);
        var respuesta = service.generarCertificado(inscripcionId);
        assertEquals(new BigDecimal("100.00"), respuesta.getPorcentajeAsistencia());
    }

    @Test
    void consultaDeAsistenciaCuentaDistintosYSoloRequeridasDeInscripcionYEvento() throws Exception {
        Query query = AsistenciaRepository.class
                .getMethod("countSesionesRequeridasAsistidas", UUID.class, UUID.class)
                .getAnnotation(Query.class);
        String jpql = query.value();
        assertTrue(jpql.contains("COUNT(DISTINCT a.sesionEvento.id)"));
        assertTrue(jpql.contains("a.inscripcion.id = :inscripcionId"));
        assertTrue(jpql.contains("a.sesionEvento.evento.id = :eventoId"));
        assertTrue(jpql.contains("a.sesionEvento.requiereAsistencia = true"));
    }

    @Test
    void generacionDuplicadaEsIdempotente() {
        Certificado existente = certificado();
        when(certificadoRepository.findByInscripcionId(inscripcionId)).thenReturn(Optional.of(existente));
        var respuesta = service.generarCertificado(inscripcionId);
        assertEquals(existente.getId(), respuesta.getId());
        verify(certificadoRepository, never()).save(any());
    }

    @Test
    void codigoEsNoPredecibleLargoYUnico() {
        service.generarCertificado(inscripcionId);
        verify(certificadoRepository, atLeastOnce()).save(argThat(c -> c.getCodigoCertificado().matches("UAJMS-[0-9A-F]{32}")));
        verify(certificadoRepository).existsByCodigoCertificado(anyString());
    }

    @Test
    void usuarioNoPuedeGenerarCertificados() {
        when(auth.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(auth.tieneRol("ORGANIZADOR")).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.generarCertificado(inscripcionId));
    }

    @Test
    void usuarioConsultaCertificadoPropio() {
        autenticarUsuario(participante);
        Certificado certificado = certificado();
        when(certificadoRepository.findByIdAndUsuarioId(certificado.getId(), participante.getId()))
                .thenReturn(Optional.of(certificado));
        assertEquals(certificado.getId(), service.obtenerPorId(certificado.getId()).getId());
    }

    @Test
    void usuarioNoConsultaCertificadoAjeno() {
        autenticarUsuario(participante);
        UUID idAjeno = UUID.randomUUID();
        when(certificadoRepository.findByIdAndUsuarioId(idAjeno, participante.getId())).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(idAjeno));
        verify(certificadoRepository, never()).findById(idAjeno);
    }

    @Test
    void organizadorListaSoloCertificadosDeEventoPropio() {
        Certificado certificado = certificado();
        when(certificadoRepository.findByEventoIdAndEventoOrganizadorId(evento.getId(), organizador.getId()))
                .thenReturn(List.of(certificado));
        assertEquals(1, service.listarPorEvento(evento.getId()).size());
        verify(certificadoRepository, never()).findByEventoId(evento.getId());
    }

    @Test
    void organizadorConsultaCertificadoDeEventoPropio() {
        Certificado certificado = certificado();
        when(certificadoRepository.findByIdAndEventoOrganizadorId(certificado.getId(), organizador.getId()))
                .thenReturn(Optional.of(certificado));
        assertEquals(certificado.getId(), service.obtenerPorId(certificado.getId()).getId());
    }

    @Test
    void organizadorNoConsultaNiGeneraPdfDeEventoAjeno() {
        UUID idAjeno = UUID.randomUUID();
        when(certificadoRepository.findByIdAndEventoOrganizadorId(idAjeno, organizador.getId()))
                .thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.descargarPdf(idAjeno));
        verify(documentoService, never()).generarPdf(any());
    }

    @Test
    void organizadorNoGeneraParaEventoAjeno() {
        when(inscripcionRepository.findByIdAndEventoOrganizadorForUpdate(inscripcionId, organizador.getId()))
                .thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.generarCertificado(inscripcionId));
        verify(certificadoRepository, never()).save(any());
    }

    @Test
    void organizadorNoListaCertificadosDeEventoAjeno() {
        when(eventoRepository.findByIdAndOrganizadorId(evento.getId(), organizador.getId()))
                .thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.listarPorEvento(evento.getId()));
        verify(certificadoRepository, never()).findByEventoIdAndEventoOrganizadorId(any(), any());
    }

    @Test
    void administradorTieneAccesoGlobal() {
        when(auth.tieneRol("ADMINISTRADOR")).thenReturn(true);
        when(inscripcionRepository.findByIdForUpdate(inscripcionId)).thenReturn(Optional.of(inscripcion));
        assertDoesNotThrow(() -> service.generarCertificado(inscripcionId));
    }

    @Test
    void administradorConsultaCertificadoGlobalmente() {
        Certificado certificado = certificado();
        when(auth.tieneRol("ADMINISTRADOR")).thenReturn(true);
        when(certificadoRepository.findById(certificado.getId())).thenReturn(Optional.of(certificado));
        assertEquals(certificado.getId(), service.obtenerPorId(certificado.getId()).getId());
    }

    @Test
    void verificacionPublicaValidaNoExponeCamposSensibles() {
        Certificado certificado = certificado();
        when(certificadoRepository.findByCodigoCertificado(certificado.getCodigoCertificado()))
                .thenReturn(Optional.of(certificado));
        VerificacionCertificadoResponse respuesta = service.verificarCertificadoPublico(certificado.getCodigoCertificado());
        assertTrue(respuesta.isValido());
        assertEquals("Congreso UAJMS", respuesta.getEvento());
        assertThrows(NoSuchFieldException.class, () -> VerificacionCertificadoResponse.class.getDeclaredField("ci"));
        assertThrows(NoSuchFieldException.class, () -> VerificacionCertificadoResponse.class.getDeclaredField("email"));
        assertThrows(NoSuchFieldException.class, () -> VerificacionCertificadoResponse.class.getDeclaredField("telefono"));
    }

    @Test
    void verificacionPublicaDeCodigoInexistenteEsControlada() {
        when(certificadoRepository.findByCodigoCertificado("NO-EXISTE")).thenReturn(Optional.empty());
        var respuesta = service.verificarCertificadoPublico("NO-EXISTE");
        assertFalse(respuesta.isValido());
        assertEquals("NO_REGISTRADO", respuesta.getEstado());
    }

    @Test
    void relacionesDeTrazabilidadNoSonActualizables() throws Exception {
        for (String campo : List.of("usuario", "evento", "inscripcion")) {
            Field field = Certificado.class.getDeclaredField(campo);
            assertFalse(field.getAnnotation(JoinColumn.class).updatable());
        }
    }

    private void autenticarOrganizador() {
        lenient().when(auth.tieneRol("ADMINISTRADOR")).thenReturn(false);
        lenient().when(auth.tieneRol("ORGANIZADOR")).thenReturn(true);
        lenient().when(auth.obtenerUsuario()).thenReturn(organizador);
    }

    private void autenticarUsuario(Usuario usuario) {
        when(auth.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(auth.tieneRol("ORGANIZADOR")).thenReturn(false);
        when(auth.tieneRol("USUARIO")).thenReturn(true);
        when(auth.obtenerUsuario()).thenReturn(usuario);
    }

    private Usuario usuario(String nombre) {
        Usuario usuario = Usuario.builder().nombres(nombre).apellidos("Prueba").ci(UUID.randomUUID().toString()).build();
        usuario.setId(UUID.randomUUID());
        return usuario;
    }

    private Certificado certificado() {
        Certificado certificado = Certificado.builder().usuario(participante).evento(evento).inscripcion(inscripcion)
                .codigoCertificado("UAJMS-1234567890ABCDEF1234567890ABCDEF")
                .tipoCertificado(Certificado.TipoCertificado.NO_CURRICULAR)
                .estado(Certificado.EstadoCertificado.GENERADO).porcentajeAsistencia(new BigDecimal("100.00"))
                .urlVerificacion("/api/v1/certificados/verificar/UAJMS-1234567890ABCDEF1234567890ABCDEF")
                .build();
        certificado.setId(UUID.randomUUID());
        return certificado;
    }
}
