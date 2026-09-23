package bo.uajms.eventos.core.seguridad;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RolSistemaTest {

    @Test
    void contieneExactamenteLosTresRolesOficiales() {
        Set<String> roles = Arrays.stream(RolSistema.values()).map(Enum::name).collect(Collectors.toSet());
        assertEquals(Set.of("ADMINISTRADOR", "ORGANIZADOR", "USUARIO"), roles);
    }

    @Test
    void noReconoceRolesFueraDelModeloOficial() {
        assertFalse(RolSistema.esOficial("ROL_NO_OFICIAL"));
    }
}
