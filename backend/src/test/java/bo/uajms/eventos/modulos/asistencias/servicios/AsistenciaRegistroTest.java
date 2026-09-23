package bo.uajms.eventos.modulos.asistencias.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.dtos.RegistrarAsistenciaRequest;
import bo.uajms.eventos.modulos.asistencias.entidades.Asistencia;
import bo.uajms.eventos.modulos.asistencias.infraestructura.QrAsistenciaTemporal;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.*;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.sesiones.entidades.SesionEvento;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsistenciaRegistroTest {
    @Mock AsistenciaRepository asistenciaRepository; @Mock EventoRepository eventoRepository;
    @Mock InscripcionRepository inscripcionRepository; @Mock QrAsistenciaService qrService;
    @Mock UsuarioAutenticadoService auth; @Mock Clock clock; @InjectMocks AsistenciaService service;
    LocalDateTime ahora=LocalDateTime.of(2026,9,23,10,0); Usuario usuario; Evento evento; SesionEvento sesion;
    Inscripcion inscripcion; QrAsistenciaTemporal qr; RegistrarAsistenciaRequest request;

    @BeforeEach void setup(){
        usuario=Usuario.builder().correoElectronico("u@test.local").build(); usuario.setId(UUID.randomUUID());
        evento=Evento.builder().organizador(Usuario.builder().build()).estado(EstadoEvento.PUBLICADO).build(); evento.setId(UUID.randomUUID());
        sesion=SesionEvento.builder().evento(evento).fecha(ahora.toLocalDate()).horaInicio(LocalTime.of(9,0)).horaFin(LocalTime.of(11,0)).requiereAsistencia(true).activa(true).latitud(new BigDecimal("-21.5354900")).longitud(new BigDecimal("-64.7295600")).radioMetros(100).build(); sesion.setId(UUID.randomUUID());
        inscripcion=Inscripcion.builder().usuario(usuario).evento(evento).estado(EstadoInscripcion.CONFIRMADA).build(); inscripcion.setId(UUID.randomUUID());
        qr=QrAsistenciaTemporal.builder().sesionEvento(sesion).activo(true).emitidoEn(ahora.minusSeconds(30)).expiraEn(ahora.plusSeconds(90)).build();
        request=new RegistrarAsistenciaRequest(); request.setToken("token-valido"); request.setLatitud(sesion.getLatitud()); request.setLongitud(sesion.getLongitud()); request.setPrecision(new BigDecimal("10"));
        lenient().when(clock.instant()).thenReturn(ahora.atZone(ZoneId.of("America/La_Paz")).toInstant()); lenient().when(clock.getZone()).thenReturn(ZoneId.of("America/La_Paz"));
    }

    @Test void usuarioInscritoRegistraSuAsistencia(){ prepararValido(); when(asistenciaRepository.saveAndFlush(any())).thenAnswer(i->i.getArgument(0)); Asistencia a=service.registrar(request); assertSame(usuario,a.getRegistradoPor()); assertSame(inscripcion,a.getInscripcion()); assertSame(sesion,a.getSesionEvento()); }
    @Test void usuarioNoInscritoEsRechazado(){ prepararHastaQr(); when(inscripcionRepository.findByUsuarioIdAndEventoId(usuario.getId(),evento.getId())).thenReturn(Optional.empty()); assertThrows(NegocioException.class,()->service.registrar(request)); sinEscrituras(); }
    @Test void inscripcionDeOtroUsuarioEsRechazada(){ prepararHastaQr(); Usuario otro=Usuario.builder().build();otro.setId(UUID.randomUUID());inscripcion.setUsuario(otro); when(inscripcionRepository.findByUsuarioIdAndEventoId(usuario.getId(),evento.getId())).thenReturn(Optional.of(inscripcion)); assertThrows(NegocioException.class,()->service.registrar(request)); }
    @Test void inscripcionCanceladaEsRechazada(){ inscripcion.setEstado(EstadoInscripcion.CANCELADA); prepararConInscripcion(); assertThrows(NegocioException.class,()->service.registrar(request)); }
    @Test void inscripcionRechazadaEsRechazada(){ inscripcion.setEstado(EstadoInscripcion.RECHAZADA); prepararConInscripcion(); assertThrows(NegocioException.class,()->service.registrar(request)); }
    @Test void inscripcionPendientePagoEsRechazada(){ inscripcion.setEstado(EstadoInscripcion.PENDIENTE_PAGO); prepararConInscripcion(); assertThrows(NegocioException.class,()->service.registrar(request)); }
    @Test void inscripcionPendienteValidacionEsRechazada(){ inscripcion.setEstado(EstadoInscripcion.PENDIENTE_VALIDACION); prepararConInscripcion(); assertThrows(NegocioException.class,()->service.registrar(request)); }
    @Test void inscripcionDeOtroEventoEsRechazada(){ prepararHastaQr(); Evento otro=Evento.builder().build();otro.setId(UUID.randomUUID());inscripcion.setEvento(otro);when(inscripcionRepository.findByUsuarioIdAndEventoId(usuario.getId(),evento.getId())).thenReturn(Optional.of(inscripcion));assertThrows(NegocioException.class,()->service.registrar(request)); }
    @Test void qrInvalidoEsRechazado(){ autenticarUsuario(); when(qrService.resolverToken(anyString())).thenThrow(new NegocioException("QR invalido")); assertThrows(NegocioException.class,()->service.registrar(request)); sinEscrituras(); }
    @Test void qrExpiradoEsRechazado(){ prepararConInscripcion(); doThrow(new NegocioException("QR expirado")).when(qrService).validarVigencia(qr,ahora); assertThrows(NegocioException.class,()->service.registrar(request)); }
    @Test void qrRevocadoEsRechazado(){ prepararConInscripcion(); doThrow(new NegocioException("QR revocado")).when(qrService).validarVigencia(qr,ahora); assertThrows(NegocioException.class,()->service.registrar(request)); }
    @Test void sesionAntesDeHorarioEsRechazada(){ sesion.setHoraInicio(LocalTime.of(10,1));sesion.setHoraFin(LocalTime.of(12,0));prepararConInscripcion();assertThrows(NegocioException.class,()->service.registrar(request)); }
    @Test void sesionDespuesDeHorarioEsRechazada(){ sesion.setHoraInicio(LocalTime.of(8,0));sesion.setHoraFin(LocalTime.of(9,59));prepararConInscripcion();assertThrows(NegocioException.class,()->service.registrar(request)); }
    @Test void gpsFueraDeRangoEsRechazado(){ request.setLatitud(new BigDecimal("91"));prepararConInscripcion();assertThrows(NegocioException.class,()->service.registrar(request)); }
    @Test void precisionMayorA30EsRechazada(){ request.setPrecision(new BigDecimal("30.01"));prepararConInscripcion();assertThrows(NegocioException.class,()->service.registrar(request)); }
    @Test void distanciaMasPrecisionFueraDelRadioEsRechazada(){ request.setLatitud(new BigDecimal("-21.5340000"));prepararConInscripcion();assertThrows(NegocioException.class,()->service.registrar(request)); }
    @Test void distanciaMasPrecisionDentroDelRadioEsAceptada(){ request.setLatitud(new BigDecimal("-21.5358500"));request.setPrecision(new BigDecimal("20"));prepararValido();when(asistenciaRepository.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));assertDoesNotThrow(()->service.registrar(request)); }
    @Test void duplicadoDetectadoAntesDeGuardar(){ prepararConInscripcion();when(asistenciaRepository.existsByInscripcionIdAndSesionEventoId(inscripcion.getId(),sesion.getId())).thenReturn(true);assertThrows(NegocioException.class,()->service.registrar(request));sinEscrituras(); }
    @Test void constraintDeBaseResuelveCarreraConcurrente(){ prepararConInscripcion();when(asistenciaRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unique"));assertThrows(NegocioException.class,()->service.registrar(request)); }
    @Test void organizadorNoRegistraComoParticipante(){ when(auth.tieneRol("USUARIO")).thenReturn(false);assertThrows(AccessDeniedException.class,()->service.registrar(request));verify(qrService,never()).resolverToken(any()); }
    @Test void noSeAlmacenanCoordenadasDelParticipante(){ prepararValido();when(asistenciaRepository.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));Asistencia a=service.registrar(request);assertNotNull(a.getDistanciaMetros());assertNotNull(a.getPrecisionGpsMetros());assertTrue(Arrays.stream(Asistencia.class.getDeclaredFields()).noneMatch(f->f.getName().equals("latitud")||f.getName().equals("longitud"))); }
    @Test void registroNoEliminaAsistenciaHistorica(){ prepararValido();when(asistenciaRepository.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));service.registrar(request);verify(asistenciaRepository,never()).delete(any());verify(asistenciaRepository,never()).deleteAll(); }
    @Test void haversineMismoPuntoEsCero(){ assertEquals(0d,AsistenciaService.haversine(-21.53549,-64.72956,-21.53549,-64.72956),0.001); }
    @Test void eventoPublicadoPermiteAsistencia(){ prepararValido();when(asistenciaRepository.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));assertDoesNotThrow(()->service.registrar(request)); }
    @Test void eventoCanceladoRechazaAsistencia(){ evento.setEstado(EstadoEvento.CANCELADO);prepararConInscripcion();assertThrows(NegocioException.class,()->service.registrar(request));sinEscrituras(); }
    @Test void eventoFinalizadoRechazaAsistencia(){ evento.setEstado(EstadoEvento.FINALIZADO);prepararConInscripcion();assertThrows(NegocioException.class,()->service.registrar(request));sinEscrituras(); }
    @Test void eventoBorradorRechazaAsistencia(){ evento.setEstado(EstadoEvento.BORRADOR);prepararConInscripcion();assertThrows(NegocioException.class,()->service.registrar(request));sinEscrituras(); }

    private void prepararValido(){ prepararConInscripcion(); lenient().when(asistenciaRepository.existsByInscripcionIdAndSesionEventoId(inscripcion.getId(),sesion.getId())).thenReturn(false); }
    private void prepararConInscripcion(){ prepararHastaQr(); when(inscripcionRepository.findByUsuarioIdAndEventoId(usuario.getId(),evento.getId())).thenReturn(Optional.of(inscripcion)); }
    private void prepararHastaQr(){ autenticarUsuario(); when(qrService.resolverToken("token-valido")).thenReturn(qr); }
    private void autenticarUsuario(){ when(auth.tieneRol("USUARIO")).thenReturn(true);when(auth.obtenerUsuario()).thenReturn(usuario); }
    private void sinEscrituras(){verify(asistenciaRepository,never()).saveAndFlush(any());}
}
