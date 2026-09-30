package bo.uajms.eventos.modulos.certificados;

import bo.uajms.eventos.modulos.certificados.entidades.Certificado;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CertificadoPersistenceContractTest {

    @Test
    void emisionConcurrenteSerializaLaInscripcion() throws Exception {
        Lock administrador = InscripcionRepository.class
                .getMethod("findByIdForUpdate", UUID.class).getAnnotation(Lock.class);
        Lock organizador = InscripcionRepository.class
                .getMethod("findByIdAndEventoOrganizadorForUpdate", UUID.class, UUID.class).getAnnotation(Lock.class);

        assertEquals(LockModeType.PESSIMISTIC_WRITE, administrador.value());
        assertEquals(LockModeType.PESSIMISTIC_WRITE, organizador.value());
    }

    @Test
    void persistenciaImpideDuplicarCertificadoPorInscripcionOCodigo() {
        Table table = Certificado.class.getAnnotation(Table.class);

        assertTrue(Arrays.stream(table.uniqueConstraints())
                .anyMatch(unique -> Arrays.asList(unique.columnNames()).contains("inscripcion_id")));
        assertTrue(Arrays.stream(table.uniqueConstraints())
                .anyMatch(unique -> Arrays.asList(unique.columnNames()).contains("codigo_certificado")));
    }
}
