package bo.uajms.eventos.modulos.usuarios.dtos;
import java.time.LocalDateTime;
import java.util.UUID;
public record AdministradorResponse(UUID id, String nombres, String apellidos, String correo,
    boolean activo, LocalDateTime fechaCreacion) {}
