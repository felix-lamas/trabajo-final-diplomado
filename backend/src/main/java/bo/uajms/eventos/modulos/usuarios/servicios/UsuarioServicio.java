package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.core.seguridad.RolSistema;
import bo.uajms.eventos.modulos.usuarios.dtos.ActualizarPerfilRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.CambioContrasenaRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.PerfilResponse;
import bo.uajms.eventos.modulos.usuarios.dtos.UsuarioDto;
import bo.uajms.eventos.modulos.usuarios.dtos.SolicitudOrganizadorResponse;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.entidades.Rol;
import bo.uajms.eventos.modulos.usuarios.entidades.UsuarioRol;
import bo.uajms.eventos.modulos.usuarios.mappers.UsuarioMapper;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRolRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioServicio {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    @Transactional(readOnly = true)
    public List<UsuarioDto> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(u -> usuarioMapper.aDto(u, obtenerRolesNombres(u.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDto buscarPorId(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return usuarioMapper.aDto(usuario, obtenerRolesNombres(id));
    }

    @Transactional(readOnly = true)
    public PerfilResponse obtenerPerfilActual() {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();
        return usuarioMapper.aPerfilResponse(usuario, obtenerRolesNombres(usuario.getId()));
    }

    @Transactional
    public PerfilResponse actualizarPerfil(ActualizarPerfilRequest request) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();

        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setCelular(request.getCelular());

        usuarioRepository.save(usuario);
        return usuarioMapper.aPerfilResponse(usuario, obtenerRolesNombres(usuario.getId()));
    }

    @Transactional
    public void cambiarContrasena(CambioContrasenaRequest request) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();

        if (!passwordEncoder.matches(request.getContrasenaActual(), usuario.getContrasena())) {
            throw new NegocioException("La contraseña actual es incorrecta");
        }

        if (!request.getNuevaContrasena().equals(request.getConfirmacion())) {
            throw new NegocioException("La nueva contraseña y su confirmación no coinciden");
        }

        usuario.setContrasena(passwordEncoder.encode(request.getNuevaContrasena()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    @PreAuthorize("hasRole('USUARIO')")
    public SolicitudOrganizadorResponse solicitarSerOrganizador() {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();
        if (!usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuario.getId(), "USUARIO")
                || usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuario.getId(), "ORGANIZADOR")
                || usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuario.getId(), "ADMINISTRADOR")) {
            throw new NegocioException("Solo un usuario puede solicitar convertirse en organizador");
        }
        if (usuario.getEstadoSolicitudOrganizador() == Usuario.EstadoSolicitudOrganizador.PENDIENTE) {
            throw new NegocioException("Ya existe una solicitud pendiente");
        }
        if (usuario.getEstadoSolicitudOrganizador() == Usuario.EstadoSolicitudOrganizador.APROBADA) {
            throw new NegocioException("La solicitud ya fue aprobada");
        }

        usuario.setEstadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.PENDIENTE);
        usuario.setFechaSolicitudOrganizador(LocalDateTime.now());
        usuario.setFechaResolucionOrganizador(null);
        usuario.setMotivoRechazoOrganizador(null);
        usuario.setSolicitudResueltaPor(null);
        return aSolicitudResponse(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<SolicitudOrganizadorResponse> listarSolicitudesOrganizador(
            Usuario.EstadoSolicitudOrganizador estado) {
        return usuarioRepository.findByEstadoSolicitudOrganizadorOrderByFechaSolicitudOrganizadorAsc(estado)
                .stream()
                .map(this::aSolicitudResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public SolicitudOrganizadorResponse aprobarSolicitudOrganizador(UUID usuarioId) {
        Usuario administrador = usuarioAutenticadoService.obtenerUsuario();
        if (administrador.getId().equals(usuarioId)) {
            throw new NegocioException("Un administrador no puede aprobar su propia solicitud");
        }

        Usuario solicitante = obtenerSolicitudPendiente(usuarioId);
        Rol rolOrganizador = rolRepository.findByNombre("ORGANIZADOR")
                .orElseThrow(() -> new NegocioException("Rol ORGANIZADOR no encontrado"));

        usuarioRolRepository.deleteByUsuarioId(solicitante.getId());
        usuarioRolRepository.save(UsuarioRol.builder().usuario(solicitante).rol(rolOrganizador).build());

        solicitante.setEstadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.APROBADA);
        solicitante.setFechaResolucionOrganizador(LocalDateTime.now());
        solicitante.setMotivoRechazoOrganizador(null);
        solicitante.setSolicitudResueltaPor(administrador);
        return aSolicitudResponse(usuarioRepository.save(solicitante));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public SolicitudOrganizadorResponse rechazarSolicitudOrganizador(UUID usuarioId, String motivo) {
        Usuario administrador = usuarioAutenticadoService.obtenerUsuario();
        if (administrador.getId().equals(usuarioId)) {
            throw new NegocioException("Un administrador no puede resolver su propia solicitud");
        }

        Usuario solicitante = obtenerSolicitudPendiente(usuarioId);
        solicitante.setEstadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.RECHAZADA);
        solicitante.setFechaResolucionOrganizador(LocalDateTime.now());
        solicitante.setMotivoRechazoOrganizador(motivo.trim());
        solicitante.setSolicitudResueltaPor(administrador);
        return aSolicitudResponse(usuarioRepository.save(solicitante));
    }

    private Usuario obtenerSolicitudPendiente(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", usuarioId));
        if (usuario.getEstadoSolicitudOrganizador() != Usuario.EstadoSolicitudOrganizador.PENDIENTE) {
            throw new NegocioException("El usuario no tiene una solicitud pendiente");
        }
        boolean esUsuario = usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuarioId, "USUARIO");
        boolean tienePrivilegios = usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuarioId, "ORGANIZADOR")
                || usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuarioId, "ADMINISTRADOR");
        if (!esUsuario || tienePrivilegios) {
            throw new NegocioException("La solicitud no pertenece a un usuario elegible");
        }
        return usuario;
    }

    private SolicitudOrganizadorResponse aSolicitudResponse(Usuario usuario) {
        return SolicitudOrganizadorResponse.builder()
                .usuarioId(usuario.getId())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .correoElectronico(usuario.getCorreoElectronico())
                .estado(usuario.getEstadoSolicitudOrganizador() == null
                        ? Usuario.EstadoSolicitudOrganizador.NINGUNA.name()
                        : usuario.getEstadoSolicitudOrganizador().name())
                .fechaSolicitud(usuario.getFechaSolicitudOrganizador())
                .fechaResolucion(usuario.getFechaResolucionOrganizador())
                .motivoRechazo(usuario.getMotivoRechazoOrganizador())
                .resueltaPorId(usuario.getSolicitudResueltaPor() == null
                        ? null : usuario.getSolicitudResueltaPor().getId())
                .build();
    }

    private List<String> obtenerRolesNombres(UUID usuarioId) {
        return usuarioRolRepository.findByUsuarioId(usuarioId).stream()
                .map(ur -> ur.getRol().getNombre())
                .filter(RolSistema::esOficial)
                .toList();
    }
}
