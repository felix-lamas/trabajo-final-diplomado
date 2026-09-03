package bo.uajms.eventos.modulos.usuarios.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRespuestaDto {
    private String token;
    private UsuarioDto usuario;
}
