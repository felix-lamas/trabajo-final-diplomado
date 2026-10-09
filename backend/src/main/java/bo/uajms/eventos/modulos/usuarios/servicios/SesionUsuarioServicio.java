package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.seguridad.JwtPropiedades;
import bo.uajms.eventos.modulos.usuarios.entidades.SesionUsuario;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.repositorios.SesionUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SesionUsuarioServicio {

    private final SesionUsuarioRepository sesionRepository;
    private final JwtPropiedades jwtPropiedades;

    @Transactional
    public SesionUsuario crearSesionUnica(Usuario usuario) {
        revocarSesionesActivas(usuario.getId());
        LocalDateTime expiracion = LocalDateTime.now()
                .plusNanos(jwtPropiedades.getExpiracionMs() * 1_000_000L);
        return sesionRepository.saveAndFlush(SesionUsuario.builder()
                .usuario(usuario)
                .fechaExpiracion(expiracion)
                .build());
    }

    @Transactional(readOnly = true)
    public boolean esSesionActiva(UUID sesionId, String correoUsuario) {
        return sesionRepository.findById(sesionId)
                .filter(sesion -> sesion.getUsuario().getCorreoElectronico().equalsIgnoreCase(correoUsuario))
                .map(sesion -> sesion.getUsuario().isActivo() && sesion.estaActiva(LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional
    public void revocarSesion(UUID sesionId, String correoUsuario) {
        sesionRepository.findById(sesionId)
                .filter(sesion -> sesion.getUsuario().getCorreoElectronico().equalsIgnoreCase(correoUsuario))
                .filter(sesion -> sesion.getFechaRevocacion() == null)
                .ifPresent(sesion -> sesion.setFechaRevocacion(LocalDateTime.now()));
    }

    @Transactional
    public void revocarSesionesActivas(UUID usuarioId) {
        LocalDateTime ahora = LocalDateTime.now();
        sesionRepository.findByUsuarioIdAndFechaRevocacionIsNull(usuarioId)
                .forEach(sesion -> sesion.setFechaRevocacion(ahora));
    }
}
