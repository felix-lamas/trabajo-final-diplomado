package bo.uajms.eventos.core.seguridad;

import java.util.Arrays;

public enum RolSistema {
    ADMINISTRADOR,
    ORGANIZADOR,
    USUARIO;

    public static boolean esOficial(String nombre) {
        return nombre != null && Arrays.stream(values()).anyMatch(rol -> rol.name().equals(nombre));
    }
}
