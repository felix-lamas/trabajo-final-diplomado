package bo.uajms.eventos.modulos.pagos;

import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OneToOne;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PagoPersistenceContractTest {

    @Test
    void pagoPerteneceAUnaUnicaInscripcionObligatoria() throws Exception {
        Field inscripcion = Pago.class.getDeclaredField("inscripcion");
        OneToOne relacion = inscripcion.getAnnotation(OneToOne.class);
        JoinColumn columna = inscripcion.getAnnotation(JoinColumn.class);

        assertEquals("inscripcion_id", columna.name());
        assertFalse(columna.nullable());
        assertTrue(columna.unique());
        assertEquals(jakarta.persistence.FetchType.LAZY, relacion.fetch());
    }

    @Test
    void operacionesConcurrentesBloqueanElMismoPagoParaEscritura() throws Exception {
        assertWriteLock("findByIdForUpdate", UUID.class);
        assertWriteLock("findByIdAndUsuarioForUpdate", UUID.class, UUID.class);
        assertWriteLock("findByIdAndOrganizadorForUpdate", UUID.class, UUID.class);
        assertWriteLock("findByInscripcionIdAndUsuarioIdForUpdate", UUID.class, UUID.class);
    }

    private void assertWriteLock(String metodo, Class<?>... parametros) throws Exception {
        Lock lock = PagoRepository.class.getMethod(metodo, parametros).getAnnotation(Lock.class);
        assertEquals(LockModeType.PESSIMISTIC_WRITE, lock.value());
    }
}
