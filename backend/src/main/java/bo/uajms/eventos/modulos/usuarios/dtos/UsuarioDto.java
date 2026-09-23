package bo.uajms.eventos.modulos.usuarios.dtos;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class UsuarioDto {
    private UUID id;
    private String nombres;
    private String apellidos;
    private String correoElectronico;
    private String ci;
    private String ru;
    private String celular;
    private String tipoUsuario;
    private String estadoSolicitudOrganizador;
    private List<String> roles;
}
