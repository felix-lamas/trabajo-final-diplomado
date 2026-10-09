package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.seguridad.DetallesUsuarioService;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.entidades.Asistencia;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.asistencias.servicios.AsistenciaService;
import bo.uajms.eventos.modulos.asistencias.servicios.QrAsistenciaService;
import bo.uajms.eventos.modulos.certificados.entidades.Certificado;
import bo.uajms.eventos.modulos.certificados.repositorios.CertificadoRepository;
import bo.uajms.eventos.modulos.certificados.servicios.CertificadoDocumentoService;
import bo.uajms.eventos.modulos.certificados.servicios.CertificadoService;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.mappers.InscripcionMapper;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.inscripciones.servicios.InscripcionService;
import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import bo.uajms.eventos.modulos.pagos.mappers.PagoMapper;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.pagos.servicios.AlmacenamientoArchivos;
import bo.uajms.eventos.modulos.pagos.servicios.ArchivoSeguroServicio;
import bo.uajms.eventos.modulos.pagos.servicios.PagoService;
import bo.uajms.eventos.modulos.sesiones.repositorios.SesionEventoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Rol;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.entidades.UsuarioRol;
import bo.uajms.eventos.modulos.usuarios.mappers.UsuarioMapper;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolPermisoRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRolRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Flujo de servicios reales con persistencia simulada, sin escribir en ninguna base. */
class CuentaDualHistorialTest {
    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void solicitarYAprobarConservaIdentidadEHistorialConsultable() {
        Usuario usuario = usuario("participante@example.test");
        Usuario administrador = usuario("administrador@example.test");
        Rol rolUsuario = rol("USUARIO");
        Rol rolOrganizador = rol("ORGANIZADOR");
        var asignaciones = new ArrayList<>(List.of(
                UsuarioRol.builder().usuario(usuario).rol(rolUsuario).build()));
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        UsuarioRolRepository usuarioRoles = mock(UsuarioRolRepository.class);
        RolRepository roles = mock(RolRepository.class);
        when(usuarios.findByCorreoElectronicoIgnoreCase(usuario.getCorreoElectronico())).thenReturn(Optional.of(usuario));
        when(usuarios.findByCorreoElectronicoIgnoreCase(administrador.getCorreoElectronico())).thenReturn(Optional.of(administrador));
        when(usuarios.findByIdForUpdate(usuario.getId())).thenReturn(Optional.of(usuario));
        when(usuarios.save(usuario)).thenReturn(usuario);
        when(usuarioRoles.findByUsuarioId(usuario.getId())).thenReturn(asignaciones);
        when(usuarioRoles.existsByUsuarioIdAndRolNombre(usuario.getId(), "USUARIO")).thenReturn(true);
        when(roles.findByNombre("ORGANIZADOR")).thenReturn(Optional.of(rolOrganizador));
        when(usuarioRoles.save(any(UsuarioRol.class))).thenAnswer(invocacion -> {
            UsuarioRol nueva = invocacion.getArgument(0);
            asignaciones.add(nueva);
            return nueva;
        });
        var detalles = new DetallesUsuarioService(usuarios, usuarioRoles, mock(RolPermisoRepository.class));
        var auth = new UsuarioAutenticadoService(usuarios);
        var cuentas = new UsuarioServicio(usuarios, usuarioRoles, roles, new UsuarioMapper(),
                mock(PasswordEncoder.class), auth, mock(SesionUsuarioServicio.class), mock(bo.uajms.eventos.modulos.usuarios.repositorios.SolicitudOrganizadorHistorialRepository.class));
        var inscripciones = mock(InscripcionRepository.class);
        var pagos = mock(PagoRepository.class);
        var certificados = mock(CertificadoRepository.class);
        var asistencias = mock(AsistenciaRepository.class);
        var eventos = mock(EventoRepository.class);
        Clock clock = Clock.systemUTC();
        var inscripcionService = new InscripcionService(inscripciones, eventos, new InscripcionMapper(), auth, pagos, clock);
        var pagoService = new PagoService(pagos, inscripciones, new PagoMapper(),
                mock(ArchivoSeguroServicio.class), mock(AlmacenamientoArchivos.class), auth, clock);
        var certificadoService = new CertificadoService(certificados, inscripciones, eventos,
                mock(SesionEventoRepository.class), asistencias, pagos, auth, mock(CertificadoDocumentoService.class), clock);
        var asistenciaService = new AsistenciaService(asistencias, eventos, inscripciones,
                mock(QrAsistenciaService.class), auth, clock);

        Evento evento = Evento.builder().titulo("Evento historico ajeno").organizador(administrador).build();
        evento.setId(UUID.randomUUID());
        Evento eventoPropio = Evento.builder().organizador(usuario).build();
        eventoPropio.setId(UUID.randomUUID());
        UUID eventoPropioId = eventoPropio.getId();
        Inscripcion inscripcion = Inscripcion.builder().usuario(usuario).evento(evento)
                .estado(EstadoInscripcion.CONFIRMADA).build();
        inscripcion.setId(UUID.randomUUID());
        Pago pago = Pago.builder().inscripcion(inscripcion).estado(EstadoPago.APROBADO).build();
        pago.setId(UUID.randomUUID());
        Certificado certificado = Certificado.builder().usuario(usuario).evento(evento).inscripcion(inscripcion)
                .tipoCertificado(Certificado.TipoCertificado.NO_CURRICULAR)
                .estado(Certificado.EstadoCertificado.GENERADO).build();
        certificado.setId(UUID.randomUUID());
        Asistencia asistencia = Asistencia.builder().inscripcion(inscripcion).registradoPor(usuario).build();
        asistencia.setId(UUID.randomUUID());
        when(inscripciones.findByUsuarioId(usuario.getId())).thenReturn(List.of(inscripcion));
        when(pagos.findByUsuarioId(usuario.getId())).thenReturn(List.of(pago));
        when(certificados.findByUsuarioId(usuario.getId())).thenReturn(List.of(certificado));
        when(asistencias.findByInscripcionUsuarioId(usuario.getId())).thenReturn(List.of(asistencia));

        autenticar(detalles.loadUserByUsername(usuario.getCorreoElectronico()));
        assertEquals(List.of("USUARIO"), cuentas.obtenerPerfilActual().getRoles());
        var antes = List.of(inscripcionService.listarMisInscripciones().getFirst().getId(),
                pagoService.listarMisPagos().getFirst().getId(),
                certificadoService.listarMisCertificados().getFirst().getId(),
                asistenciaService.obtenerMisAsistencias().getFirst().getId());
        assertEquals("PENDIENTE", cuentas.solicitarSerOrganizador(solicitudValida()).getEstado());
        autenticar(org.springframework.security.core.userdetails.User.withUsername(administrador.getCorreoElectronico())
                .password("hash").roles("ADMINISTRADOR").build());
        assertEquals("APROBADA", cuentas.aprobarSolicitudOrganizador(usuario.getId()).getEstado());
        autenticar(detalles.loadUserByUsername(usuario.getCorreoElectronico()));
        assertTrue(auth.tieneRol("USUARIO"));
        assertTrue(auth.tieneRol("ORGANIZADOR"));
        assertFalse(auth.tieneRol("ADMINISTRADOR"));
        assertEquals(List.of("USUARIO", "ORGANIZADOR"), cuentas.obtenerPerfilActual().getRoles());
        var despues = List.of(inscripcionService.listarMisInscripciones().getFirst().getId(),
                pagoService.listarMisPagos().getFirst().getId(),
                certificadoService.listarMisCertificados().getFirst().getId(),
                asistenciaService.obtenerMisAsistencias().getFirst().getId());
        assertEquals(antes, despues);
        assertSame(usuario, inscripcion.getUsuario());
        assertSame(inscripcion, pago.getInscripcion());
        assertSame(usuario, certificado.getUsuario());
        assertSame(inscripcion, certificado.getInscripcion());
        assertSame(usuario, asistencia.getRegistradoPor());
        assertEquals(eventoPropioId, eventoPropio.getId());
        assertSame(usuario, eventoPropio.getOrganizador());
        assertEquals(EstadoInscripcion.CONFIRMADA, inscripcion.getEstado());
        assertEquals(EstadoPago.APROBADO, pago.getEstado());
        verify(usuarioRoles, never()).deleteByUsuarioId(any());
        verify(inscripciones, never()).save(any());
        verify(pagos, never()).save(any());
        verify(certificados, never()).save(any());
        verify(asistencias, never()).saveAndFlush(any());
        verifyNoInteractions(eventos);
    }

    private static void autenticar(UserDetails detalles) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                detalles, null, detalles.getAuthorities()));
    }

    private static Usuario usuario(String correo) {
        Usuario usuario = Usuario.builder().correoElectronico(correo).contrasena("hash")
                .nombres("Nombre").apellidos("Apellido").tipoUsuario(Usuario.TipoUsuario.EXTERNO).build();
        usuario.setId(UUID.randomUUID());
        return usuario;
    }

    private static Rol rol(String nombre) {
        Rol rol = Rol.builder().nombre(nombre).build();
        rol.setId(UUID.randomUUID());
        return rol;
    }
    private static bo.uajms.eventos.modulos.usuarios.dtos.SolicitarOrganizadorRequest solicitudValida() {
        var request = new bo.uajms.eventos.modulos.usuarios.dtos.SolicitarOrganizadorRequest();
        request.setMotivoSolicitud("Deseo organizar eventos universitarios educativos");
        request.setTiposEventos(java.util.List.of(bo.uajms.eventos.modulos.usuarios.entidades.TipoEventoSolicitud.CURSOS_TALLERES));
        return request;
    }

}
