package bo.uajms.eventos.modulos.codigo_qr.servicios;

import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.codigo_qr.entidades.CodigoQr;
import bo.uajms.eventos.modulos.codigo_qr.repositorios.CodigoQrRepository;
import bo.uajms.eventos.modulos.credenciales.entidades.Credencial;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodigoQrServiceAuthorizationTest {

    @Mock private CodigoQrRepository codigoQrRepository;
    @Mock private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private CodigoQrService codigoQrService;

    private Usuario organizadorA;
    private UUID credencialAId;
    private UUID credencialBId;
    private CodigoQr qrA;

    @BeforeEach
    void configurarEscenario() {
        organizadorA = usuario();
        Usuario organizadorB = usuario();
        Credencial credencialA = credencial(evento(organizadorA));
        Credencial credencialB = credencial(evento(organizadorB));
        credencialAId = credencialA.getId();
        credencialBId = credencialB.getId();
        qrA = CodigoQr.builder().credencial(credencialA).contenido(credencialAId.toString()).build();
        qrA.setId(UUID.randomUUID());
    }

    @Test
    void organizadorValidaQrDeSuEvento() {
        autenticarOrganizadorA();
        when(codigoQrRepository.findByCredencialIdAndCredencialEventoOrganizadorId(
                credencialAId, organizadorA.getId())).thenReturn(Optional.of(qrA));

        assertSame(qrA, codigoQrService.validarContenidoVisible(credencialAId.toString()));
    }

    @Test
    void organizadorNoValidaQrDeEventoAjeno() {
        autenticarOrganizadorA();
        when(codigoQrRepository.findByCredencialIdAndCredencialEventoOrganizadorId(
                credencialBId, organizadorA.getId())).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> codigoQrService.validarContenidoVisible(credencialBId.toString()));
        verify(codigoQrRepository, never()).findByCredencialId(credencialBId);
    }

    @Test
    void administradorValidaQrGlobalmente() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(true);
        when(codigoQrRepository.findByCredencialId(credencialAId)).thenReturn(Optional.of(qrA));

        assertSame(qrA, codigoQrService.validarContenidoVisible(credencialAId.toString()));
    }

    @Test
    void qrInexistenteDevuelveNoEncontrado() {
        autenticarOrganizadorA();
        when(codigoQrRepository.findByCredencialIdAndCredencialEventoOrganizadorId(
                credencialAId, organizadorA.getId())).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> codigoQrService.validarContenidoVisible(credencialAId.toString()));
    }

    @Test
    void contenidoQrInvalidoSeRespondeDeFormaControlada() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> codigoQrService.validarContenidoVisible("contenido-no-uuid"));
        verify(codigoQrRepository, never()).findByCredencialId(org.mockito.ArgumentMatchers.any());
    }

    private void autenticarOrganizadorA() {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(organizadorA);
    }

    private Usuario usuario() {
        Usuario usuario = Usuario.builder().build();
        usuario.setId(UUID.randomUUID());
        return usuario;
    }

    private Evento evento(Usuario organizador) {
        Evento evento = Evento.builder().organizador(organizador).build();
        evento.setId(UUID.randomUUID());
        return evento;
    }

    private Credencial credencial(Evento evento) {
        Credencial credencial = Credencial.builder().evento(evento).build();
        credencial.setId(UUID.randomUUID());
        return credencial;
    }
}
