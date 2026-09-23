package bo.uajms.eventos.core.seguridad;

import bo.uajms.eventos.core.excepciones.SeguridadException;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioAutenticadoService {

    private static final String PREFIJO_ROL = "ROLE_";

    private final UsuarioRepository usuarioRepository;

    public boolean tieneRol(String rol) {
        if (!RolSistema.esOficial(rol)) {
            return false;
        }
        Authentication authentication = obtenerAutenticacion();
        if (!esAutenticacionValida(authentication)) {
            return false;
        }

        String autoridadEsperada = PREFIJO_ROL + rol;
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> autoridadEsperada.equals(authority.getAuthority()));
    }

    public Usuario obtenerUsuario() {
        Authentication authentication = obtenerAutenticacion();
        if (!esAutenticacionValida(authentication)) {
            throw new SeguridadException("Se requiere un usuario autenticado");
        }

        return usuarioRepository.findByCorreoElectronicoIgnoreCase(authentication.getName())
                .orElseThrow(() -> new SeguridadException("Usuario autenticado no encontrado"));
    }

    private Authentication obtenerAutenticacion() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean esAutenticacionValida(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}
