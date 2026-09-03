package bo.uajms.eventos.core.seguridad;

import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRolRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolPermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DetallesUsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RolPermisoRepository rolPermisoRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreoElectronico(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Obtener roles del usuario
        usuarioRolRepository.findByUsuarioId(usuario.getId()).forEach(ur -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + ur.getRol().getNombre()));
            
            // Obtener permisos del rol
            rolPermisoRepository.findByRolId(ur.getRol().getId()).forEach(rp -> {
                authorities.add(new SimpleGrantedAuthority(rp.getPermiso().getNombre()));
            });
        });

        return new User(
                usuario.getCorreoElectronico(),
                usuario.getContrasena(),
                authorities
        );
    }
}
