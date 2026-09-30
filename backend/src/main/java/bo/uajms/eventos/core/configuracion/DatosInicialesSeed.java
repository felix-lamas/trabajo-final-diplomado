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
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.Modalidad;
import bo.uajms.eventos.modulos.eventos.entidades.PublicoObjetivo;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.entidades.TipoCertificadoEvento;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Profile("demo")
@RequiredArgsConstructor
public class DatosInicialesSeed implements CommandLineRunner {

    private static final ZoneId ZONA_OFICIAL = ZoneId.of("America/La_Paz");

    @Value("${app.seed.demo-password}")
    private String demoPassword;

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RolPermisoRepository rolPermisoRepository;
    private final CategoriaEventoRepository categoriaEventoRepository;
    private final EventoRepository eventoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Map<String, Rol> roles = cargarSeguridad();
        cargarCategoriasDemo();
        Usuario administrador = crearUsuarioDemo("admin@demo.local", "DEMO-ADMIN", "RU-DEMO-ADMIN", "Administrador", "Demo",
                Usuario.TipoUsuario.INTERNO, roles.get("ADMINISTRADOR"));

        Usuario organizadorAprobado = crearUsuarioDemo(
                "organizador1@demo.local", "DEMO-ORGANIZADOR-1", "RU-DEMO-ORG-1", "Organizador", "Demo Uno",
                Usuario.TipoUsuario.INTERNO, roles.get("ORGANIZADOR"));
        configurarSolicitudOrganizadorDemo(organizadorAprobado,
                Usuario.EstadoSolicitudOrganizador.APROBADA, administrador);

        Usuario organizadorPendiente = crearUsuarioDemo(
                "organizador2@demo.local", "DEMO-ORGANIZADOR-2", "RU-DEMO-ORG-2", "Organizador", "Demo Dos",
                Usuario.TipoUsuario.INTERNO, roles.get("USUARIO"));
        configurarSolicitudOrganizadorDemo(organizadorPendiente,
                Usuario.EstadoSolicitudOrganizador.PENDIENTE, null);

        Usuario participante = crearUsuarioDemo("usuario@demo.local", "DEMO-USUARIO", null, "Usuario", "Demo",
                Usuario.TipoUsuario.EXTERNO, roles.get("USUARIO"));
        configurarSolicitudOrganizadorDemo(participante,
                Usuario.EstadoSolicitudOrganizador.NINGUNA, null);

        cargarEventosDemo(organizadorAprobado, administrador);
    }

    private void cargarCategoriasDemo() {
        crearCategoriaDemo("Conferencia", "Conferencias ficticias para demostracion");
        crearCategoriaDemo("Taller", "Talleres ficticios para demostracion");
        crearCategoriaDemo("Curso", "Cursos ficticios para demostracion");
        crearCategoriaDemo("Seminario", "Seminarios ficticios para demostracion");
        crearCategoriaDemo("Jornada", "Jornadas ficticias para demostracion");
        crearCategoriaDemo("Cultural", "Actividades culturales ficticias para demostracion");
        crearCategoriaDemo("Deportivo", "Actividades deportivas ficticias para demostracion");
    }

    private void crearCategoriaDemo(String nombre, String descripcion) {
        if (!categoriaEventoRepository.existsByNombreNormalizado(nombre.trim().toLowerCase(Locale.ROOT))) {
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

    private Usuario crearUsuarioDemo(String correo, String ci, String ru, String nombres, String apellidos,
                                     Usuario.TipoUsuario tipoUsuario, Rol rol) {
        Usuario usuario = usuarioRepository.findByCorreoElectronico(correo)
                .map(existente -> {
                    existente.setContrasena(passwordEncoder.encode(demoPassword));
                    existente.setCorreoVerificado(true);
                    return usuarioRepository.save(existente);
                })
                .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                        .correoElectronico(correo)
                        .contrasena(passwordEncoder.encode(demoPassword))
                        .nombres(nombres)
                        .apellidos(apellidos)
                        .ci(ci)
                        .ru(ru)
                        .celular("70000000")
                        .correoVerificado(true)
                        .tipoUsuario(tipoUsuario)
                        .build()));
        var rolesActuales = usuarioRolRepository.findByUsuarioId(usuario.getId());
        boolean asignacionExacta = rolesActuales.size() == 1
                && rolesActuales.getFirst().getRol().getId().equals(rol.getId());
        if (!asignacionExacta) {
            usuarioRolRepository.deleteByUsuarioId(usuario.getId());
            usuarioRolRepository.save(UsuarioRol.builder().usuario(usuario).rol(rol).build());
        }
        return usuario;
    }

    private void configurarSolicitudOrganizadorDemo(Usuario usuario,
                                                     Usuario.EstadoSolicitudOrganizador estado,
                                                     Usuario administrador) {
        usuario.setEstadoSolicitudOrganizador(estado);
        usuario.setMotivoRechazoOrganizador(null);

        if (estado == Usuario.EstadoSolicitudOrganizador.APROBADA) {
            LocalDateTime fechaSolicitud = LocalDateTime.now();
            usuario.setFechaSolicitudOrganizador(fechaSolicitud);
            usuario.setFechaResolucionOrganizador(LocalDateTime.now());
            usuario.setSolicitudResueltaPor(administrador);
        } else if (estado == Usuario.EstadoSolicitudOrganizador.PENDIENTE) {
            usuario.setFechaSolicitudOrganizador(LocalDateTime.now());
            usuario.setFechaResolucionOrganizador(null);
            usuario.setSolicitudResueltaPor(null);
        } else {
            usuario.setFechaSolicitudOrganizador(null);
            usuario.setFechaResolucionOrganizador(null);
            usuario.setSolicitudResueltaPor(null);
        }

        usuarioRepository.save(usuario);
    }

    private void cargarEventosDemo(Usuario organizador, Usuario administrador) {
        Map<String, CategoriaEvento> categorias = categoriaEventoRepository.findAll().stream()
                .filter(categoria -> "ACTIVO".equalsIgnoreCase(categoria.getEstado()))
                .collect(Collectors.toMap(categoria -> categoria.getNombre().trim().toLowerCase(Locale.ROOT),
                        Function.identity(), (primera, segunda) -> primera));
        Set<String> titulosExistentes = eventoRepository.findAll().stream()
                .map(Evento::getTitulo)
                .filter(titulo -> titulo != null)
                .map(titulo -> titulo.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        LocalDate fechaBase = LocalDate.now(ZONA_OFICIAL).plusDays(30);

        crearEventoDemoSiNoExiste(titulosExistentes, organizador, administrador,
                categorias.get("conferencia"), "Congreso de Innovación Tecnológica UAJMS",
                EstadoEvento.PUBLICADO, Modalidad.PRESENCIAL, fechaBase);
        crearEventoDemoSiNoExiste(titulosExistentes, organizador, administrador,
                categorias.get("taller"), "Taller de Desarrollo Web",
                EstadoEvento.PUBLICADO, Modalidad.VIRTUAL, fechaBase.plusDays(15));
        crearEventoDemoSiNoExiste(titulosExistentes, organizador, administrador,
                categorias.get("curso"), "Jornada de Emprendimiento Universitario",
                EstadoEvento.PUBLICADO, Modalidad.PRESENCIAL, fechaBase.plusDays(30));
        crearEventoDemoSiNoExiste(titulosExistentes, organizador, administrador,
                categorias.get("seminario"), "Seminario de Inteligencia Artificial",
                EstadoEvento.EN_REVISION, Modalidad.VIRTUAL, fechaBase.plusDays(45));
        crearEventoDemoSiNoExiste(titulosExistentes, organizador, administrador,
                categorias.get("curso"), "Curso de Gestión de Proyectos",
                EstadoEvento.EN_REVISION, Modalidad.PRESENCIAL, fechaBase.plusDays(60));
        crearEventoDemoSiNoExiste(titulosExistentes, organizador, administrador,
                categorias.get("conferencia"), "Conferencia de Innovación y Tecnología",
                EstadoEvento.EN_REVISION, Modalidad.VIRTUAL, fechaBase.plusDays(75));
        crearEventoDemoSiNoExiste(titulosExistentes, organizador, administrador,
                categorias.get("cultural"), "Jornada Cultural en Preparacion",
                EstadoEvento.BORRADOR, Modalidad.PRESENCIAL, fechaBase.plusDays(90));
        crearEventoDemoSiNoExiste(titulosExistentes, organizador, administrador,
                categorias.get("deportivo"), "Encuentro Deportivo por Corregir",
                EstadoEvento.RECHAZADO, Modalidad.PRESENCIAL, fechaBase.plusDays(105));
    }

    private void crearEventoDemoSiNoExiste(Set<String> titulosExistentes, Usuario organizador,
                                            Usuario administrador, CategoriaEvento categoria, String titulo,
                                            EstadoEvento estado, Modalidad modalidad, LocalDate fechaInicio) {
        String tituloNormalizado = titulo.toLowerCase(Locale.ROOT);
        if (titulosExistentes.contains(tituloNormalizado)) {
            return;
        }
        if (categoria == null) {
            throw new IllegalStateException("No existe la categoria demo requerida para " + titulo);
        }

        LocalDateTime fechaEnvioRevision = LocalDateTime.now(ZONA_OFICIAL);
        boolean publicado = estado == EstadoEvento.PUBLICADO;
        boolean rechazado = estado == EstadoEvento.RECHAZADO;
        boolean pagado = titulo.startsWith("Curso");
        boolean certificado = titulo.startsWith("Congreso");
        boolean sinCupo = titulo.startsWith("Conferencia");
        PublicoObjetivo audiencia = certificado ? PublicoObjetivo.AMBOS
                : modalidad == Modalidad.VIRTUAL ? PublicoObjetivo.EXTERNO : PublicoObjetivo.UAJMS;
        Evento evento = Evento.builder()
                .titulo(titulo)
                .descripcion("Evento ficticio de demostración para el flujo E2 de la plataforma UAJMS.")
                .objetivos("Demostrar el flujo de revisión, publicación e inscripción gratuita.")
                .categoria(categoria)
                .modalidad(modalidad)
                .tipoInscripcion(pagado ? TipoInscripcion.PAGO : TipoInscripcion.GRATUITO)
                .costo(pagado ? new BigDecimal("50.00") : BigDecimal.ZERO)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaInicio.plusDays(1))
                .horaInicio(LocalTime.of(9, 0))
                .horaFin(LocalTime.of(17, 0))
                .ubicacion(modalidad == Modalidad.PRESENCIAL ? "Campus Universitario UAJMS" : null)
                .direccion(modalidad == Modalidad.PRESENCIAL ? "Zona universitaria, Tarija" : null)
                .latitud(modalidad == Modalidad.PRESENCIAL ? new BigDecimal("-21.5350000") : null)
                .longitud(modalidad == Modalidad.PRESENCIAL ? new BigDecimal("-64.7290000") : null)
                .radioMetros(modalidad == Modalidad.PRESENCIAL ? 100 : null)
                .enlaceVirtual(modalidad == Modalidad.VIRTUAL ? "https://demo.local/eventos/virtual" : null)
                .requiereInscripcion(true)
                .cupoLimitado(!sinCupo)
                .cupoMaximo(sinCupo ? null : 100)
                .cupoDisponible(sinCupo ? null : 100)
                .emiteCertificado(certificado)
                .tipoCertificado(certificado ? TipoCertificadoEvento.CURRICULAR : null)
                .horasAcademicas(certificado ? 20 : null)
                .publicoObjetivo(audiencia)
                .telefonoContacto("70000000")
                .emailContacto("eventos@demo.local")
                .whatsappContacto("70000000")
                .instruccionesPago(pagado ? "Transferencia ficticia para demostracion; no realizar pagos reales." : null)
                .estado(estado)
                .fechaEnvioRevision(estado == EstadoEvento.BORRADOR ? null : fechaEnvioRevision)
                .fechaResolucion(publicado || rechazado ? fechaEnvioRevision : null)
                .resueltoPor(publicado || rechazado ? administrador : null)
                .motivoRechazo(rechazado ? "Evento demo pendiente de correccion." : null)
                .organizador(organizador)
                .build();
        eventoRepository.save(evento);
        titulosExistentes.add(tituloNormalizado);
    }
}
