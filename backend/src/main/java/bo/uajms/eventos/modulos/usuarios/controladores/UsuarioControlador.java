package bo.uajms.eventos.modulos.usuarios.controladores;

import bo.uajms.eventos.modulos.usuarios.dtos.ActualizarPerfilRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.CambioContrasenaRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.PerfilResponse;
import bo.uajms.eventos.modulos.usuarios.dtos.UsuarioDto;
import bo.uajms.eventos.modulos.usuarios.dtos.RechazarSolicitudOrganizadorRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.SolicitudOrganizadorResponse;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.servicios.UsuarioServicio;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Usuarios", description = "Gestión de usuarios y perfiles")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar todos los usuarios (Solo ADMIN)")
    public ResponseEntity<List<UsuarioDto>> listarTodos() {
        return ResponseEntity.ok(usuarioServicio.listarTodos());
    }

    @GetMapping("/perfil")
    @Operation(summary = "Obtener perfil del usuario autenticado")
    public ResponseEntity<PerfilResponse> obtenerPerfil() {
        return ResponseEntity.ok(usuarioServicio.obtenerPerfilActual());
    }

    @PutMapping("/perfil")
    @Operation(summary = "Actualizar perfil del usuario autenticado")
    public ResponseEntity<PerfilResponse> actualizarPerfil(@Valid @RequestBody ActualizarPerfilRequest request) {
        return ResponseEntity.ok(usuarioServicio.actualizarPerfil(request));
    }

    @PostMapping("/cambiar-contrasena")
    @Operation(summary = "Cambiar contraseña del usuario autenticado")
    public ResponseEntity<Void> cambiarContrasena(@Valid @RequestBody CambioContrasenaRequest request) {
        usuarioServicio.cambiarContrasena(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Obtener detalles de un usuario por ID (Solo ADMIN)")
    public ResponseEntity<UsuarioDto> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioServicio.buscarPorId(id));
    }

    @PostMapping("/solicitud-organizador")
    @PreAuthorize("hasRole('USUARIO')")
    @Operation(summary = "Solicitar el rol de organizador")
    public ResponseEntity<SolicitudOrganizadorResponse> solicitarSerOrganizador() {
        return ResponseEntity.ok(usuarioServicio.solicitarSerOrganizador());
    }

    @GetMapping("/solicitudes-organizador")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Consultar solicitudes de organizador")
    public ResponseEntity<List<SolicitudOrganizadorResponse>> listarSolicitudesOrganizador(
            @RequestParam(defaultValue = "PENDIENTE") Usuario.EstadoSolicitudOrganizador estado) {
        return ResponseEntity.ok(usuarioServicio.listarSolicitudesOrganizador(estado));
    }

    @PatchMapping("/solicitudes-organizador/{usuarioId}/aprobar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Aprobar una solicitud de organizador")
    public ResponseEntity<SolicitudOrganizadorResponse> aprobarSolicitudOrganizador(
            @PathVariable UUID usuarioId) {
        return ResponseEntity.ok(usuarioServicio.aprobarSolicitudOrganizador(usuarioId));
    }

    @PatchMapping("/solicitudes-organizador/{usuarioId}/rechazar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Rechazar una solicitud de organizador")
    public ResponseEntity<SolicitudOrganizadorResponse> rechazarSolicitudOrganizador(
            @PathVariable UUID usuarioId,
            @Valid @RequestBody RechazarSolicitudOrganizadorRequest request) {
        return ResponseEntity.ok(usuarioServicio.rechazarSolicitudOrganizador(usuarioId, request.getMotivo()));
    }
}
