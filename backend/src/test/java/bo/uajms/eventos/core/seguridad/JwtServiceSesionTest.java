package bo.uajms.eventos.core.seguridad;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceSesionTest {

    @Test
    void jwtContieneIdentificadorUnicoDeSesion() {
        JwtService servicio = new JwtService(propiedades(60_000L));
        UUID sesionId = UUID.randomUUID();
        UserDetails usuario = User.withUsername("usuario@example.test")
                .password("hash").authorities("ROLE_USUARIO").build();

        String jwt = servicio.generarToken(usuario, sesionId);

        assertEquals(sesionId, servicio.extraerIdSesion(jwt));
        assertEquals("usuario@example.test", servicio.extraerNombreUsuario(jwt));
    }

    @Test
    void jwtExpiradoEsRechazado() {
        JwtService servicio = new JwtService(propiedades(-1L));
        UserDetails usuario = User.withUsername("usuario@example.test")
                .password("hash").authorities("ROLE_USUARIO").build();
        String jwt = servicio.generarToken(usuario, UUID.randomUUID());

        assertThrows(ExpiredJwtException.class, () -> servicio.extraerNombreUsuario(jwt));
    }

    private JwtPropiedades propiedades(long expiracionMs) {
        JwtPropiedades propiedades = new JwtPropiedades();
        propiedades.setSecreto(Base64.getEncoder().encodeToString(new byte[32]));
        propiedades.setExpiracionMs(expiracionMs);
        return propiedades;
    }
}
