package bo.uajms.eventos.modulos.usuarios.dtos;
import java.time.LocalDateTime;
import java.util.UUID;
public record InvitacionAdministradorResponse(UUID id, String correo, String nombres, String apellidos,
    String estado, LocalDateTime fechaCreacion, LocalDateTime fechaExpiracion, LocalDateTime fechaAceptacion) {}
