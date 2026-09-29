package bo.uajms.eventos.modulos.usuarios.repositorios;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UsuarioRepositoryLockTest {

    @Test
    void transicionesDeOrganizadorUsanBloqueoPesimistaDeUsuario() throws NoSuchMethodException {
        Lock lock = UsuarioRepository.class.getMethod("findByIdForUpdate", UUID.class)
                .getAnnotation(Lock.class);

        assertNotNull(lock);
        assertEquals(LockModeType.PESSIMISTIC_WRITE, lock.value());
    }
}
