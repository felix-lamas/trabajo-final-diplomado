package bo.uajms.eventos.modulos.eventos.mappers;

import bo.uajms.eventos.modulos.categorias.entidades.CategoriaEvento;
import bo.uajms.eventos.modulos.eventos.dtos.EventoDetalleResponse;
import bo.uajms.eventos.modulos.eventos.dtos.EventoResponse;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventoMapperQrPagoTest {
    private final EventoMapper mapper = new EventoMapper();

    @Test
    void responseExponeRutaVidiaPeroNuncaStorageKey() {
        UUID id = UUID.randomUUID();
        Evento evento = Evento.builder()
                .categoria(CategoriaEvento.builder().nombre("Taller").build())
                .organizador(Usuario.builder().nombres("Org").apellidos("Test").build())
                .build();
        evento.setId(id);
        evento.setQrPagoStorageKey("eventos/secret/qr-pago/private.png");
        EventoDetalleResponse response = mapper.toDetalleResponse(evento);
        assertEquals("/api/v1/eventos/" + id + "/qr-pago", response.getQrPagoUrl());
        assertFalse(Arrays.stream(response.getClass().getDeclaredFields())
                .anyMatch(field -> field.getName().toLowerCase().contains("storage")));
        assertFalse(Arrays.stream(EventoResponse.class.getDeclaredFields())
                .anyMatch(field -> field.getName().equals("qrPagoUrl")));
    }

    @Test
    void conservaUrlLegacySoloComoCompatibilidadDeLectura() {
        Evento evento = Evento.builder()
                .categoria(CategoriaEvento.builder().nombre("Taller").build())
                .organizador(Usuario.builder().nombres("Org").apellidos("Test").build())
                .qrPagoUrl("https://legacy.example.test/qr.png")
                .build();
        evento.setId(UUID.randomUUID());
        assertEquals("https://legacy.example.test/qr.png", mapper.toDetalleResponse(evento).getQrPagoUrl());
    }
}
