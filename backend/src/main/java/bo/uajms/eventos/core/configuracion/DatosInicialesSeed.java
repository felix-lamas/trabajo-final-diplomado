package bo.uajms.eventos.core.configuracion;

import bo.uajms.eventos.modulos.usuarios.entidades.Permiso;
import bo.uajms.eventos.modulos.usuarios.entidades.Rol;
import bo.uajms.eventos.modulos.usuarios.entidades.RolPermiso;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.entidades.UsuarioRol;
import bo.uajms.eventos.modulos.usuarios.repositorios.PermisoRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolPermisoRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRolRepository;
import bo.uajms.eventos.modulos.categorias.entidades.CategoriaEvento;
import bo.uajms.eventos.modulos.categorias.repositorios.CategoriaEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Profile("demo")
@RequiredArgsConstructor
public class DatosInicialesSeed implements CommandLineRunner {

    @Value("${app.seed.demo-password}")
    private String demoPassword;

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RolPermisoRepository rolPermisoRepository;
    private final CategoriaEventoRepository categoriaEventoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Map<String, Rol> roles = cargarSeguridad();
        cargarCategoriasDemo();
        crearUsuarioDemo("admin@demo.local", "DEMO-ADMIN", "RU-DEMO-ADMIN", "Administrador", "Demo",
                Usuario.TipoUsuario.INTERNO, roles.get("ADMINISTRADOR"));
        crearUsuarioDemo("organizador@demo.local", "DEMO-ORGANIZADOR", "RU-DEMO-ORG", "Organizador", "Demo",
                Usuario.TipoUsuario.INTERNO, roles.get("ORGANIZADOR"));
        crearUsuarioDemo("usuario@demo.local", "DEMO-USUARIO", null, "Usuario", "Demo",
                Usuario.TipoUsuario.EXTERNO, roles.get("USUARIO"));
    }

    private void cargarCategoriasDemo() {
        crearCategoriaDemo("Conferencia", "Conferencias ficticias para demostracion");
        crearCategoriaDemo("Taller", "Talleres ficticios para demostracion");
        crearCategoriaDemo("Curso", "Cursos ficticios para demostracion");
        crearCategoriaDemo("Seminario", "Seminarios ficticios para demostracion");
    }

    private void crearCategoriaDemo(String nombre, String descripcion) {
        if (!categoriaEventoRepository.existsByNombreIgnoreCase(nombre)) {
            categoriaEventoRepository.save(CategoriaEvento.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .estado("ACTIVO")
                    .build());
        }
    }

    private Map<String, Rol> cargarSeguridad() {
        Map<String, Permiso> permisos = new LinkedHashMap<>();
        permisos.put("usuarios:gestionar", crearPermiso("usuarios:gestionar", "Gestion de usuarios"));
        permisos.put("eventos:gestionar", crearPermiso("eventos:gestionar", "Gestion de eventos"));
        permisos.put("pagos:validar", crearPermiso("pagos:validar", "Validacion de pagos"));
        permisos.put("asistencias:registrar", crearPermiso("asistencias:registrar", "Registro de asistencias"));
        permisos.put("certificados:emitir", crearPermiso("certificados:emitir", "Emision de certificados"));

        Map<String, Rol> roles = new LinkedHashMap<>();
        roles.put("ADMINISTRADOR", crearRol("ADMINISTRADOR", "Acceso administrativo global"));
        roles.put("ORGANIZADOR", crearRol("ORGANIZADOR", "Gestiona sus propios eventos"));
        roles.put("USUARIO", crearRol("USUARIO", "Participa en eventos"));

        permisos.values().forEach(permiso -> asignarPermiso(roles.get("ADMINISTRADOR"), permiso));
        asignarPermiso(roles.get("ORGANIZADOR"), permisos.get("eventos:gestionar"));
        asignarPermiso(roles.get("ORGANIZADOR"), permisos.get("pagos:validar"));
        asignarPermiso(roles.get("ORGANIZADOR"), permisos.get("asistencias:registrar"));
        asignarPermiso(roles.get("ORGANIZADOR"), permisos.get("certificados:emitir"));
        return roles;
    }

    private Rol crearRol(String nombre, String descripcion) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre(nombre).descripcion(descripcion).build()));
    }

    private Permiso crearPermiso(String nombre, String descripcion) {
        return permisoRepository.findByNombre(nombre)
                .orElseGet(() -> permisoRepository.save(Permiso.builder().nombre(nombre).descripcion(descripcion).build()));
    }

    private void asignarPermiso(Rol rol, Permiso permiso) {
        boolean asignado = rolPermisoRepository.findByRolId(rol.getId()).stream()
                .anyMatch(relacion -> relacion.getPermiso().getId().equals(permiso.getId()));
        if (!asignado) {
            rolPermisoRepository.save(RolPermiso.builder().rol(rol).permiso(permiso).build());
        }
    }

    private void crearUsuarioDemo(String correo, String ci, String ru, String nombres, String apellidos,
                                  Usuario.TipoUsuario tipoUsuario, Rol rol) {
        Usuario usuario = usuarioRepository.findByCorreoElectronico(correo).orElseGet(() ->
                usuarioRepository.save(Usuario.builder()
                        .correoElectronico(correo)
                        .contrasena(passwordEncoder.encode(demoPassword))
                        .nombres(nombres)
                        .apellidos(apellidos)
                        .ci(ci)
                        .ru(ru)
                        .celular("70000000")
                        .tipoUsuario(tipoUsuario)
                        .build()));
        boolean asignado = usuarioRolRepository.findByUsuarioId(usuario.getId()).stream()
                .anyMatch(relacion -> relacion.getRol().getId().equals(rol.getId()));
        if (!asignado) {
            usuarioRolRepository.save(UsuarioRol.builder().usuario(usuario).rol(rol).build());
        }
    }
}
