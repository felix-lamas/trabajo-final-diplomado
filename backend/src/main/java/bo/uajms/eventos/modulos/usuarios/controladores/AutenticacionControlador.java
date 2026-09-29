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
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Registro, login y recuperacion de acceso")
@SecurityRequirements
public class AutenticacionControlador {

    private final AutenticacionServicio autenticacionServicio;

    @PostMapping("/registro")
    @Operation(summary = "Registrar usuario",
            description = "Crea una cuenta UAJMS o externa con rol inicial USUARIO y devuelve el contrato de autenticacion vigente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario registrado", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Correo, CI o RU ya registrado o datos invalidos")
    })
    public ResponseEntity<LoginResponse> registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        return ResponseEntity.ok(autenticacionServicio.registrar(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion",
            description = "Valida correo y contrasena y devuelve un JWT con los roles oficiales del usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticacion correcta", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(autenticacionServicio.login(request));
    }

    @PostMapping("/recuperar-contrasena")
    @Operation(summary = "Solicitar recuperacion de contrasena",
            description = "Genera un token unico que expira en 30 minutos y envia un enlace al correo registrado. La respuesta no revela si el correo existe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud procesada sin revelar si el correo existe"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos")
    })
    public ResponseEntity<Void> solicitarRecuperacion(@Valid @RequestBody RecuperarContrasenaRequest request) {
        autenticacionServicio.solicitarRecuperacion(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restablecer-contrasena")
    @Operation(summary = "Restablecer contrasena con token",
            description = "Valida que el token exista, no este expirado y no haya sido utilizado antes de actualizar la contrasena.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrasena restablecida correctamente"),
            @ApiResponse(responseCode = "400", description = "Token invalido, expirado, usado o contrasena insegura")
    })
    public ResponseEntity<Void> restablecerContrasena(@Valid @RequestBody ResetContrasenaRequest request) {
        autenticacionServicio.restablecerContrasena(request);
        return ResponseEntity.ok().build();
    }
}
