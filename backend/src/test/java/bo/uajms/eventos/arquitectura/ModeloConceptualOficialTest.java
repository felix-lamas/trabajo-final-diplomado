package bo.uajms.eventos.arquitectura;

import bo.uajms.eventos.modulos.asistencias.entidades.Asistencia;
import bo.uajms.eventos.modulos.categorias.entidades.CategoriaEvento;
import bo.uajms.eventos.modulos.certificados.entidades.Certificado;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import bo.uajms.eventos.modulos.sesiones.entidades.SesionEvento;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModeloConceptualOficialTest {

    @Test
    void lasOchoEntidadesConceptualesSonPersistentes() {
        List.of(Usuario.class, Evento.class, CategoriaEvento.class, Inscripcion.class,
                        Pago.class, SesionEvento.class, Asistencia.class, Certificado.class)
                .forEach(tipo -> assertNotNull(tipo.getAnnotation(Entity.class), tipo.getSimpleName()));
    }

    @Test
    void lasEntidadesConceptualesRetiradasYaNoEstanEnElBackend() {
        List.of(
                "bo.uajms.eventos.modulos.facultades.entidades.Facultad",
                "bo.uajms.eventos.modulos.carreras.entidades.Carrera",
                "bo.uajms.eventos.modulos.pagos.entidades.ComprobantePago",
                "bo.uajms.eventos.modulos.credenciales.entidades.Credencial",
                "bo.uajms.eventos.modulos.codigo_qr.entidades.CodigoQr",
                "bo.uajms.eventos.modulos.control_acceso.entidades.ControlAcceso",
                "bo.uajms.eventos.modulos.encuestas.entidades.Encuesta",
                "bo.uajms.eventos.modulos.encuestas.entidades.PreguntaEncuesta",
                "bo.uajms.eventos.modulos.encuestas.entidades.RespuestaEncuesta"
        ).forEach(nombre -> assertThrows(ClassNotFoundException.class, () -> Class.forName(nombre), nombre));
    }
}
