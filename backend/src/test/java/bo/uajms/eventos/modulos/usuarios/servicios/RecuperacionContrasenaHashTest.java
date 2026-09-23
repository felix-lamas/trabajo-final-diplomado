package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.seguridad.JwtService;
import bo.uajms.eventos.modulos.usuarios.dtos.RecuperarContrasenaRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.ResetContrasenaRequest;
import bo.uajms.eventos.modulos.usuarios.entidades.TokenRecuperacion;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.mappers.UsuarioMapper;
import bo.uajms.eventos.modulos.usuarios.repositorios.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuperacionContrasenaHashTest {
    @Mock UsuarioRepository usuarioRepository;
    @Mock RolRepository rolRepository;
    @Mock UsuarioRolRepository usuarioRolRepository;
    @Mock TokenRecuperacionRepository tokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;
    @Mock UserDetailsService userDetailsService;
    @Mock UsuarioMapper usuarioMapper;
    @Mock CorreoServicio correoServicio;
    @InjectMocks AutenticacionServicio service;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        usuario = Usuario.builder().correoElectronico("usuario@example.test").contrasena("hash-anterior").build();
        ReflectionTestUtils.setField(service, "resetPasswordUrl", "https://app.example.test/restablecer");
    }

    @Test
    void solicitudPersisteHashYEnviaSoloTokenPlano() {
        RecuperarContrasenaRequest request = new RecuperarContrasenaRequest();
        request.setCorreoElectronico("usuario@example.test");
        when(usuarioRepository.findByCorreoElectronicoIgnoreCase("usuario@example.test"))
                .thenReturn(Optional.of(usuario));
        when(tokenRepository.findByUsuarioIdAndUtilizadoFalse(usuario.getId())).thenReturn(java.util.List.of());
        when(tokenRepository.existsByToken(anyString())).thenReturn(false);
        ArgumentCaptor<TokenRecuperacion> entidad = ArgumentCaptor.forClass(TokenRecuperacion.class);
        ArgumentCaptor<String> enlace = ArgumentCaptor.forClass(String.class);

        service.solicitarRecuperacion(request);

        verify(tokenRepository).save(entidad.capture());
        verify(correoServicio).enviarRecuperacionContrasena(eq("usuario@example.test"), enlace.capture());
        String tokenPlano = enlace.getValue().substring(enlace.getValue().indexOf("token=") + 6);
        assertNotEquals(tokenPlano, entidad.getValue().getToken());
        assertEquals(hash(tokenPlano), entidad.getValue().getToken());
        assertEquals(64, entidad.getValue().getToken().length());
    }

    @Test
    void tokenValidoRestableceContrasena() {
        String plano = "token-plano-valido";
        TokenRecuperacion token = token(plano, false, LocalDateTime.now().plusMinutes(5));
        when(tokenRepository.findByToken(hash(plano))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NuevaSegura1!")).thenReturn("nuevo-hash");

        service.restablecerContrasena(request(plano));

        assertEquals("nuevo-hash", usuario.getContrasena());
        assertTrue(token.isUtilizado());
        verify(usuarioRepository).save(usuario);
        verify(tokenRepository).save(token);
    }

    @Test
    void tokenIncorrectoEsRechazado() {
        when(tokenRepository.findByToken(hash("incorrecto"))).thenReturn(Optional.empty());
        assertThrows(NegocioException.class, () -> service.restablecerContrasena(request("incorrecto")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void tokenExpiradoEsRechazado() {
        String plano = "expirado";
        when(tokenRepository.findByToken(hash(plano)))
                .thenReturn(Optional.of(token(plano, false, LocalDateTime.now().minusMinutes(1))));
        assertThrows(NegocioException.class, () -> service.restablecerContrasena(request(plano)));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void tokenUtilizadoEsRechazado() {
        String plano = "utilizado";
        when(tokenRepository.findByToken(hash(plano)))
                .thenReturn(Optional.of(token(plano, true, LocalDateTime.now().plusMinutes(5))));
        assertThrows(NegocioException.class, () -> service.restablecerContrasena(request(plano)));
        verify(usuarioRepository, never()).save(any());
    }

    private TokenRecuperacion token(String plano, boolean utilizado, LocalDateTime expiracion) {
        return TokenRecuperacion.builder().token(hash(plano)).usuario(usuario)
                .fechaExpiracion(expiracion).utilizado(utilizado).build();
    }

    private ResetContrasenaRequest request(String token) {
        ResetContrasenaRequest request = new ResetContrasenaRequest();
        request.setToken(token);
        request.setNuevaContrasena("NuevaSegura1!");
        request.setConfirmacion("NuevaSegura1!");
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
