package bo.uajms.eventos.modulos.asistencias.servicios;

import bo.uajms.eventos.core.excepciones.*;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.infraestructura.*;
import bo.uajms.eventos.modulos.eventos.entidades.*;
import bo.uajms.eventos.modulos.sesiones.entidades.SesionEvento;
import bo.uajms.eventos.modulos.sesiones.repositorios.SesionEventoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.access.AccessDeniedException;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QrAsistenciaServiceTest {
    @Mock QrAsistenciaTemporalRepository qrRepository;
    @Mock SesionEventoRepository sesionRepository;
    @Mock UsuarioAutenticadoService auth;
    @Mock Clock clock;
    @InjectMocks QrAsistenciaService service;
    LocalDateTime ahora=LocalDateTime.of(2026,9,23,10,0); UUID sesionId=UUID.randomUUID(), orgId=UUID.randomUUID();
    Usuario org; SesionEvento sesion;

    @BeforeEach void setup(){
        org=Usuario.builder().correoElectronico("org@test.local").build(); org.setId(orgId);
        Evento e=Evento.builder().estado(EstadoEvento.PUBLICADO).organizador(org).build(); e.setId(UUID.randomUUID());
        sesion=SesionEvento.builder().evento(e).fecha(ahora.toLocalDate()).horaInicio(LocalTime.of(9,0)).horaFin(LocalTime.of(12,0)).activa(true).requiereAsistencia(true).build(); sesion.setId(sesionId);
        lenient().when(clock.instant()).thenReturn(ahora.atZone(ZoneId.of("America/La_Paz")).toInstant()); lenient().when(clock.getZone()).thenReturn(ZoneId.of("America/La_Paz"));
        ReflectionTestUtils.setField(service, "jwtSecret", "test-secret-test-secret-test-secret");
    }

    @Test void generaTokenSeguroHashPersistidoYDuracionDosMinutos(){
        prepararPropio(); when(qrRepository.findActivosBySesionIdForUpdate(sesionId)).thenReturn(List.of()); when(qrRepository.save(any())).thenAnswer(i->i.getArgument(0));
        var r=service.generar(sesionId); var qr=capturar();
        assertNotNull(r.getToken()); assertEquals(ahora.plusMinutes(2),r.getExpiraEn()); assertNotEquals(r.getToken(),qr.getTokenHash()); assertEquals(64,qr.getTokenHash().length()); assertNotNull(qr.getTokenCifrado()); assertTrue(sesion.getControlAsistenciaActivo()); assertSame(sesion,qr.getSesionEvento());
    }

    @Test void nuevoQrRevocaAnterior(){
        prepararPropio(); var anterior=QrAsistenciaTemporal.builder().sesionEvento(sesion).activo(true).build();
        when(qrRepository.findActivosBySesionIdForUpdate(sesionId)).thenReturn(List.of(anterior)); when(qrRepository.save(any())).thenAnswer(i->i.getArgument(0));
        service.generar(sesionId); assertFalse(anterior.getActivo()); assertEquals(ahora,anterior.getRevocadoEn()); verify(qrRepository).saveAll(List.of(anterior));
    }

    @Test void generacionBloqueaSesionParaEvitarDosActivos(){
        prepararPropio(); when(qrRepository.findActivosBySesionIdForUpdate(sesionId)).thenReturn(List.of()); when(qrRepository.save(any())).thenAnswer(i->i.getArgument(0));
        service.generar(sesionId); verify(sesionRepository).findByIdForUpdate(sesionId);
    }

    @Test void usuarioNoGeneraQr(){
        when(auth.tieneRol("ADMINISTRADOR")).thenReturn(false); when(auth.tieneRol("ORGANIZADOR")).thenReturn(false);
        assertThrows(AccessDeniedException.class,()->service.generar(sesionId)); verify(sesionRepository,never()).findByIdForUpdate(any());
    }

    @Test void noGeneraQrAlFinalExactoDeLaSesion(){
        sesion.setHoraFin(ahora.toLocalTime()); prepararPropio();
        assertThrows(NegocioException.class, () -> service.generar(sesionId));
        verify(qrRepository, never()).save(any());
    }

    @Test void organizadorAjenoNoGeneraQr(){
        Usuario otro=Usuario.builder().build(); otro.setId(UUID.randomUUID()); when(auth.tieneRol("ADMINISTRADOR")).thenReturn(false); when(auth.tieneRol("ORGANIZADOR")).thenReturn(true); when(auth.obtenerUsuario()).thenReturn(otro); when(sesionRepository.findByIdForUpdate(sesionId)).thenReturn(Optional.of(sesion));
        assertThrows(RecursoNoEncontradoException.class,()->service.generar(sesionId)); verify(qrRepository,never()).save(any());
    }

    @Test void qrExpiradoEsRechazado(){
        var qr=QrAsistenciaTemporal.builder().activo(true).emitidoEn(ahora.minusMinutes(3)).expiraEn(ahora.minusSeconds(1)).build();
        assertThrows(NegocioException.class,()->service.validarVigencia(qr,ahora));
    }

    @Test void qrRevocadoEsRechazado(){
        var qr=QrAsistenciaTemporal.builder().activo(false).revocadoEn(ahora.minusSeconds(1)).emitidoEn(ahora.minusMinutes(1)).expiraEn(ahora.plusMinutes(1)).build();
        assertThrows(NegocioException.class,()->service.validarVigencia(qr,ahora));
    }

    @Test void tokenInexistenteEsQrInvalido(){
        when(qrRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());
        assertThrows(NegocioException.class,()->service.resolverToken("token-inexistente"));
    }

    @Test void resolverTokenBloqueaFilaParaSerializarRevocacionYRegistro(){
        var qr=QrAsistenciaTemporal.builder().sesionEvento(sesion).activo(true).build();
        when(qrRepository.findByTokenHash(anyString())).thenReturn(Optional.of(qr));
        assertSame(qr, service.resolverToken("token-valido"));
        verify(qrRepository).findByTokenHash(anyString());
    }

    @Test void qrActivoPeroExpiradoNoSeInformaComoVigente(){
        prepararPropioLectura(); var qr=QrAsistenciaTemporal.builder().sesionEvento(sesion).activo(true)
                .emitidoEn(ahora.minusMinutes(3)).expiraEn(ahora.minusSeconds(1)).build();
        when(qrRepository.findFirstBySesionEventoIdAndActivoTrueOrderByEmitidoEnDesc(sesionId)).thenReturn(Optional.of(qr));
        assertThrows(NegocioException.class, () -> service.obtenerActivo(sesionId));
    }

    @Test void tokenCifradoSeRecuperaParaOrganizadorAutorizado(){
        prepararPropio(); when(qrRepository.findActivosBySesionIdForUpdate(sesionId)).thenReturn(List.of());
        var generado=new java.util.concurrent.atomic.AtomicReference<QrAsistenciaTemporal>();
        when(qrRepository.save(any())).thenAnswer(i->{ var q=(QrAsistenciaTemporal)i.getArgument(0); generado.set(q); return q; });
        var inicial=service.generar(sesionId);
        when(sesionRepository.findById(sesionId)).thenReturn(Optional.of(sesion));
        when(qrRepository.findFirstBySesionEventoIdAndActivoTrueOrderByEmitidoEnDesc(sesionId)).thenAnswer(i->Optional.of(generado.get()));
        assertEquals(inicial.getToken(),service.obtenerActivo(sesionId).getToken());
    }

    @Test void renuevaAutomaticamenteQrExpiradoMientrasControlSigueActivo(){
        sesion.setControlAsistenciaActivo(true);
        when(sesionRepository.findByIdForUpdate(sesionId)).thenReturn(Optional.of(sesion));
        var expirado=QrAsistenciaTemporal.builder().sesionEvento(sesion).activo(true)
                .emitidoEn(ahora.minusMinutes(2)).expiraEn(ahora).build();
        when(qrRepository.findActivosBySesionIdForUpdate(sesionId)).thenReturn(List.of(expirado),List.of(expirado));
        when(qrRepository.save(any())).thenAnswer(i->i.getArgument(0));
        service.renovarExpirado(sesionId);
        assertFalse(expirado.getActivo());
        assertEquals(ahora,expirado.getRevocadoEn());
        verify(qrRepository,times(2)).findActivosBySesionIdForUpdate(sesionId);
        verify(qrRepository).save(any(QrAsistenciaTemporal.class));
    }

    @Test void detenerDesactivaControlYRevocaQrVigente(){
        prepararPropio();
        sesion.setControlAsistenciaActivo(true);
        var activo=QrAsistenciaTemporal.builder().sesionEvento(sesion).activo(true).build();
        when(qrRepository.findActivosBySesionIdForUpdate(sesionId)).thenReturn(List.of(activo));
        service.detener(sesionId);
        assertFalse(sesion.getControlAsistenciaActivo());
        assertFalse(activo.getActivo());
        assertEquals(ahora,activo.getRevocadoEn());
    }

    @Test void segundoConsumoDelMismoQrEsRechazadoSinEmitirOtro(){
        sesion.setControlAsistenciaActivo(true);
        var qr=QrAsistenciaTemporal.builder().sesionEvento(sesion).tokenHash("hash")
                .generadoPor(org).emitidoEn(ahora.minusSeconds(30)).expiraEn(ahora.plusSeconds(90)).activo(true).build();
        when(sesionRepository.findByIdForUpdate(sesionId)).thenReturn(Optional.of(sesion));
        when(qrRepository.findByTokenHashForUpdate("hash")).thenReturn(Optional.of(qr));
        when(qrRepository.findActivosBySesionIdForUpdate(sesionId)).thenReturn(List.of());
        when(qrRepository.save(any())).thenAnswer(i->i.getArgument(0));
        service.consumirYRotar(qr,ahora);
        assertFalse(qr.getActivo());
        assertNotNull(qr.getRevocadoEn());
        assertThrows(NegocioException.class,()->service.consumirYRotar(qr,ahora));
        verify(qrRepository,times(2)).save(any());
    }

    private void prepararPropio(){ when(auth.tieneRol("ADMINISTRADOR")).thenReturn(false); when(auth.tieneRol("ORGANIZADOR")).thenReturn(true); when(auth.obtenerUsuario()).thenReturn(org); when(sesionRepository.findByIdForUpdate(sesionId)).thenReturn(Optional.of(sesion)); }
    private void prepararPropioLectura(){ when(auth.tieneRol("ADMINISTRADOR")).thenReturn(false); when(auth.tieneRol("ORGANIZADOR")).thenReturn(true); when(auth.obtenerUsuario()).thenReturn(org); when(sesionRepository.findById(sesionId)).thenReturn(Optional.of(sesion)); }
    private QrAsistenciaTemporal capturar(){var c=ArgumentCaptor.forClass(QrAsistenciaTemporal.class);verify(qrRepository).save(c.capture());return c.getValue();}
}
