package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;
import bo.uajms.eventos.core.seguridad.JwtService;
import bo.uajms.eventos.modulos.usuarios.dtos.ReenviarVerificacionRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.VerificarCorreoRequest;
import bo.uajms.eventos.modulos.usuarios.entidades.TokenVerificacionCorreo;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.mappers.UsuarioMapper;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.TokenRecuperacionRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.TokenVerificacionCorreoRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificacionCorreoServicioTest {
    @Mock UsuarioRepository usuarioRepository;
    @Mock RolRepository rolRepository;
    @Mock UsuarioRolRepository usuarioRolRepository;
    @Mock TokenRecuperacionRepository tokenRecuperacionRepository;
    @Mock TokenVerificacionCorreoRepository tokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;
    @Mock UserDetailsService userDetailsService;
    @Mock UsuarioMapper usuarioMapper;
    @Mock CorreoServicio correoServicio;
    @Mock SesionUsuarioServicio sesionUsuarioServicio;
    @InjectMocks AutenticacionServicio servicio;

    private Usuario usuario;

    @BeforeEach
    void configurar() {
        usuario = Usuario.builder().correoElectronico("usuario@example.test").correoVerificado(false).build();
        ReflectionTestUtils.setField(usuario, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(servicio, "verifyEmailUrl", "https://app.example.test/verificar-correo");
    }

    @Test
    void tokenValidoVerificaCorreo() {
        TokenVerificacionCorreo token = token("valido", false, LocalDateTime.now().plusMinutes(5));
        when(tokenRepository.findByTokenHashForUpdate(hash("valido"))).thenReturn(Optional.of(token));

        servicio.verificarCorreo(request("valido"));

        assertTrue(usuario.isCorreoVerificado());
        assertTrue(token.isUtilizado());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void tokenExpiradoFalla() {
        when(tokenRepository.findByTokenHashForUpdate(hash("expirado")))
                .thenReturn(Optional.of(token("expirado", false, LocalDateTime.now().minusSeconds(1))));
        assertCodigo("EMAIL_VERIFICATION_TOKEN_EXPIRED", "expirado");
    }

    @Test
    void tokenUsadoFallaAlReutilizarse() {
        when(tokenRepository.findByTokenHashForUpdate(hash("usado")))
                .thenReturn(Optional.of(token("usado", true, LocalDateTime.now().plusMinutes(5))));
        assertCodigo("EMAIL_VERIFICATION_TOKEN_USED", "usado");
    }

    @Test
    void tokenIncorrectoFalla() {
        when(tokenRepository.findByTokenHashForUpdate(hash("incorrecto"))).thenReturn(Optional.empty());
        assertCodigo("EMAIL_VERIFICATION_TOKEN_INVALID", "incorrecto");
    }

    @Test
    void reenvioInvalidaAnteriorYEnviaNuevoSinPersistirTokenPlano() {
        ReenviarVerificacionRequest request = reenviar("usuario@example.test");
        TokenVerificacionCorreo anterior = token("anterior", false, LocalDateTime.now().plusMinutes(5));
        when(usuarioRepository.findByCorreoElectronicoIgnoreCaseForUpdate("usuario@example.test"))
                .thenReturn(Optional.of(usuario));
        when(tokenRepository.findByUsuarioIdAndUtilizadoFalse(usuario.getId())).thenReturn(List.of(anterior));
        when(tokenRepository.existsByTokenHash(anyString())).thenReturn(false);

        servicio.reenviarVerificacion(request);

        assertTrue(anterior.isUtilizado());
        verify(tokenRepository).save(argThat(token -> token.getTokenHash().length() == 64));
        verify(correoServicio).enviarVerificacionCorreo(eq("usuario@example.test"), contains("token="));
    }

    @Test
    void correoInexistenteNoPermiteEnumeracion() {
        ReenviarVerificacionRequest request = reenviar("inexistente@example.test");
        when(usuarioRepository.findByCorreoElectronicoIgnoreCaseForUpdate("inexistente@example.test"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> servicio.reenviarVerificacion(request));
        verifyNoInteractions(correoServicio);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void falloSmtpEnReenvioConservaRespuestaNoEnumerativaYNoPersisteToken() {
        ReenviarVerificacionRequest request = reenviar("usuario@example.test");
        when(usuarioRepository.findByCorreoElectronicoIgnoreCaseForUpdate("usuario@example.test"))
                .thenReturn(Optional.of(usuario));
        when(tokenRepository.existsByTokenHash(anyString())).thenReturn(false);
        doThrow(new ServicioNoDisponibleException("MAIL_SERVICE_UNAVAILABLE", "correo no disponible"))
                .when(correoServicio).enviarVerificacionCorreo(eq("usuario@example.test"), anyString());

        assertDoesNotThrow(() -> servicio.reenviarVerificacion(request));
        verify(tokenRepository, never()).save(any());
        verify(tokenRepository, never()).findByUsuarioIdAndUtilizadoFalse(any());
    }

    private void assertCodigo(String codigo, String token) {
        NegocioException error = assertThrows(NegocioException.class,
                () -> servicio.verificarCorreo(request(token)));
        assertEquals(codigo, error.getCodigo());
        assertFalse(usuario.isCorreoVerificado());
    }

    private TokenVerificacionCorreo token(String plano, boolean utilizado, LocalDateTime expiracion) {
        return TokenVerificacionCorreo.builder().tokenHash(hash(plano)).usuario(usuario)
                .fechaExpiracion(expiracion).utilizado(utilizado).build();
    }

    private VerificarCorreoRequest request(String token) {
        VerificarCorreoRequest request = new VerificarCorreoRequest();
        request.setToken(token);
        return request;
    }

    private ReenviarVerificacionRequest reenviar(String correo) {
        ReenviarVerificacionRequest request = new ReenviarVerificacionRequest();
        request.setCorreoElectronico(correo);
        return request;
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
}
