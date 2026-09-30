package bo.uajms.eventos.modulos.asistencias;

import bo.uajms.eventos.modulos.asistencias.entidades.Asistencia;
import bo.uajms.eventos.modulos.asistencias.infraestructura.QrAsistenciaTemporalRepository;
import bo.uajms.eventos.modulos.sesiones.entidades.SesionEvento;
import bo.uajms.eventos.modulos.sesiones.repositorios.SesionEventoRepository;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsistenciaPersistenceContractTest {

    @Test
    void baseDeDatosImpideDuplicadoEIndexaConsultasPrincipales() {
        Table asistencia = Asistencia.class.getAnnotation(Table.class);
        assertTrue(Arrays.stream(asistencia.uniqueConstraints()).anyMatch(constraint ->
                constraint.name().equals("uk_asistencia_inscripcion_sesion")
                        && Arrays.equals(constraint.columnNames(), new String[]{"inscripcion_id", "sesion_evento_id"})));
        assertTrue(Arrays.stream(asistencia.indexes()).anyMatch(index ->
                index.name().equals("idx_asistencia_sesion") && index.columnList().equals("sesion_evento_id")));

        Table sesion = SesionEvento.class.getAnnotation(Table.class);
        assertTrue(Arrays.stream(sesion.indexes()).anyMatch(index ->
                index.name().equals("idx_sesion_evento_evento_fecha")
                        && index.columnList().equals("evento_id,fecha,hora_inicio")));
    }

    @Test
    void sesionYQrUsanBloqueoPesimistaEnOperacionesConcurrentes() throws Exception {
        assertWriteLock(SesionEventoRepository.class, "findByIdForUpdate", UUID.class);
        assertWriteLock(SesionEventoRepository.class, "findByIdAndEventoOrganizadorIdForUpdate", UUID.class, UUID.class);
        assertWriteLock(QrAsistenciaTemporalRepository.class, "findByTokenHashForUpdate", String.class);
        assertWriteLock(QrAsistenciaTemporalRepository.class, "findActivosBySesionIdForUpdate", UUID.class);
    }

    private void assertWriteLock(Class<?> repository, String metodo, Class<?>... parametros) throws Exception {
        Lock lock = repository.getMethod(metodo, parametros).getAnnotation(Lock.class);
        assertEquals(LockModeType.PESSIMISTIC_WRITE, lock.value());
    }
}
