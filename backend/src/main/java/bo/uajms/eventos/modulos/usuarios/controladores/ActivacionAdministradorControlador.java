package bo.uajms.eventos.modulos.usuarios.controladores;
import bo.uajms.eventos.modulos.usuarios.dtos.*;
import bo.uajms.eventos.modulos.usuarios.servicios.AdministradorServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/auth/invitaciones-administrador")
@io.swagger.v3.oas.annotations.security.SecurityRequirements
@Tag(name="Activacion administrativa", description="Publico controlado por token secreto. No permite elegir roles ni promover cuentas existentes.")
public class ActivacionAdministradorControlador {
    private final AdministradorServicio servicio;
    @PostMapping("/consultar")
    @Operation(summary="Consultar estado de una invitacion", description="Token en body para evitar URL de API/logs. Solo devuelve estado, sin datos personales. No consume el token.")
    @ApiResponses({@ApiResponse(responseCode="200", description="PENDIENTE, EXPIRADA, REVOCADA o ACEPTADA"), @ApiResponse(responseCode="400", description="Token invalido")})
    public ResponseEntity<Map<String,String>> consultar(@Valid @RequestBody ConsultarInvitacionAdministradorRequest r) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("Referrer-Policy", "no-referrer").body(servicio.consultar(r.getToken()));
    }
    @PostMapping("/aceptar")
    @Operation(summary="Activar cuenta administrativa", description="Token pendiente y vigente de un solo uso; confirma datos y contrasena segura. Crea cuenta activa, correo verificado y SOLO ADMINISTRADOR.")
    @ApiResponses({@ApiResponse(responseCode="204", description="Cuenta creada; iniciar sesion mediante login existente"), @ApiResponse(responseCode="400", description="Token invalido, expirado, revocado, utilizado o datos invalidos")})
    public ResponseEntity<Void> aceptar(@Valid @RequestBody AceptarInvitacionAdministradorRequest r) {
        servicio.aceptar(r);
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}
