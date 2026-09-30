package bo.uajms.eventos.modulos.eventos.dtos;

import bo.uajms.eventos.modulos.eventos.entidades.*;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventoDatosRequestValidationTest {
    private static Validator validator;

    @BeforeAll static void prepararValidador() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test void tituloObligatorio() {
        CrearEventoRequest request = solicitudValida(); request.setTitulo(null);
        assertTrue(tieneError(request, "titulo"));
    }

    @Test void tituloVacioEsInvalido() {
        CrearEventoRequest request = solicitudValida(); request.setTitulo("   ");
        assertTrue(tieneError(request, "titulo"));
    }

    @Test void fechasYHorasSonObligatorias() {
        CrearEventoRequest request = solicitudValida(); request.setFechaInicio(null); request.setHoraFin(null);
        assertTrue(tieneError(request, "fechaInicio"));
        assertTrue(tieneError(request, "horaFin"));
    }

    @Test void categoriaYAudienciaSonObligatorias() {
        CrearEventoRequest request = solicitudValida(); request.setCategoriaId(null); request.setPublicoObjetivo(null);
        assertTrue(tieneError(request, "categoriaId"));
        assertTrue(tieneError(request, "publicoObjetivo"));
    }

    @Test void coordenadasFueraDeRangoSonInvalidas() {
        CrearEventoRequest request = solicitudValida(); request.setLatitud(new BigDecimal("91")); request.setLongitud(new BigDecimal("-181"));
        assertTrue(tieneError(request, "latitud"));
        assertTrue(tieneError(request, "longitud"));
    }

    @Test void contactoValidoCumpleContrato() {
        CrearEventoRequest request = solicitudValida(); request.setTelefonoContacto("+591 70000000"); request.setEmailContacto("evento@example.test");
        assertFalse(tieneError(request, "telefonoContacto"));
        assertFalse(tieneError(request, "emailContacto"));
    }

    @Test void audienciaUsaExactamenteLosValoresOficiales() {
        assertArrayEquals(new PublicoObjetivo[]{PublicoObjetivo.UAJMS, PublicoObjetivo.EXTERNO, PublicoObjetivo.AMBOS},
                PublicoObjetivo.values());
    }

    private boolean tieneError(CrearEventoRequest request, String campo) {
        return validator.validate(request).stream().anyMatch(error -> error.getPropertyPath().toString().equals(campo));
    }

    private CrearEventoRequest solicitudValida() {
        CrearEventoRequest request = new CrearEventoRequest();
        request.setTitulo("Evento"); request.setCategoriaId(UUID.randomUUID()); request.setModalidad(Modalidad.PRESENCIAL);
        request.setTipoInscripcion(TipoInscripcion.GRATUITO); request.setCosto(BigDecimal.ZERO);
        request.setFechaInicio(LocalDate.now().plusDays(1)); request.setFechaFin(LocalDate.now().plusDays(1));
        request.setHoraInicio(LocalTime.of(8, 0)); request.setHoraFin(LocalTime.of(10, 0));
        request.setRequiereInscripcion(true); request.setCupoLimitado(false); request.setEmiteCertificado(false);
        request.setPublicoObjetivo(PublicoObjetivo.AMBOS);
        return request;
    }
}
