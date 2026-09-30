package bo.uajms.eventos.modulos.inscripciones;

import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InscripcionPersistenceContractTest {

    @Test
    void baseDeDatosImpideDuplicadoPorUsuarioYEvento() {
        Table table = Inscripcion.class.getAnnotation(Table.class);
        assertTrue(Arrays.stream(table.uniqueConstraints()).anyMatch(constraint ->
                constraint.name().equals("uk_inscripcion_usuario_evento")
                        && Arrays.equals(constraint.columnNames(), new String[]{"usuario_id", "evento_id"})));
    }

    @Test
    void reservaYCancellationDisponenDeBloqueosPesimistas() throws Exception {
        Lock eventoLock = EventoRepository.class.getMethod("findByIdForUpdate", UUID.class)
                .getAnnotation(Lock.class);
        Lock inscripcionLock = InscripcionRepository.class
                .getMethod("findByIdAndUsuarioForUpdate", UUID.class, UUID.class)
                .getAnnotation(Lock.class);
        Lock pagoLock = PagoRepository.class.getMethod("findByInscripcionIdAndUsuarioIdForUpdate", UUID.class, UUID.class)
                .getAnnotation(Lock.class);

        assertEquals(LockModeType.PESSIMISTIC_WRITE, eventoLock.value());
        assertEquals(LockModeType.PESSIMISTIC_WRITE, inscripcionLock.value());
        assertEquals(LockModeType.PESSIMISTIC_WRITE, pagoLock.value());
    }
}
