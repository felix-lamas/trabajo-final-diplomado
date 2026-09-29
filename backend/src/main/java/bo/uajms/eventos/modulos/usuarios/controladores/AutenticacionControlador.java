package bo.uajms.eventos.modulos.usuarios.controladores;

import bo.uajms.eventos.modulos.usuarios.dtos.LoginRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.LoginResponse;
import bo.uajms.eventos.modulos.usuarios.dtos.RegistroResponse;
import bo.uajms.eventos.modulos.usuarios.dtos.ReenviarVerificacionRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.VerificarCorreoRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.RecuperarContrasenaRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.RegistroUsuarioRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.ResetContrasenaRequest;
import bo.uajms.eventos.modulos.usuarios.servicios.AutenticacionServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.security.core.Authentication;
import bo.uajms.eventos.core.seguridad.JwtAuthenticationFilter;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Registro, login y recuperacion de acceso")
public class AutenticacionControlador {

    private final AutenticacionServicio autenticacionServicio;

    @PostMapping("/registro")
    @SecurityRequirements
    @Operation(summary = "Registrar usuario",
            description = "Crea una cuenta UAJMS o externa no verificada con rol inicial USUARIO. No emite JWT hasta verificar el correo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario registrado", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Correo, CI o RU ya registrado o datos invalidos"),
            @ApiResponse(responseCode = "503", description = "No fue posible entregar el correo de verificacion")
    })
    public ResponseEntity<RegistroResponse> registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        return ResponseEntity.ok(autenticacionServicio.registrar(request));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesion",
            description = "Valida una cuenta con correo verificado, revoca su sesion anterior y devuelve un JWT asociado a la nueva sesion.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticacion correcta", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "El correo todavia no fue verificado"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(autenticacionServicio.login(request));
    }

    @PostMapping("/recuperar-contrasena")
    @SecurityRequirements
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
    @SecurityRequirements
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

    @PostMapping("/verificar-correo")
    @SecurityRequirements
    @Operation(summary = "Verificar correo",
            description = "Consume un token aleatorio de un solo uso, almacenado como hash y con vigencia de 24 horas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correo verificado"),
            @ApiResponse(responseCode = "400", description = "Token invalido, expirado o utilizado")
    })
    public ResponseEntity<Void> verificarCorreo(@Valid @RequestBody VerificarCorreoRequest request) {
        autenticacionServicio.verificarCorreo(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reenviar-verificacion")
    @SecurityRequirements
    @Operation(summary = "Reenviar verificacion de correo",
            description = "Procesa la solicitud sin revelar si el correo existe o ya esta verificado. Invalida tokens anteriores cuando corresponde.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud procesada de forma no enumerativa")
    })
    public ResponseEntity<Void> reenviarVerificacion(@Valid @RequestBody ReenviarVerificacionRequest request) {
        autenticacionServicio.reenviarVerificacion(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cerrar sesion",
            description = "Revoca la sesion identificada por el JWT actual. El cliente debe eliminar el token localmente.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sesion revocada"),
            @ApiResponse(responseCode = "401", description = "JWT o sesion no validos")
    })
    public ResponseEntity<Void> logout(
            @RequestAttribute(JwtAuthenticationFilter.SESION_ID_ATTRIBUTE) UUID sesionId,
            Authentication authentication) {
        autenticacionServicio.logout(sesionId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
