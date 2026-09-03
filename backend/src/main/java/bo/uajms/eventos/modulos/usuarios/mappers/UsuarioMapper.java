package bo.uajms.eventos.modulos.usuarios.mappers;

import bo.uajms.eventos.modulos.usuarios.dtos.PerfilResponse;
import bo.uajms.eventos.modulos.usuarios.dtos.RegistroUsuarioRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.UsuarioDto;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class UsuarioMapper {

    public Usuario deRegistroRequest(RegistroUsuarioRequest dto) {
        if (dto == null) return null;
        return Usuario.builder()
                .nombres(dto.getNombres())
                .apellidos(dto.getApellidos())
                .correoElectronico(dto.getCorreoElectronico())
                .contrasena(dto.getContrasena())
                .ci(dto.getCi())
                .celular(dto.getCelular())
                .tipoUsuario(dto.getTipoUsuario())
                .build();
    }

    public UsuarioDto aDto(Usuario usuario, List<String> roles) {
        if (usuario == null) return null;
        UsuarioDto dto = new UsuarioDto();
        dto.setId(usuario.getId());
        dto.setNombres(usuario.getNombres());
        dto.setApellidos(usuario.getApellidos());
        dto.setCorreoElectronico(usuario.getCorreoElectronico());
        dto.setCi(usuario.getCi());
        dto.setTipoUsuario(usuario.getTipoUsuario().name());
        dto.setRoles(roles);
        return dto;
    }

    public PerfilResponse aPerfilResponse(Usuario usuario, List<String> roles) {
        if (usuario == null) return null;
        return PerfilResponse.builder()
                .id(usuario.getId())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .correoElectronico(usuario.getCorreoElectronico())
                .ci(usuario.getCi())
                .celular(usuario.getCelular())
                .tipoUsuario(usuario.getTipoUsuario().name())
                .roles(roles)
                .build();
    }
}
