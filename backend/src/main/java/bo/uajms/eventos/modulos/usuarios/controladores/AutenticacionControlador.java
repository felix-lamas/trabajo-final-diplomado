package bo.uajms.eventos.modulos.usuarios.controladores;

import bo.uajms.eventos.modulos.usuarios.dtos.LoginRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.LoginResponse;
import bo.uajms.eventos.modulos.usuarios.dtos.RecuperarContrasenaRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.RegistroUsuarioRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.ResetContrasenaRequest;
import bo.uajms.eventos.modulos.usuarios.servicios.AutenticacionServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Endpoints para registro, login y recuperacion")
public class AutenticacionControlador {

    private final AutenticacionServicio autenticacionServicio;

    @PostMapping("/registro")
    @Operation(summary = "Registrar un nuevo usuario (Estudiante o Externo)")
    public ResponseEntity<LoginResponse> registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        return ResponseEntity.ok(autenticacionServicio.registrar(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(autenticacionServicio.login(request));
    }

    @PostMapping("/recuperar-contrasena")
    @Operation(
            summary = "Solicitar recuperacion de contrasena",
            description = "Genera un token unico que expira en 30 minutos y envia un enlace de restablecimiento al correo registrado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud procesada sin revelar si el correo existe"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos")
    })
    public ResponseEntity<Void> solicitarRecuperacion(@Valid @RequestBody RecuperarContrasenaRequest request) {
        autenticacionServicio.solicitarRecuperacion(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restablecer-contrasena")
    @Operation(
            summary = "Restablecer contrasena con token",
            description = "Valida que el token exista, no este expirado y no haya sido utilizado antes de actualizar la contrasena."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrasena restablecida correctamente"),
            @ApiResponse(responseCode = "400", description = "Token invalido, expirado, usado o contrasena insegura")
    })
    public ResponseEntity<Void> restablecerContrasena(@Valid @RequestBody ResetContrasenaRequest request) {
        autenticacionServicio.restablecerContrasena(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resetear-contrasena")
    @Operation(summary = "Alias legacy para restablecer contrasena con token")
    public ResponseEntity<Void> resetearContrasena(@Valid @RequestBody ResetContrasenaRequest request) {
        autenticacionServicio.restablecerContrasena(request);
        return ResponseEntity.ok().build();
    }
}
