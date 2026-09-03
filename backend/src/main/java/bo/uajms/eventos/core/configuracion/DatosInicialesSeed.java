package bo.uajms.eventos.core.configuracion;

import bo.uajms.eventos.modulos.asistencias.entidades.Asistencia;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.carreras.entidades.Carrera;
import bo.uajms.eventos.modulos.carreras.repositorios.CarreraRepository;
import bo.uajms.eventos.modulos.categorias.entidades.CategoriaEvento;
import bo.uajms.eventos.modulos.categorias.repositorios.CategoriaEventoRepository;
import bo.uajms.eventos.modulos.certificados.entidades.Certificado;
import bo.uajms.eventos.modulos.certificados.repositorios.CertificadoRepository;
import bo.uajms.eventos.modulos.codigo_qr.entidades.CodigoQr;
import bo.uajms.eventos.modulos.codigo_qr.repositorios.CodigoQrRepository;
import bo.uajms.eventos.modulos.control_acceso.entidades.ControlAcceso;
import bo.uajms.eventos.modulos.control_acceso.repositorios.ControlAccesoRepository;
import bo.uajms.eventos.modulos.credenciales.entidades.Credencial;
import bo.uajms.eventos.modulos.credenciales.repositorios.CredencialRepository;
import bo.uajms.eventos.modulos.encuestas.entidades.Encuesta;
import bo.uajms.eventos.modulos.encuestas.entidades.PreguntaEncuesta;
import bo.uajms.eventos.modulos.encuestas.entidades.RespuestaEncuesta;
import bo.uajms.eventos.modulos.encuestas.entidades.TipoPreguntaEncuesta;
import bo.uajms.eventos.modulos.encuestas.repositorios.EncuestaRepository;
import bo.uajms.eventos.modulos.encuestas.repositorios.PreguntaEncuestaRepository;
import bo.uajms.eventos.modulos.encuestas.repositorios.RespuestaEncuestaRepository;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.Modalidad;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.facultades.entidades.Facultad;
import bo.uajms.eventos.modulos.facultades.repositorios.FacultadRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.entidades.ComprobantePago;
import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import bo.uajms.eventos.modulos.pagos.repositorios.ComprobantePagoRepository;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
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
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DatosInicialesSeed implements CommandLineRunner {

    @org.springframework.beans.factory.annotation.Value("${app.seed.demo-password}")
    private String demoPassword;
    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RolPermisoRepository rolPermisoRepository;
    private final FacultadRepository facultadRepository;
    private final CarreraRepository carreraRepository;
    private final CategoriaEventoRepository categoriaRepository;
    private final EventoRepository eventoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final PagoRepository pagoRepository;
    private final ComprobantePagoRepository comprobantePagoRepository;
    private final CredencialRepository credencialRepository;
    private final CodigoQrRepository codigoQrRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final ControlAccesoRepository controlAccesoRepository;
    private final CertificadoRepository certificadoRepository;
    private final PreguntaEncuestaRepository preguntaEncuestaRepository;
    private final EncuestaRepository encuestaRepository;
    private final RespuestaEncuestaRepository respuestaEncuestaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Map<String, Rol> roles = cargarSeguridad();
        Map<String, Facultad> facultades = cargarFacultadesYCarreras();
        Map<String, Carrera> carreras = cargarCarreras(facultades);
        Map<String, CategoriaEvento> categorias = cargarCategorias();
        Map<String, Usuario> usuarios = cargarUsuarios(roles, carreras);
        List<Evento> eventos = cargarEventos(categorias, usuarios);
        cargarInscripcionesYPagos(eventos, usuarios);
    }

    private Map<String, Rol> cargarSeguridad() {
        String[][] permisos = {
                {"usuarios:leer", "Permite ver la lista de usuarios"},
                {"usuarios:escribir", "Permite crear y editar usuarios"},
                {"eventos:leer", "Permite ver eventos"},
                {"eventos:escribir", "Permite crear y editar eventos"},
                {"catalogos:gestionar", "Permite gestionar facultades, carreras y categorias"},
                {"pagos:validar", "Permite validar y rechazar pagos"},
                {"asistencias:registrar", "Permite controlar accesos y asistencias"},
                {"certificados:emitir", "Permite emitir certificados"},
                {"reportes:leer", "Permite consultar dashboards y reportes"}
        };

        Map<String, Permiso> permisosPorNombre = new LinkedHashMap<>();
        for (String[] permiso : permisos) {
            permisosPorNombre.put(permiso[0], crearPermiso(permiso[0], permiso[1]));
        }

        Map<String, Rol> roles = new LinkedHashMap<>();
        roles.put("ADMINISTRADOR", crearRol("ADMINISTRADOR", "Acceso total al sistema"));
        roles.put("ORGANIZADOR", crearRol("ORGANIZADOR", "Gestiona eventos academicos"));
        roles.put("ESTUDIANTE", crearRol("ESTUDIANTE", "Participante interno UAJMS"));
        roles.put("PARTICIPANTE_EXTERNO", crearRol("PARTICIPANTE_EXTERNO", "Participante externo a la UAJMS"));
        roles.put("PARTICIPANTE", crearRol("PARTICIPANTE", "Rol compatible para certificados y vistas privadas"));
        roles.put("PERSONAL_CONTROL", crearRol("PERSONAL_CONTROL", "Control de accesos, QRs y asistencia"));

        permisosPorNombre.values().forEach(p -> asignarPermisoARol(roles.get("ADMINISTRADOR"), p));
        asignarPermisoARol(roles.get("ORGANIZADOR"), permisosPorNombre.get("eventos:leer"));
        asignarPermisoARol(roles.get("ORGANIZADOR"), permisosPorNombre.get("eventos:escribir"));
        asignarPermisoARol(roles.get("ORGANIZADOR"), permisosPorNombre.get("certificados:emitir"));
        asignarPermisoARol(roles.get("ORGANIZADOR"), permisosPorNombre.get("reportes:leer"));
        asignarPermisoARol(roles.get("PERSONAL_CONTROL"), permisosPorNombre.get("eventos:leer"));
        asignarPermisoARol(roles.get("PERSONAL_CONTROL"), permisosPorNombre.get("asistencias:registrar"));
        asignarPermisoARol(roles.get("ESTUDIANTE"), permisosPorNombre.get("eventos:leer"));
        asignarPermisoARol(roles.get("PARTICIPANTE_EXTERNO"), permisosPorNombre.get("eventos:leer"));
        asignarPermisoARol(roles.get("PARTICIPANTE"), permisosPorNombre.get("eventos:leer"));

        return roles;
    }

    private Map<String, Facultad> cargarFacultadesYCarreras() {
        Map<String, Facultad> facultades = new LinkedHashMap<>();
        String[][] datos = {
                {"Facultad de Ciencias y Tecnologia", "Area academica de ingenierias, tecnologia e innovacion."},
                {"Facultad de Ciencias de la Salud", "Formacion en ciencias de la salud y servicios sanitarios."},
                {"Facultad de Ciencias Agricolas y Forestales", "Formacion agropecuaria, forestal y ambiental."},
                {"Facultad de Ciencias Economicas y Financieras", "Gestion empresarial, economia, contabilidad y finanzas."},
                {"Facultad de Medicina", "Formacion medica y desarrollo de competencias clinicas."},
                {"Facultad de Ciencias Juridicas y Politicas", "Derecho, ciencias politicas y gestion publica."},
                {"Facultad de Humanidades", "Educacion, psicologia, comunicacion e idiomas."},
                {"Facultad de Odontologia", "Formacion odontologica integral."},
                {"Facultad de Ciencias Integradas del Chaco", "Oferta academica regional en el Gran Chaco."},
                {"Facultad de Ciencias Integradas de Villa Montes", "Oferta academica regional en Villa Montes."},
                {"Facultad de Ciencias Integradas de Bermejo", "Oferta academica regional en Bermejo."}
        };

        for (String[] fila : datos) {
            facultades.put(fila[0], crearFacultad(fila[0], fila[1]));
        }
        return facultades;
    }

    private Map<String, Carrera> cargarCarreras(Map<String, Facultad> facultades) {
        Map<String, Carrera> carreras = new LinkedHashMap<>();
        agregarCarrera(carreras, "Ingenieria Informatica", "Software, datos y sistemas de informacion.", facultades.get("Facultad de Ciencias y Tecnologia"));
        agregarCarrera(carreras, "Ingenieria Civil", "Infraestructura, construccion y obras civiles.", facultades.get("Facultad de Ciencias y Tecnologia"));
        agregarCarrera(carreras, "Ingenieria Quimica", "Procesos industriales y transformacion de materiales.", facultades.get("Facultad de Ciencias y Tecnologia"));
        agregarCarrera(carreras, "Ingenieria de Alimentos", "Tecnologia, inocuidad y procesos alimentarios.", facultades.get("Facultad de Ciencias y Tecnologia"));
        agregarCarrera(carreras, "Arquitectura y Urbanismo", "Diseno arquitectonico y planificacion urbana.", facultades.get("Facultad de Ciencias y Tecnologia"));
        agregarCarrera(carreras, "Administracion de Empresas", "Gestion organizacional y emprendimiento.", facultades.get("Facultad de Ciencias Economicas y Financieras"));
        agregarCarrera(carreras, "Contaduria Publica", "Contabilidad, auditoria y tributacion.", facultades.get("Facultad de Ciencias Economicas y Financieras"));
        agregarCarrera(carreras, "Economia", "Analisis economico y politicas publicas.", facultades.get("Facultad de Ciencias Economicas y Financieras"));
        agregarCarrera(carreras, "Derecho", "Ciencias juridicas y practica legal.", facultades.get("Facultad de Ciencias Juridicas y Politicas"));
        agregarCarrera(carreras, "Psicologia", "Procesos psicologicos, educacion y bienestar.", facultades.get("Facultad de Humanidades"));
        agregarCarrera(carreras, "Ciencias de la Educacion", "Pedagogia, didactica y gestion educativa.", facultades.get("Facultad de Humanidades"));
        agregarCarrera(carreras, "Medicina", "Atencion medica integral y salud publica.", facultades.get("Facultad de Medicina"));
        agregarCarrera(carreras, "Enfermeria", "Cuidado integral y gestion de servicios de salud.", facultades.get("Facultad de Ciencias de la Salud"));
        agregarCarrera(carreras, "Bioquimica", "Laboratorio clinico, analisis y diagnostico.", facultades.get("Facultad de Ciencias de la Salud"));
        agregarCarrera(carreras, "Ingenieria Agronomica", "Produccion agricola y desarrollo rural.", facultades.get("Facultad de Ciencias Agricolas y Forestales"));
        agregarCarrera(carreras, "Ingenieria Forestal", "Manejo forestal y conservacion ambiental.", facultades.get("Facultad de Ciencias Agricolas y Forestales"));
        agregarCarrera(carreras, "Odontologia", "Salud bucal, prevencion y rehabilitacion.", facultades.get("Facultad de Odontologia"));
        agregarCarrera(carreras, "Ingenieria Comercial", "Gestion comercial regional y finanzas.", facultades.get("Facultad de Ciencias Integradas del Chaco"));
        agregarCarrera(carreras, "Ingenieria del Petroleo y Gas Natural", "Energia, hidrocarburos y procesos productivos.", facultades.get("Facultad de Ciencias Integradas de Villa Montes"));
        agregarCarrera(carreras, "Comercio Internacional", "Operaciones comerciales y mercados fronterizos.", facultades.get("Facultad de Ciencias Integradas de Bermejo"));
        return carreras;
    }

    private Map<String, CategoriaEvento> cargarCategorias() {
        Map<String, CategoriaEvento> categorias = new LinkedHashMap<>();
        agregarCategoria(categorias, "Conferencia", "Exposicion academica breve con especialistas invitados.");
        agregarCategoria(categorias, "Curso", "Actividad formativa con desarrollo secuencial de contenidos.");
        agregarCategoria(categorias, "Diplomado", "Programa de formacion continua con carga horaria extendida.");
        agregarCategoria(categorias, "Taller", "Actividad practica orientada a habilidades aplicadas.");
        agregarCategoria(categorias, "Seminario", "Encuentro academico de analisis y discusion especializada.");
        agregarCategoria(categorias, "Congreso", "Evento academico de gran alcance con multiples ponencias.");
        return categorias;
    }

    private Map<String, Usuario> cargarUsuarios(Map<String, Rol> roles, Map<String, Carrera> carreras) {
        Map<String, Usuario> usuarios = new LinkedHashMap<>();

        Usuario admin = crearUsuario("Administrador", "General UAJMS", "admin@uajms.edu.bo", "0000000", "72900001", Usuario.TipoUsuario.INTERNO, null);
        asignarRol(admin, roles.get("ADMINISTRADOR"));
        usuarios.put("admin", admin);

        Usuario control = crearUsuario("Mario", "Vargas Rios", "control.acceso@uajms.edu.bo", "2000001", "72900002", Usuario.TipoUsuario.INTERNO, carreras.get("Ingenieria Informatica"));
        asignarRol(control, roles.get("PERSONAL_CONTROL"));
        usuarios.put("control", control);

        String[][] organizadores = {
                {"Laura", "Mendoza Arce", "laura.mendoza@uajms.edu.bo", "3000001", "72910001"},
                {"Carlos", "Paredes Flores", "carlos.paredes@uajms.edu.bo", "3000002", "72910002"},
                {"Mariela", "Gutierrez Rojas", "mariela.gutierrez@uajms.edu.bo", "3000003", "72910003"}
        };
        for (int i = 0; i < organizadores.length; i++) {
            Usuario organizador = crearUsuario(organizadores[i][0], organizadores[i][1], organizadores[i][2], organizadores[i][3], organizadores[i][4], Usuario.TipoUsuario.INTERNO, carreras.get(i == 0 ? "Ingenieria Informatica" : i == 1 ? "Administracion de Empresas" : "Medicina"));
            asignarRol(organizador, roles.get("ORGANIZADOR"));
            usuarios.put("organizador" + (i + 1), organizador);
        }

        String[][] estudiantes = {
                {"Ana Lucia", "Rojas Molina", "ana.rojas@est.uajms.edu.bo", "4100001", "75110001", "Ingenieria Informatica"},
                {"Diego", "Sanchez Aguilar", "diego.sanchez@est.uajms.edu.bo", "4100002", "75110002", "Ingenieria Civil"},
                {"Valeria", "Torres Nava", "valeria.torres@est.uajms.edu.bo", "4100003", "75110003", "Medicina"},
                {"Jorge", "Camacho Ruiz", "jorge.camacho@est.uajms.edu.bo", "4100004", "75110004", "Derecho"},
                {"Sofia", "Quiroga Salazar", "sofia.quiroga@est.uajms.edu.bo", "4100005", "75110005", "Administracion de Empresas"},
                {"Miguel", "Romero Castillo", "miguel.romero@est.uajms.edu.bo", "4100006", "75110006", "Contaduria Publica"},
                {"Daniela", "Lopez Mercado", "daniela.lopez@est.uajms.edu.bo", "4100007", "75110007", "Psicologia"},
                {"Rodrigo", "Vaca Fernandez", "rodrigo.vaca@est.uajms.edu.bo", "4100008", "75110008", "Ingenieria Agronomica"},
                {"Camila", "Arias Pena", "camila.arias@est.uajms.edu.bo", "4100009", "75110009", "Enfermeria"},
                {"Fernando", "Ribera Soto", "fernando.ribera@est.uajms.edu.bo", "4100010", "75110010", "Economia"},
                {"Natalia", "Cortez Vargas", "natalia.cortez@est.uajms.edu.bo", "4100011", "75110011", "Bioquimica"},
                {"Luis", "Mamani Condori", "luis.mamani@est.uajms.edu.bo", "4100012", "75110012", "Odontologia"},
                {"Paola", "Aguilera Paz", "paola.aguilera@est.uajms.edu.bo", "4100013", "75110013", "Ciencias de la Educacion"},
                {"Sergio", "Mendez Ortiz", "sergio.mendez@est.uajms.edu.bo", "4100014", "75110014", "Ingenieria Comercial"}
        };
        for (int i = 0; i < estudiantes.length; i++) {
            Usuario estudiante = crearUsuario(estudiantes[i][0], estudiantes[i][1], estudiantes[i][2], estudiantes[i][3], estudiantes[i][4], Usuario.TipoUsuario.INTERNO, carreras.get(estudiantes[i][5]));
            asignarRol(estudiante, roles.get("ESTUDIANTE"));
            asignarRol(estudiante, roles.get("PARTICIPANTE"));
            usuarios.put("participante" + (i + 1), estudiante);
        }

        String[][] externos = {
                {"Gabriela", "Salinas Vega", "gabriela.salinas@example.com", "5100001", "76120001"},
                {"Andres", "Montano Saavedra", "andres.montano@example.com", "5100002", "76120002"},
                {"Patricia", "Delgado Rios", "patricia.delgado@example.com", "5100003", "76120003"},
                {"Hector", "Valdez Pereira", "hector.valdez@example.com", "5100004", "76120004"},
                {"Claudia", "Rivera Campos", "claudia.rivera@example.com", "5100005", "76120005"},
                {"Mauricio", "Alvarez Leon", "mauricio.alvarez@example.com", "5100006", "76120006"}
        };
        for (int i = 0; i < externos.length; i++) {
            Usuario externo = crearUsuario(externos[i][0], externos[i][1], externos[i][2], externos[i][3], externos[i][4], Usuario.TipoUsuario.EXTERNO, null);
            asignarRol(externo, roles.get("PARTICIPANTE_EXTERNO"));
            asignarRol(externo, roles.get("PARTICIPANTE"));
            usuarios.put("externo" + (i + 1), externo);
        }

        return usuarios;
    }

    private List<Evento> cargarEventos(Map<String, CategoriaEvento> categorias, Map<String, Usuario> usuarios) {
        LocalDate hoy = LocalDate.now();
        List<Evento> eventos = new ArrayList<>();
        eventos.add(crearEvento("Conferencia de Transformacion Digital Universitaria", categorias.get("Conferencia"), usuarios.get("organizador1"), EstadoEvento.PUBLICADO, Modalidad.PRESENCIAL, TipoInscripcion.GRATUITO, "0", hoy.plusDays(12), hoy.plusDays(12), 120, 8, "Auditorio Central UAJMS"));
        eventos.add(crearEvento("Curso de Analisis de Datos con Python", categorias.get("Curso"), usuarios.get("organizador1"), EstadoEvento.PUBLICADO, Modalidad.HIBRIDO, TipoInscripcion.PAGO, "120", hoy.plusDays(18), hoy.plusDays(22), 80, 30, "Laboratorio de Informatica"));
        eventos.add(crearEvento("Taller de Emprendimiento e Innovacion", categorias.get("Taller"), usuarios.get("organizador2"), EstadoEvento.PUBLICADO, Modalidad.PRESENCIAL, TipoInscripcion.GRATUITO, "0", hoy.plusDays(25), hoy.plusDays(25), 60, 6, "Aula Magna Economia"));
        eventos.add(crearEvento("Seminario de Salud Publica Regional", categorias.get("Seminario"), usuarios.get("organizador3"), EstadoEvento.PUBLICADO, Modalidad.VIRTUAL, TipoInscripcion.PAGO, "80", hoy.plusDays(30), hoy.plusDays(31), 150, 12, null));
        eventos.add(crearEvento("Congreso UAJMS de Investigacion Cientifica", categorias.get("Congreso"), usuarios.get("organizador2"), EstadoEvento.PUBLICADO, Modalidad.HIBRIDO, TipoInscripcion.PAGO, "180", hoy.plusDays(45), hoy.plusDays(47), 250, 24, "Coliseo Universitario"));

        eventos.add(crearEvento("Diplomado en Gestion Publica Universitaria", categorias.get("Diplomado"), usuarios.get("organizador2"), EstadoEvento.BORRADOR, Modalidad.VIRTUAL, TipoInscripcion.PAGO, "950", hoy.plusDays(60), hoy.plusDays(120), 40, 120, null));
        eventos.add(crearEvento("Curso de Normativa Academica y Calidad", categorias.get("Curso"), usuarios.get("organizador2"), EstadoEvento.BORRADOR, Modalidad.PRESENCIAL, TipoInscripcion.GRATUITO, "0", hoy.plusDays(38), hoy.plusDays(40), 50, 16, "Facultad de Humanidades"));
        eventos.add(crearEvento("Taller de Escritura Cientifica", categorias.get("Taller"), usuarios.get("organizador1"), EstadoEvento.BORRADOR, Modalidad.HIBRIDO, TipoInscripcion.GRATUITO, "0", hoy.plusDays(52), hoy.plusDays(53), 70, 10, "Biblioteca Central"));
        eventos.add(crearEvento("Seminario de Derecho Ambiental", categorias.get("Seminario"), usuarios.get("organizador2"), EstadoEvento.BORRADOR, Modalidad.PRESENCIAL, TipoInscripcion.PAGO, "60", hoy.plusDays(70), hoy.plusDays(70), 90, 8, "Salon Facultad de Derecho"));
        eventos.add(crearEvento("Conferencia de Inteligencia Artificial Responsable", categorias.get("Conferencia"), usuarios.get("organizador1"), EstadoEvento.BORRADOR, Modalidad.VIRTUAL, TipoInscripcion.GRATUITO, "0", hoy.plusDays(80), hoy.plusDays(80), 200, 4, null));

        eventos.add(crearEvento("Congreso de Ciencias de la Salud del Sur", categorias.get("Congreso"), usuarios.get("organizador3"), EstadoEvento.FINALIZADO, Modalidad.PRESENCIAL, TipoInscripcion.PAGO, "160", hoy.minusDays(75), hoy.minusDays(73), 180, 24, "Auditorio Medicina"));
        eventos.add(crearEvento("Curso de Bioseguridad en Laboratorios", categorias.get("Curso"), usuarios.get("organizador3"), EstadoEvento.FINALIZADO, Modalidad.PRESENCIAL, TipoInscripcion.GRATUITO, "0", hoy.minusDays(55), hoy.minusDays(53), 90, 20, "Laboratorio de Bioquimica"));
        eventos.add(crearEvento("Taller de Prototipado para Ingenieria", categorias.get("Taller"), usuarios.get("organizador1"), EstadoEvento.FINALIZADO, Modalidad.PRESENCIAL, TipoInscripcion.PAGO, "100", hoy.minusDays(40), hoy.minusDays(39), 75, 12, "FabLab UAJMS"));
        eventos.add(crearEvento("Seminario de Tributacion Boliviana", categorias.get("Seminario"), usuarios.get("organizador2"), EstadoEvento.FINALIZADO, Modalidad.VIRTUAL, TipoInscripcion.PAGO, "70", hoy.minusDays(28), hoy.minusDays(27), 110, 10, null));
        eventos.add(crearEvento("Conferencia de Extension Universitaria", categorias.get("Conferencia"), usuarios.get("organizador2"), EstadoEvento.FINALIZADO, Modalidad.HIBRIDO, TipoInscripcion.GRATUITO, "0", hoy.minusDays(18), hoy.minusDays(18), 140, 4, "Auditorio Central UAJMS"));
        return eventos;
    }

    private void cargarInscripcionesYPagos(List<Evento> eventos, Map<String, Usuario> usuarios) {
        List<Usuario> participantes = usuarios.entrySet().stream()
                .filter(e -> e.getKey().startsWith("participante") || e.getKey().startsWith("externo"))
                .map(Map.Entry::getValue)
                .toList();
        Usuario control = usuarios.get("control");

        int codigoSecuencia = 1;
        for (int eventIndex = 0; eventIndex < eventos.size(); eventIndex++) {
            Evento evento = eventos.get(eventIndex);
            if (evento.getEstado() == EstadoEvento.BORRADOR) {
                continue;
            }

            int cantidad = evento.getEstado() == EstadoEvento.FINALIZADO ? 10 : 7;
            for (int i = 0; i < cantidad; i++) {
                Usuario participante = participantes.get((eventIndex + i) % participantes.size());
                String codigo = "UAJMS-" + evento.getFechaInicio().getYear() + "-" + String.format("%04d", codigoSecuencia++);
                EstadoInscripcion estado = estadoInicial(evento, i);
                Inscripcion inscripcion = crearInscripcion(participante, evento, codigo, estado, i);

                if (evento.getTipoInscripcion() == TipoInscripcion.PAGO) {
                    EstadoPago estadoPago = estadoPagoPara(i);
                    crearPago(inscripcion, evento.getCosto(), estadoPago, i);
                    if (estadoPago == EstadoPago.VALIDADO && estado != EstadoInscripcion.ASISTIO) {
                        inscripcion.setEstado(EstadoInscripcion.CONFIRMADA);
                        inscripcionRepository.save(inscripcion);
                    }
                }

                boolean habilitada = inscripcion.getEstado() == EstadoInscripcion.CONFIRMADA || inscripcion.getEstado() == EstadoInscripcion.ASISTIO;
                if (habilitada) {
                    Credencial credencial = crearCredencial(inscripcion, codigo);
                    CodigoQr qr = crearCodigoQr(credencial, evento.getEstado() == EstadoEvento.FINALIZADO && i % 2 == 0);

                    if (evento.getEstado() == EstadoEvento.FINALIZADO) {
                        registrarAccesoYAsistencia(inscripcion, credencial, qr, control, i);
                        if (i % 5 != 4) {
                            crearCertificado(inscripcion, i);
                            crearEncuesta(inscripcion, i);
                        }
                    }
                }
            }
            recalcularCupoDisponible(evento);
        }
    }

    private EstadoInscripcion estadoInicial(Evento evento, int index) {
        if (evento.getEstado() == EstadoEvento.FINALIZADO) {
            return index % 5 == 4 ? EstadoInscripcion.NO_ASISTIO : EstadoInscripcion.ASISTIO;
        }
        if (evento.getTipoInscripcion() == TipoInscripcion.GRATUITO) {
            return EstadoInscripcion.CONFIRMADA;
        }
        return switch (index % 3) {
            case 0 -> EstadoInscripcion.PENDIENTE_VALIDACION;
            case 1 -> EstadoInscripcion.CONFIRMADA;
            default -> EstadoInscripcion.RECHAZADA;
        };
    }

    private EstadoPago estadoPagoPara(int index) {
        return switch (index % 3) {
            case 0 -> EstadoPago.PENDIENTE;
            case 1 -> EstadoPago.VALIDADO;
            default -> EstadoPago.RECHAZADO;
        };
    }

    private void registrarAccesoYAsistencia(Inscripcion inscripcion, Credencial credencial, CodigoQr qr, Usuario control, int index) {
        LocalDateTime fecha = inscripcion.getEvento().getFechaInicio().atTime(8, 30).plusMinutes(index * 7L);
        ControlAcceso.EstadoIngreso ingreso = inscripcion.getEstado() == EstadoInscripcion.ASISTIO
                ? ControlAcceso.EstadoIngreso.AUTORIZADO
                : ControlAcceso.EstadoIngreso.DENEGADO;

        if (!controlAccesoRepository.findByCredencialIdOrderByFechaHoraIngresoDesc(credencial.getId()).isEmpty()) {
            return;
        }

        controlAccesoRepository.save(ControlAcceso.builder()
                .credencial(credencial)
                .usuarioControl(control)
                .fechaHoraIngreso(fecha)
                .estadoIngreso(ingreso)
                .observacion(ingreso == ControlAcceso.EstadoIngreso.AUTORIZADO ? "Ingreso autorizado por QR demo." : "Ingreso denegado para prueba funcional.")
                .build());

        if (ingreso == ControlAcceso.EstadoIngreso.AUTORIZADO && !asistenciaRepository.existsByInscripcionIdAndFechaEliminacionIsNull(inscripcion.getId())) {
            asistenciaRepository.save(Asistencia.builder()
                    .inscripcion(inscripcion)
                    .usuarioControl(control)
                    .fechaHoraRegistro(fecha.plusMinutes(2))
                    .observacion("Asistencia registrada desde carga demo.")
                    .build());
            qr.setEstadoQr(CodigoQr.EstadoQr.UTILIZADO);
            codigoQrRepository.save(qr);
        }
    }

    private void crearEncuesta(Inscripcion inscripcion, int index) {
        if (encuestaRepository.existsByEventoIdAndUsuarioId(inscripcion.getEvento().getId(), inscripcion.getUsuario().getId())) {
            return;
        }

        PreguntaEncuesta preguntaCalificacion = crearPregunta("Califique la organizacion general del evento", TipoPreguntaEncuesta.CALIFICACION, 1, true);
        PreguntaEncuesta preguntaComentario = crearPregunta("Comparta un comentario sobre contenidos y logistica", TipoPreguntaEncuesta.COMENTARIO, 2, false);

        Encuesta encuesta = encuestaRepository.save(Encuesta.builder()
                .evento(inscripcion.getEvento())
                .usuario(inscripcion.getUsuario())
                .inscripcion(inscripcion)
                .build());

        respuestaEncuestaRepository.save(RespuestaEncuesta.builder()
                .encuesta(encuesta)
                .pregunta(preguntaCalificacion)
                .calificacion(3 + (index % 3))
                .build());

        respuestaEncuestaRepository.save(RespuestaEncuesta.builder()
                .encuesta(encuesta)
                .pregunta(preguntaComentario)
                .comentario(comentariosEncuesta().get(index % comentariosEncuesta().size()))
                .build());
    }

    private List<String> comentariosEncuesta() {
        return List.of(
                "La organizacion fue clara y los contenidos aportaron a mi formacion.",
                "El registro y control de acceso funcionaron sin inconvenientes.",
                "Seria util ampliar el tiempo de preguntas al final de cada bloque.",
                "Los materiales compartidos fueron pertinentes para el tema.",
                "La modalidad del evento facilito la participacion de estudiantes y externos."
        );
    }

    private void crearCertificado(Inscripcion inscripcion, int index) {
        if (certificadoRepository.existsByInscripcionId(inscripcion.getId())) {
            return;
        }
        String codigo = "UAJMS-CERT-" + inscripcion.getEvento().getFechaInicio().getYear() + "-" + inscripcion.getCodigoParticipante().replace("UAJMS-", "");
        certificadoRepository.save(Certificado.builder()
                .usuario(inscripcion.getUsuario())
                .evento(inscripcion.getEvento())
                .inscripcion(inscripcion)
                .codigoCertificado(codigo)
                .fechaEmision(inscripcion.getEvento().getFechaFin().atTime(18, 0).plusDays(1))
                .urlVerificacion("http://localhost:4200/publico/verificacion/" + codigo)
                .archivoPdfUrl("https://storage.uajms.edu.bo/certificados/" + codigo + ".pdf")
                .estado(index % 4 == 0 ? Certificado.EstadoCertificado.DESCARGADO : Certificado.EstadoCertificado.GENERADO)
                .build());
    }

    private CodigoQr crearCodigoQr(Credencial credencial, boolean utilizado) {
        return codigoQrRepository.findByCredencialId(credencial.getId())
                .orElseGet(() -> codigoQrRepository.save(CodigoQr.builder()
                        .credencial(credencial)
                        .contenido("QR-DEMO|" + credencial.getCodigoParticipante() + "|" + UUID.randomUUID())
                        .fechaGeneracion(credencial.getFechaGeneracion())
                        .estadoQr(utilizado ? CodigoQr.EstadoQr.UTILIZADO : CodigoQr.EstadoQr.GENERADO)
                        .activo(true)
                        .build()));
    }

    private Credencial crearCredencial(Inscripcion inscripcion, String codigo) {
        return credencialRepository.findByInscripcionId(inscripcion.getId())
                .orElseGet(() -> credencialRepository.save(Credencial.builder()
                        .usuario(inscripcion.getUsuario())
                        .evento(inscripcion.getEvento())
                        .inscripcion(inscripcion)
                        .codigoParticipante(codigo)
                        .fechaGeneracion(inscripcion.getFechaInscripcion().plusHours(1))
                        .estado("ACTIVA")
                        .build()));
    }

    private void crearPago(Inscripcion inscripcion, BigDecimal monto, EstadoPago estado, int index) {
        if (pagoRepository.findByInscripcionId(inscripcion.getId()).isPresent()) {
            return;
        }
        Pago pago = pagoRepository.save(Pago.builder()
                .inscripcion(inscripcion)
                .monto(monto)
                .fechaPago(inscripcion.getFechaInscripcion().plusHours(4))
                .estado(estado)
                .observacion(observacionPago(estado))
                .build());

        ComprobantePago comprobante = ComprobantePago.builder()
                .pago(pago)
                .urlArchivo("uploads/demo/comprobantes/" + inscripcion.getCodigoParticipante() + ".pdf")
                .nombreArchivo("comprobante-" + inscripcion.getCodigoParticipante() + ".pdf")
                .tipoContenido("application/pdf")
                .build();
        comprobantePagoRepository.save(comprobante);
        pago.setComprobante(comprobante);
        pagoRepository.save(pago);

        if (estado == EstadoPago.PENDIENTE) {
            inscripcion.setEstado(EstadoInscripcion.PENDIENTE_VALIDACION);
        } else if (estado == EstadoPago.RECHAZADO) {
            inscripcion.setEstado(EstadoInscripcion.RECHAZADA);
        }
        inscripcionRepository.save(inscripcion);
    }

    private String observacionPago(EstadoPago estado) {
        return switch (estado) {
            case PENDIENTE -> "Comprobante recibido, pendiente de validacion administrativa.";
            case VALIDADO -> "Pago validado contra comprobante bancario demo.";
            case RECHAZADO -> "Comprobante rechazado por datos incompletos en escenario demo.";
        };
    }

    private Inscripcion crearInscripcion(Usuario usuario, Evento evento, String codigo, EstadoInscripcion estado, int index) {
        return inscripcionRepository.findByUsuarioIdAndEventoId(usuario.getId(), evento.getId())
                .orElseGet(() -> inscripcionRepository.save(Inscripcion.builder()
                        .usuario(usuario)
                        .evento(evento)
                        .codigoParticipante(codigo)
                        .fechaInscripcion(evento.getFechaInicio().atTime(9, 0).minusDays(10).plusHours(index))
                        .estado(estado)
                        .observacion("Inscripcion generada para datos demo coherentes.")
                        .build()));
    }

    private void recalcularCupoDisponible(Evento evento) {
        int inscritos = inscripcionRepository.findByEventoId(evento.getId()).size();
        evento.setCupoDisponible(Math.max(0, evento.getCupoMaximo() - inscritos));
        eventoRepository.save(evento);
    }

    private Evento crearEvento(String titulo, CategoriaEvento categoria, Usuario organizador, EstadoEvento estado, Modalidad modalidad,
                               TipoInscripcion tipoInscripcion, String costo, LocalDate fechaInicio, LocalDate fechaFin,
                               int cupoMaximo, int cargaHoraria, String ubicacion) {
        return eventoRepository.findAll().stream()
                .filter(e -> e.getTitulo().equalsIgnoreCase(titulo))
                .findFirst()
                .orElseGet(() -> eventoRepository.save(Evento.builder()
                        .titulo(titulo)
                        .descripcion("Actividad academica UAJMS orientada a fortalecer competencias, investigacion y vinculacion universitaria.")
                        .objetivos("Fortalecer capacidades academicas, promover intercambio de experiencias y generar evidencia para reportes institucionales.")
                        .categoria(categoria)
                        .modalidad(modalidad)
                        .tipoInscripcion(tipoInscripcion)
                        .costo(new BigDecimal(costo))
                        .fechaInicio(fechaInicio)
                        .fechaFin(fechaFin)
                        .horaInicio(LocalTime.of(8, 30))
                        .horaFin(LocalTime.of(18, 0))
                        .ubicacion(ubicacion)
                        .enlaceVirtual(modalidad == Modalidad.PRESENCIAL ? null : "https://meet.uajms.edu.bo/demo/" + titulo.toLowerCase().replace(" ", "-"))
                        .cupoMaximo(cupoMaximo)
                        .cupoDisponible(cupoMaximo)
                        .cargaHoraria(cargaHoraria)
                        .asistenciaMinimaCert(80)
                        .estado(estado)
                        .imagenPortada("https://storage.uajms.edu.bo/eventos/demo/" + titulo.toLowerCase().replace(" ", "-") + ".jpg")
                        .organizador(organizador)
                        .build()));
    }

    private PreguntaEncuesta crearPregunta(String texto, TipoPreguntaEncuesta tipo, int orden, boolean obligatoria) {
        return preguntaEncuestaRepository.findByActivaTrueOrderByOrdenAsc().stream()
                .filter(p -> p.getTexto().equalsIgnoreCase(texto))
                .findFirst()
                .orElseGet(() -> preguntaEncuestaRepository.save(PreguntaEncuesta.builder()
                        .texto(texto)
                        .tipo(tipo)
                        .obligatoria(obligatoria)
                        .orden(orden)
                        .activa(true)
                        .build()));
    }

    private void agregarCategoria(Map<String, CategoriaEvento> categorias, String nombre, String descripcion) {
        categorias.put(nombre, categoriaRepository.findAll().stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseGet(() -> categoriaRepository.save(CategoriaEvento.builder()
                        .nombre(nombre)
                        .descripcion(descripcion)
                        .estado(ESTADO_ACTIVO)
                        .build())));
    }

    private Facultad crearFacultad(String nombre, String descripcion) {
        return facultadRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> facultadRepository.save(Facultad.builder()
                        .nombre(nombre)
                        .descripcion(descripcion)
                        .estado(ESTADO_ACTIVO)
                        .build()));
    }

    private void agregarCarrera(Map<String, Carrera> carreras, String nombre, String descripcion, Facultad facultad) {
        Carrera carrera = carreraRepository.findByFacultadId(facultad.getId()).stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseGet(() -> carreraRepository.save(Carrera.builder()
                        .nombre(nombre)
                        .descripcion(descripcion)
                        .estado(ESTADO_ACTIVO)
                        .facultad(facultad)
                        .build()));
        carreras.put(nombre, carrera);
    }

    private Usuario crearUsuario(String nombres, String apellidos, String correo, String ci, String celular, Usuario.TipoUsuario tipo, Carrera carrera) {
        return usuarioRepository.findByCorreoElectronico(correo)
                .map(usuario -> {
                    if (!passwordEncoder.matches(demoPassword, usuario.getContrasena())) {
                        usuario.setContrasena(passwordEncoder.encode(demoPassword));
                        return usuarioRepository.save(usuario);
                    }
                    return usuario;
                })
                .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                        .nombres(nombres)
                        .apellidos(apellidos)
                        .correoElectronico(correo)
                        .contrasena(passwordEncoder.encode(demoPassword))
                        .ci(ci)
                        .celular(celular)
                        .tipoUsuario(tipo)
                        .carrera(carrera)
                        .fotografiaUrl("https://storage.uajms.edu.bo/usuarios/demo/" + correo.replace("@", "-") + ".jpg")
                        .nombreArchivoFotografia(correo.substring(0, correo.indexOf('@')) + ".jpg")
                        .fechaCargaFotografia(LocalDateTime.now())
                        .build()));
    }

    private Permiso crearPermiso(String nombre, String descripcion) {
        return permisoRepository.findByNombre(nombre)
                .orElseGet(() -> permisoRepository.save(Permiso.builder()
                        .nombre(nombre)
                        .descripcion(descripcion)
                        .build()));
    }

    private Rol crearRol(String nombre, String descripcion) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(Rol.builder()
                        .nombre(nombre)
                        .descripcion(descripcion)
                        .build()));
    }

    private void asignarPermisoARol(Rol rol, Permiso permiso) {
        boolean existe = rolPermisoRepository.findByRolId(rol.getId()).stream()
                .anyMatch(rp -> rp.getPermiso().getId().equals(permiso.getId()));
        if (!existe) {
            rolPermisoRepository.save(RolPermiso.builder().rol(rol).permiso(permiso).build());
        }
    }

    private void asignarRol(Usuario usuario, Rol rol) {
        boolean existe = usuarioRolRepository.findByUsuarioId(usuario.getId()).stream()
                .anyMatch(ur -> ur.getRol().getId().equals(rol.getId()));
        if (!existe) {
            usuarioRolRepository.save(UsuarioRol.builder().usuario(usuario).rol(rol).build());
        }
    }
}
