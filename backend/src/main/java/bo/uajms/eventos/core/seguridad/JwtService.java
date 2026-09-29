package bo.uajms.eventos.core.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtPropiedades propiedades;

    public JwtService(JwtPropiedades propiedades) {
        this.propiedades = propiedades;
    }

    public String extraerNombreUsuario(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public UUID extraerIdSesion(String token) {
        String identificador = extraerClaim(token, Claims::getId);
        if (identificador == null || identificador.isBlank()) {
            throw new IllegalArgumentException("El JWT no contiene identificador de sesion");
        }
        return UUID.fromString(identificador);
    }

    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodosLosClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generarToken(UserDetails userDetails, UUID sesionId) {
        return generarToken(new HashMap<>(), userDetails, sesionId);
    }

    public String generarToken(Map<String, Object> extraClaims, UserDetails userDetails, UUID sesionId) {
        return Jwts.builder()
                .claims(extraClaims)
                .id(sesionId.toString())
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + propiedades.getExpiracionMs()))
                .signWith(obtenerClaveFirma())
                .compact();
    }

    public boolean esTokenValido(String token, UserDetails userDetails) {
        final String nombreUsuario = extraerNombreUsuario(token);
        return (nombreUsuario.equals(userDetails.getUsername())) && !esTokenExpirado(token);
    }

    private boolean esTokenExpirado(String token) {
        return extraerExpiracion(token).before(new Date());
    }

    private Date extraerExpiracion(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClaveFirma())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey obtenerClaveFirma() {
        byte[] keyBytes = Decoders.BASE64.decode(propiedades.getSecreto());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
