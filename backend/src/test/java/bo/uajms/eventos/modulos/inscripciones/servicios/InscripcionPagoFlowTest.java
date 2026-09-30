package bo.uajms.eventos.modulos.inscripciones.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.dtos.CrearInscripcionRequest;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.mappers.InscripcionMapper;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscripcionPagoFlowTest {

    @Mock InscripcionRepository inscripcionRepository;
    @Mock EventoRepository eventoRepository;
    @Mock InscripcionMapper inscripcionMapper;
    @Mock UsuarioAutenticadoService usuarioAutenticadoService;
    @Mock PagoRepository pagoRepository;
    @Mock Clock clock;
    @InjectMocks InscripcionService service;

    private Usuario usuario;
    private Evento evento;
    private CrearInscripcionRequest request;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().correoElectronico("usuario@example.test").build();
        usuario.setId(UUID.randomUUID());
        evento = Evento.builder().estado(EstadoEvento.PUBLICADO).requiereInscripcion(true)
                .tipoInscripcion(TipoInscripcion.GRATUITO).costo(BigDecimal.ZERO)
                .cupoLimitado(false).build();
        evento.setId(UUID.randomUUID());
        request = new CrearInscripcionRequest();
        request.setEventoId(evento.getId());
        when(usuarioAutenticadoService.tieneRol("USUARIO")).thenReturn(true);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
        when(eventoRepository.findByIdForUpdate(evento.getId())).thenReturn(Optional.of(evento));
        lenient().when(clock.instant()).thenReturn(Instant.parse("2026-09-23T14:00:00Z"));
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("America/La_Paz"));
        lenient().when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(i -> {
            Inscripcion inscripcion = i.getArgument(0);
            if (inscripcion.getId() == null) inscripcion.setId(UUID.randomUUID());
            return inscripcion;
        });
    }

    @Test
    void eventoGratuitoConfirmaInscripcionSinCrearPago() {
        service.inscribir(request);

        verify(inscripcionRepository).save(argThat(i -> i.getEstado() == EstadoInscripcion.CONFIRMADA));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void eventoPagadoCreaInscripcionYPagoPendientesConMontoOficial() {
        evento.setTipoInscripcion(TipoInscripcion.PAGO);
        evento.setCosto(new BigDecimal("80.00"));

        service.inscribir(request);

        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(captor.capture());
        Pago creado = captor.getValue();
        assertEquals(EstadoPago.PENDIENTE_PAGO, creado.getEstado());
        assertEquals(0, new BigDecimal("80.00").compareTo(creado.getMonto()));
        assertEquals(EstadoInscripcion.PENDIENTE_PAGO, creado.getInscripcion().getEstado());
    }

    @Test
    void eventoPagadoSinMontoValidoRevierteAntesDeCrearPagoValido() {
        evento.setTipoInscripcion(TipoInscripcion.PAGO);
        evento.setCosto(BigDecimal.ZERO);

        assertThrows(NegocioException.class, () -> service.inscribir(request));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void inscripcionDuplicadaNoCreaInscripcionNiPago() {
        when(inscripcionRepository.existsByUsuarioIdAndEventoId(usuario.getId(), evento.getId())).thenReturn(true);

        assertThrows(NegocioException.class, () -> service.inscribir(request));
        verify(inscripcionRepository, never()).save(any());
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void inscripcionPagadaReservaCupoBajoBloqueoDelEvento() {
        evento.setTipoInscripcion(TipoInscripcion.PAGO);
        evento.setCosto(BigDecimal.TEN);
        evento.setCupoLimitado(true);
        evento.setCupoMaximo(1);
        evento.setCupoDisponible(1);

        service.inscribir(request);

        assertEquals(0, evento.getCupoDisponible());
        verify(eventoRepository).findByIdForUpdate(evento.getId());
        verify(eventoRepository).save(evento);
    }

    @Test
    void sinCupoNoCreaInscripcionNiPago() {
        evento.setCupoLimitado(true);
        evento.setCupoMaximo(1);
        evento.setCupoDisponible(0);

        assertThrows(NegocioException.class, () -> service.inscribir(request));
        verify(inscripcionRepository, never()).save(any());
        verify(pagoRepository, never()).save(any());
    }
}
