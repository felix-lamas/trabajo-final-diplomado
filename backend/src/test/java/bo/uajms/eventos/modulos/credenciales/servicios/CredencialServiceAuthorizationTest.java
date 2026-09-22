package bo.uajms.eventos.modulos.credenciales.servicios;

import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.codigo_qr.entidades.CodigoQr;
import bo.uajms.eventos.modulos.codigo_qr.repositorios.CodigoQrRepository;
import bo.uajms.eventos.modulos.codigo_qr.servicios.CodigoQrService;
import bo.uajms.eventos.modulos.credenciales.dtos.CredencialResponse;
import bo.uajms.eventos.modulos.credenciales.entidades.Credencial;
import bo.uajms.eventos.modulos.credenciales.mappers.CredencialMapper;
import bo.uajms.eventos.modulos.credenciales.repositorios.CredencialRepository;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredencialServiceAuthorizationTest {

    private static final byte[] PNG_VALIDO = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Y9Zl8sAAAAASUVORK5CYII="
    );

    @Mock private CredencialRepository credencialRepository;
    @Mock private CodigoQrRepository codigoQrRepository;
    @Mock private InscripcionRepository inscripcionRepository;
    @Mock private CodigoQrService codigoQrService;
    @Mock private CredencialMapper credencialMapper;
    @Mock private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private CredencialService credencialService;

    private Usuario usuarioA;
    private Usuario usuarioB;
    private Usuario organizadorA;
    private Usuario organizadorB;
    private Inscripcion inscripcionA;
    private Inscripcion inscripcionB;
    private Credencial credencialA;
    private Credencial credencialB;
    private CodigoQr qrA;
    private UUID credencialAId;
    private UUID credencialBId;

    @BeforeEach
    void configurarEscenario() {
        usuarioA = usuario();
        usuarioB = usuario();
        organizadorA = usuario();
        organizadorB = usuario();
        Evento eventoA = evento(organizadorA);
        Evento eventoB = evento(organizadorB);
        inscripcionA = inscripcion(usuarioA, eventoA);
        inscripcionB = inscripcion(usuarioB, eventoB);
        credencialA = credencial(usuarioA, eventoA, inscripcionA);
        credencialB = credencial(usuarioB, eventoB, inscripcionB);
        credencialAId = credencialA.getId();
        credencialBId = credencialB.getId();
        qrA = qr(credencialA);
    }

    @Test
    void usuarioGeneraCredencialParaSuInscripcionConfirmada() {
        autenticarParticipante(usuarioA);
        CredencialResponse response = respuesta(UUID.randomUUID());
        when(inscripcionRepository.findByIdAndUsuarioId(inscripcionA.getId(), usuarioA.getId()))
                .thenReturn(Optional.of(inscripcionA));
        when(credencialRepository.existsByInscripcionId(inscripcionA.getId())).thenReturn(false);
        when(credencialRepository.save(any(Credencial.class))).thenAnswer(invocation -> {
            Credencial credencial = invocation.getArgument(0);
            credencial.setId(response.getId());
            return credencial;
        });
        when(credencialMapper.toResponse(any(Credencial.class))).thenReturn(response);

        assertSame(response, credencialService.generarCredencial(inscripcionA.getId()));

        verify(credencialRepository).save(any(Credencial.class));
        verify(codigoQrRepository).save(any(CodigoQr.class));
        verify(inscripcionRepository, never()).findById(inscripcionA.getId());
    }

    @Test
    void usuarioNoGeneraCredencialParaInscripcionAjena() {
        autenticarParticipante(usuarioA);
        when(inscripcionRepository.findByIdAndUsuarioId(inscripcionB.getId(), usuarioA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> credencialService.generarCredencial(inscripcionB.getId()));

        verify(credencialRepository, never()).save(any());
        verify(codigoQrRepository, never()).save(any());
        verify(credencialRepository, never()).existsByInscripcionId(any());
    }

    @Test
    void administradorGeneraCredencialGlobalmente() {
        autenticarAdministrador();
        CredencialResponse response = respuesta(UUID.randomUUID());
        when(inscripcionRepository.findById(inscripcionB.getId())).thenReturn(Optional.of(inscripcionB));
        when(credencialRepository.existsByInscripcionId(inscripcionB.getId())).thenReturn(false);
        when(credencialRepository.save(any(Credencial.class))).thenAnswer(invocation -> {
            Credencial credencial = invocation.getArgument(0);
            credencial.setId(response.getId());
            return credencial;
        });
        when(credencialMapper.toResponse(any(Credencial.class))).thenReturn(response);

        assertSame(response, credencialService.generarCredencial(inscripcionB.getId()));
        verify(codigoQrRepository).save(any(CodigoQr.class));
    }

    @Test
    void usuarioConsultaSuCredencial() {
        autenticarParticipante(usuarioA);
        CredencialResponse response = respuesta(credencialAId);
        when(credencialRepository.findByIdAndUsuarioId(credencialAId, usuarioA.getId()))
                .thenReturn(Optional.of(credencialA));
        when(credencialMapper.toResponse(credencialA)).thenReturn(response);

        assertSame(response, credencialService.obtenerPorId(credencialAId));
        verify(credencialRepository, never()).findById(credencialAId);
    }

    @Test
    void usuarioNoConsultaCredencialAjena() {
        autenticarParticipante(usuarioA);
        when(credencialRepository.findByIdAndUsuarioId(credencialBId, usuarioA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> credencialService.obtenerPorId(credencialBId));
        verify(credencialMapper, never()).toResponse(any());
    }

    @Test
    void organizadorConsultaCredencialDeSuEvento() {
        autenticarOrganizador(organizadorA);
        CredencialResponse response = respuesta(credencialAId);
        when(credencialRepository.findByIdAndEventoOrganizadorId(credencialAId, organizadorA.getId()))
                .thenReturn(Optional.of(credencialA));
        when(credencialMapper.toResponse(credencialA)).thenReturn(response);

        assertSame(response, credencialService.obtenerPorId(credencialAId));
    }

    @Test
    void organizadorNoConsultaCredencialDeEventoAjeno() {
        autenticarOrganizador(organizadorA);
        when(credencialRepository.findByIdAndEventoOrganizadorId(credencialBId, organizadorA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> credencialService.obtenerPorId(credencialBId));
        verify(credencialRepository, never()).findById(credencialBId);
    }

    @Test
    void administradorConsultaCredencialGlobalmente() {
        autenticarAdministrador();
        CredencialResponse response = respuesta(credencialBId);
        when(credencialRepository.findById(credencialBId)).thenReturn(Optional.of(credencialB));
        when(credencialMapper.toResponse(credencialB)).thenReturn(response);

        assertSame(response, credencialService.obtenerPorId(credencialBId));
    }

    @Test
    void misCredencialesDevuelveUnicamenteLasPropias() {
        autenticarUsuario(usuarioA);
        CredencialResponse response = respuesta(credencialAId);
        when(credencialRepository.findByUsuarioId(usuarioA.getId())).thenReturn(List.of(credencialA));
        when(credencialMapper.toResponse(credencialA)).thenReturn(response);

        assertEquals(List.of(response), credencialService.listarMisCredenciales());
        verify(credencialRepository).findByUsuarioId(usuarioA.getId());
    }

    @Test
    void usuarioObtieneQrDeSuCredencial() {
        autenticarParticipante(usuarioA);
        byte[] esperado = {1, 2, 3};
        when(codigoQrRepository.findByCredencialIdAndCredencialUsuarioId(credencialAId, usuarioA.getId()))
                .thenReturn(Optional.of(qrA));
        when(codigoQrService.generarImagenQr(qrA.getContenido(), 300, 300)).thenReturn(esperado);

        assertArrayEquals(esperado, credencialService.obtenerQrImagen(credencialAId));
    }

    @Test
    void usuarioNoObtieneQrAjenoNiGeneraPng() {
        autenticarParticipante(usuarioA);
        when(codigoQrRepository.findByCredencialIdAndCredencialUsuarioId(credencialBId, usuarioA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> credencialService.obtenerQrImagen(credencialBId));
        verify(codigoQrService, never()).generarImagenQr(any(), any(Integer.class), any(Integer.class));
    }

    @Test
    void organizadorNoObtieneQrDeEventoAjenoNiGeneraPng() {
        autenticarOrganizador(organizadorA);
        when(codigoQrRepository.findByCredencialIdAndCredencialEventoOrganizadorId(
                credencialBId, organizadorA.getId())).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> credencialService.obtenerQrImagen(credencialBId));
        verify(codigoQrService, never()).generarImagenQr(any(), any(Integer.class), any(Integer.class));
    }

    @Test
    void usuarioDescargaPdfDeSuCredencial() {
        autenticarParticipante(usuarioA);
        when(credencialRepository.findByIdAndUsuarioId(credencialAId, usuarioA.getId()))
                .thenReturn(Optional.of(credencialA));
        when(codigoQrRepository.findByCredencialId(credencialAId)).thenReturn(Optional.of(qrA));
        when(codigoQrService.generarImagenQr(qrA.getContenido(), 200, 200)).thenReturn(PNG_VALIDO);

        byte[] pdf = credencialService.descargarPdf(credencialAId);

        assertFalse(pdf.length == 0);
        verify(codigoQrService).generarImagenQr(qrA.getContenido(), 200, 200);
    }

    @Test
    void usuarioNoDescargaPdfAjenoNiGeneraDocumento() {
        autenticarParticipante(usuarioA);
        when(credencialRepository.findByIdAndUsuarioId(credencialBId, usuarioA.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> credencialService.descargarPdf(credencialBId));

        verify(codigoQrRepository, never()).findByCredencialId(credencialBId);
        verify(codigoQrService, never()).generarImagenQr(any(), any(Integer.class), any(Integer.class));
    }

    private void autenticarParticipante(Usuario usuario) {
        lenient().when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        lenient().when(usuarioAutenticadoService.tieneRol("ORGANIZADOR")).thenReturn(false);
        autenticarUsuario(usuario);
    }

    private void autenticarOrganizador(Usuario usuario) {
        lenient().when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        lenient().when(usuarioAutenticadoService.tieneRol("ORGANIZADOR")).thenReturn(true);
        autenticarUsuario(usuario);
    }

    private void autenticarAdministrador() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(true);
    }

    private void autenticarUsuario(Usuario usuario) {
        lenient().when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
    }

    private Usuario usuario() {
        Usuario usuario = Usuario.builder().nombres("Nombre").apellidos("Apellido").build();
        usuario.setId(UUID.randomUUID());
        return usuario;
    }

    private Evento evento(Usuario organizador) {
        Evento evento = Evento.builder().titulo("Evento").organizador(organizador).build();
        evento.setId(UUID.randomUUID());
        return evento;
    }

    private Inscripcion inscripcion(Usuario usuario, Evento evento) {
        Inscripcion inscripcion = Inscripcion.builder()
                .usuario(usuario)
                .evento(evento)
                .estado(EstadoInscripcion.CONFIRMADA)
                .build();
        inscripcion.setId(UUID.randomUUID());
        return inscripcion;
    }

    private Credencial credencial(Usuario usuario, Evento evento, Inscripcion inscripcion) {
        Credencial credencial = Credencial.builder()
                .usuario(usuario)
                .evento(evento)
                .inscripcion(inscripcion)
                .codigoParticipante("PART-12345678")
                .estado("ACTIVA")
                .build();
        credencial.setId(UUID.randomUUID());
        return credencial;
    }

    private CodigoQr qr(Credencial credencial) {
        CodigoQr qr = CodigoQr.builder()
                .credencial(credencial)
                .contenido(credencial.getId().toString())
                .activo(true)
                .build();
        qr.setId(UUID.randomUUID());
        return qr;
    }

    private CredencialResponse respuesta(UUID id) {
        return CredencialResponse.builder().id(id).build();
    }
}
