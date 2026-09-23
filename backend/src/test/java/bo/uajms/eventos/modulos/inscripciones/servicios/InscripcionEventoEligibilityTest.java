package bo.uajms.eventos.modulos.inscripciones.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.dtos.CrearInscripcionRequest;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.mappers.InscripcionMapper;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscripcionEventoEligibilityTest {

    @Mock InscripcionRepository inscripcionRepository;
    @Mock EventoRepository eventoRepository;
    @Mock InscripcionMapper inscripcionMapper;
    @Mock UsuarioAutenticadoService usuarioAutenticadoService;
    @Mock PagoRepository pagoRepository;
    @Mock Clock clock;
    @InjectMocks InscripcionService service;

    private UUID eventoId;
    private Usuario usuario;
    private CrearInscripcionRequest request;

    @BeforeEach
    void setUp() {
        eventoId = UUID.randomUUID();
        usuario = Usuario.builder().correoElectronico("usuario@test.local").build();
        usuario.setId(UUID.randomUUID());
        request = new CrearInscripcionRequest();
        request.setEventoId(eventoId);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
        lenient().when(clock.instant()).thenReturn(Instant.parse("2026-09-23T14:00:00Z"));
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("America/La_Paz"));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoEvento.class, names = {"BORRADOR", "EN_REVISION", "RECHAZADO", "CANCELADO", "FINALIZADO"})
    void eventoNoPublicadoNoAdmiteInscripcion(EstadoEvento estado) {
        Evento evento = evento(estado, true, true);
        when(eventoRepository.findByIdForUpdate(eventoId)).thenReturn(Optional.of(evento));
        assertThrows(NegocioException.class, () -> service.inscribir(request));
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void eventoQueNoRequiereInscripcionRechazaRegistro() {
        Evento evento = evento(EstadoEvento.PUBLICADO, false, false);
        when(eventoRepository.findByIdForUpdate(eventoId)).thenReturn(Optional.of(evento));
        assertThrows(NegocioException.class, () -> service.inscribir(request));
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void eventoPublicadoSinCupoLimitadoNoAplicaLimiteArtificial() {
        Evento evento = evento(EstadoEvento.PUBLICADO, true, false);
        when(eventoRepository.findByIdForUpdate(eventoId)).thenReturn(Optional.of(evento));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(i -> i.getArgument(0));
        assertDoesNotThrow(() -> service.inscribir(request));
        verify(eventoRepository, never()).save(evento);
    }

    private Evento evento(EstadoEvento estado, boolean requiereInscripcion, boolean cupoLimitado) {
        return Evento.builder().estado(estado).requiereInscripcion(requiereInscripcion)
                .cupoLimitado(cupoLimitado).cupoMaximo(cupoLimitado ? 10 : null)
                .cupoDisponible(cupoLimitado ? 10 : null).tipoInscripcion(TipoInscripcion.GRATUITO).build();
    }
}
