package bo.uajms.eventos.modulos.usuarios.controladores;

import bo.uajms.eventos.modulos.usuarios.dtos.ActualizarPerfilRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.CambioContrasenaRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.PerfilResponse;
import bo.uajms.eventos.modulos.usuarios.dtos.RechazarSolicitudOrganizadorRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.SolicitudOrganizadorResponse;
import bo.uajms.eventos.modulos.usuarios.dtos.UsuarioDto;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.servicios.UsuarioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Perfiles, administracion de usuarios y solicitudes de organizador")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar usuarios",
            description = "Devuelve todos los usuarios registrados. Operacion exclusiva de ADMINISTRADOR.")
    public ResponseEntity<List<UsuarioDto>> listarTodos() {
        return ResponseEntity.ok(usuarioServicio.listarTodos());
    }

    @GetMapping("/perfil")
    @Operation(summary = "Obtener mi perfil",
            description = "Devuelve exclusivamente el perfil asociado al JWT actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil autenticado", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "JWT o sesion no validos")
    })
    public ResponseEntity<PerfilResponse> obtenerPerfil() {
        return ResponseEntity.ok(usuarioServicio.obtenerPerfilActual());
    }

    @PutMapping("/perfil")
    @Operation(summary = "Actualizar mi perfil",
            description = "Actualiza nombres, apellidos y celular del propietario del JWT. No permite cambiar email, CI, RU, roles, contrasena, estado de organizador ni verificacion de correo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Datos de perfil invalidos"),
            @ApiResponse(responseCode = "401", description = "JWT o sesion no validos")
    })
    public ResponseEntity<PerfilResponse> actualizarPerfil(@Valid @RequestBody ActualizarPerfilRequest request) {
        return ResponseEntity.ok(usuarioServicio.actualizarPerfil(request));
    }

    @PostMapping("/cambiar-contrasena")
    @Operation(summary = "Cambiar mi contrasena",
            description = "Verifica la contrasena actual, exige una clave nueva segura y diferente, actualiza BCrypt y revoca todas las sesiones. El JWT actual deja de ser valido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrasena cambiada y sesiones revocadas"),
            @ApiResponse(responseCode = "400", description = "Contrasena actual incorrecta, nueva clave invalida, repetida o confirmacion diferente"),
            @ApiResponse(responseCode = "401", description = "JWT o sesion no validos")
    })
    public ResponseEntity<Void> cambiarContrasena(@Valid @RequestBody CambioContrasenaRequest request) {
        usuarioServicio.cambiarContrasena(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Obtener usuario por ID",
            description = "Consulta administrativa de un usuario. Operacion exclusiva de ADMINISTRADOR.")
    @ApiResponse(responseCode = "404", description = "Usuario inexistente")
    public ResponseEntity<UsuarioDto> buscarPorId(
            @Parameter(description = "Identificador del usuario") @PathVariable UUID id) {
        return ResponseEntity.ok(usuarioServicio.buscarPorId(id));
    }

    @PostMapping("/solicitud-organizador")
    @PreAuthorize("hasRole('USUARIO')")
    @Operation(summary = "Solicitar rol de organizador",
            description = "Crea una solicitud PENDIENTE exclusivamente para el propietario del JWT con rol USUARIO. Una solicitud RECHAZADA puede volver a presentarse sin periodo de espera.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud creada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Ya existe una solicitud pendiente o aprobada"),
            @ApiResponse(responseCode = "401", description = "JWT o sesion no validos"),
            @ApiResponse(responseCode = "403", description = "Requiere rol USUARIO")
    })
    public ResponseEntity<SolicitudOrganizadorResponse> solicitarSerOrganizador() {
        return ResponseEntity.ok(usuarioServicio.solicitarSerOrganizador());
    }

    @GetMapping("/solicitudes-organizador")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar solicitudes de organizador",
            description = "Consulta exclusiva de ADMINISTRADOR filtrada por NINGUNA, PENDIENTE, APROBADA o RECHAZADA. El valor predeterminado es PENDIENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitudes filtradas", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Estado de filtro invalido"),
            @ApiResponse(responseCode = "401", description = "JWT o sesion no validos"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMINISTRADOR")
    })
    public ResponseEntity<List<SolicitudOrganizadorResponse>> listarSolicitudesOrganizador(
            @Parameter(description = "Estado de la solicitud", example = "PENDIENTE")
            @RequestParam(defaultValue = "PENDIENTE") Usuario.EstadoSolicitudOrganizador estado) {
        return ResponseEntity.ok(usuarioServicio.listarSolicitudesOrganizador(estado));
    }

    @PatchMapping("/solicitudes-organizador/{usuarioId}/aprobar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Aprobar solicitud de organizador",
            description = "Transicion atomica exclusiva de ADMINISTRADOR. Conserva USUARIO y agrega ORGANIZADOR sin duplicarlo en una solicitud PENDIENTE y registra fecha y resolutor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud aprobada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "La solicitud no esta pendiente o el usuario no es elegible"),
            @ApiResponse(responseCode = "401", description = "JWT o sesion no validos"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMINISTRADOR"),
            @ApiResponse(responseCode = "404", description = "Usuario inexistente")
    })
    public ResponseEntity<SolicitudOrganizadorResponse> aprobarSolicitudOrganizador(
            @Parameter(description = "Identificador del usuario solicitante") @PathVariable UUID usuarioId) {
        return ResponseEntity.ok(usuarioServicio.aprobarSolicitudOrganizador(usuarioId));
    }

    @PatchMapping("/solicitudes-organizador/{usuarioId}/rechazar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Rechazar solicitud de organizador",
            description = "Transicion atomica exclusiva de ADMINISTRADOR. Rechaza una solicitud PENDIENTE, conserva el rol USUARIO y registra motivo, fecha y resolutor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud rechazada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Solicitud no pendiente o motivo invalido"),
            @ApiResponse(responseCode = "401", description = "JWT o sesion no validos"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMINISTRADOR"),
            @ApiResponse(responseCode = "404", description = "Usuario inexistente")
    })
    public ResponseEntity<SolicitudOrganizadorResponse> rechazarSolicitudOrganizador(
            @Parameter(description = "Identificador del usuario solicitante") @PathVariable UUID usuarioId,
            @Valid @RequestBody RechazarSolicitudOrganizadorRequest request) {
        return ResponseEntity.ok(usuarioServicio.rechazarSolicitudOrganizador(usuarioId, request.getMotivo()));
    }
}
