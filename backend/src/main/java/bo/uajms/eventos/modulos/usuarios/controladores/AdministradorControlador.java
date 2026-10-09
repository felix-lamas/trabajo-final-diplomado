package bo.uajms.eventos.modulos.usuarios.controladores;
import bo.uajms.eventos.modulos.usuarios.dtos.*;
import bo.uajms.eventos.modulos.usuarios.servicios.AdministradorServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/administradores")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@SecurityRequirement(name="bearerAuth")
@Tag(name="Administradores", description="Gestion exclusiva de ADMINISTRADOR. No convierte cuentas existentes ni modifica roles de participacion.")
@ApiResponses({@ApiResponse(responseCode="401", description="Autenticacion requerida"),
    @ApiResponse(responseCode="403", description="Requiere ADMINISTRADOR"),
    @ApiResponse(responseCode="400", description="Validacion funcional, duplicado o ultimo administrador"),
    @ApiResponse(responseCode="404", description="Recurso inexistente o no administrativo"),
    @ApiResponse(responseCode="503", description="Correo no disponible; la operacion no se confirma")})
public class AdministradorControlador {
    private final AdministradorServicio servicio;
    @GetMapping @Operation(summary="Listar administradores y estado activo")
    public List<AdministradorResponse> listar() { return servicio.listar(); }
    @GetMapping("/invitaciones") @Operation(summary="Listar invitaciones", description="Incluye estado efectivo PENDIENTE, ACEPTADA, EXPIRADA o REVOCADA; nunca incluye tokens.")
    public List<InvitacionAdministradorResponse> invitaciones() { return servicio.listarInvitaciones(); }
    @PostMapping("/invitaciones") @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary="Invitar administrador", description="Correo y CI nuevos; token de un uso con expiracion configurable, por defecto 24 horas. No acepta roles.")
    public InvitacionAdministradorResponse invitar(@Valid @RequestBody InvitarAdministradorRequest r) { return servicio.invitar(r); }
    @PostMapping("/invitaciones/{id}/reenviar") @Operation(summary="Renovar invitacion", description="PENDIENTE o EXPIRADA; invalida el enlace anterior y aplica espera configurable de 5 minutos.")
    public InvitacionAdministradorResponse reenviar(@PathVariable UUID id) { return servicio.reenviar(id); }
    @PatchMapping("/invitaciones/{id}/revocar") @Operation(summary="Revocar invitacion pendiente")
    public InvitacionAdministradorResponse revocar(@PathVariable UUID id) { return servicio.revocar(id); }
    @PatchMapping("/{id}/desactivar") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary="Desactivar administrador", description="Revoca sesiones. No permite desactivar el ultimo administrador activo ni la cuenta demo protegida.")
    public void desactivar(@PathVariable UUID id) { servicio.desactivar(id); }
}
