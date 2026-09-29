package bo.uajms.eventos.core.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import bo.uajms.eventos.core.excepciones.CodigosError;
import io.jsonwebtoken.JwtException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import bo.uajms.eventos.modulos.usuarios.servicios.SesionUsuarioServicio;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String SESION_ID_ATTRIBUTE = "vidia.sesionId";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final SecurityErrorResponseWriter errorResponseWriter;
    private final SesionUsuarioServicio sesionUsuarioServicio;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String correoUsuario;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            correoUsuario = jwtService.extraerNombreUsuario(jwt);
            UUID sesionId = jwtService.extraerIdSesion(jwt);

            if (correoUsuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(correoUsuario);
                if (jwtService.esTokenValido(jwt, userDetails)
                        && sesionUsuarioServicio.esSesionActiva(sesionId, correoUsuario)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    request.setAttribute(SESION_ID_ATTRIBUTE, sesionId);
                } else {
                    errorResponseWriter.escribir(request, response, HttpServletResponse.SC_UNAUTHORIZED,
                            CodigosError.AUTH_INVALID_SESSION, "La sesion no existe, expiro o fue revocada");
                    return;
                }
            }
        } catch (JwtException | IllegalArgumentException | org.springframework.security.core.AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            errorResponseWriter.escribir(request, response, HttpServletResponse.SC_UNAUTHORIZED,
                    CodigosError.AUTH_INVALID_TOKEN, "El token de autenticacion no es valido");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
