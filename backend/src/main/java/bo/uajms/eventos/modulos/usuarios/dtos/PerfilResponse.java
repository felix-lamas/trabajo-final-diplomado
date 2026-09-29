package bo.uajms.eventos.modulos.usuarios.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PerfilResponse {
    private UUID id;
    private String nombres;
    private String apellidos;
    private String correoElectronico;
    private boolean correoVerificado;
    private String ci;
    private String ru;
    private String celular;
    private String tipoUsuario;
    private String estadoSolicitudOrganizador;
    private List<String> roles;
}
