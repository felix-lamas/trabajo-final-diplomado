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
    public ResponseEntity<PerfilResponse> obtenerPerfil() {
        return ResponseEntity.ok(usuarioServicio.obtenerPerfilActual());
    }

    @PutMapping("/perfil")
    @Operation(summary = "Actualizar mi perfil",
            description = "Actualiza los datos editables del propietario del JWT; no permite cambiar roles.")
    public ResponseEntity<PerfilResponse> actualizarPerfil(@Valid @RequestBody ActualizarPerfilRequest request) {
        return ResponseEntity.ok(usuarioServicio.actualizarPerfil(request));
    }

    @PostMapping("/cambiar-contrasena")
    @Operation(summary = "Cambiar mi contrasena",
            description = "Verifica la contrasena actual y cambia la clave del propietario del JWT. El contrato vigente utiliza POST.")
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
            description = "Crea una solicitud PENDIENTE para el usuario autenticado con rol USUARIO.")
    @ApiResponse(responseCode = "400", description = "Ya existe una solicitud pendiente o aprobada")
    public ResponseEntity<SolicitudOrganizadorResponse> solicitarSerOrganizador() {
        return ResponseEntity.ok(usuarioServicio.solicitarSerOrganizador());
    }

    @GetMapping("/solicitudes-organizador")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar solicitudes de organizador",
            description = "Consulta administrativa filtrada por estado de solicitud.")
    public ResponseEntity<List<SolicitudOrganizadorResponse>> listarSolicitudesOrganizador(
            @Parameter(description = "Estado de la solicitud", example = "PENDIENTE")
            @RequestParam(defaultValue = "PENDIENTE") Usuario.EstadoSolicitudOrganizador estado) {
        return ResponseEntity.ok(usuarioServicio.listarSolicitudesOrganizador(estado));
    }

    @PatchMapping("/solicitudes-organizador/{usuarioId}/aprobar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Aprobar solicitud de organizador",
            description = "Convierte al solicitante elegible de USUARIO a ORGANIZADOR. Operacion exclusiva de ADMINISTRADOR.")
    @ApiResponse(responseCode = "404", description = "Usuario o solicitud pendiente inexistente")
    public ResponseEntity<SolicitudOrganizadorResponse> aprobarSolicitudOrganizador(
            @Parameter(description = "Identificador del usuario solicitante") @PathVariable UUID usuarioId) {
        return ResponseEntity.ok(usuarioServicio.aprobarSolicitudOrganizador(usuarioId));
    }

    @PatchMapping("/solicitudes-organizador/{usuarioId}/rechazar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Rechazar solicitud de organizador",
            description = "Rechaza una solicitud PENDIENTE con motivo. Operacion exclusiva de ADMINISTRADOR.")
    @ApiResponse(responseCode = "404", description = "Usuario o solicitud pendiente inexistente")
    public ResponseEntity<SolicitudOrganizadorResponse> rechazarSolicitudOrganizador(
            @Parameter(description = "Identificador del usuario solicitante") @PathVariable UUID usuarioId,
            @Valid @RequestBody RechazarSolicitudOrganizadorRequest request) {
        return ResponseEntity.ok(usuarioServicio.rechazarSolicitudOrganizador(usuarioId, request.getMotivo()));
    }
}
