package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.seguridad.JwtPropiedades;
import bo.uajms.eventos.modulos.usuarios.entidades.SesionUsuario;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.repositorios.SesionUsuarioRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SesionUsuarioServicioTest {
    private SesionUsuarioRepository repository;
    private SesionUsuarioServicio servicio;
    private Usuario usuario;

    @BeforeEach
    void configurar() {
        repository = mock(SesionUsuarioRepository.class);
        JwtPropiedades propiedades = new JwtPropiedades();
        propiedades.setExpiracionMs(86_400_000L);
        servicio = new SesionUsuarioServicio(repository, propiedades);
        usuario = Usuario.builder().correoElectronico("usuario@example.test").build();
        ReflectionTestUtils.setField(usuario, "id", UUID.randomUUID());
    }

    @Test
    void nuevoLoginRevocaSesionAnteriorYCreaUnaNueva() {
        SesionUsuario anterior = sesion(LocalDateTime.now().plusHours(1), null);
        when(repository.findByUsuarioIdAndFechaRevocacionIsNull(usuario.getId())).thenReturn(List.of(anterior));
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            SesionUsuario nueva = invocation.getArgument(0);
            ReflectionTestUtils.setField(nueva, "id", UUID.randomUUID());
            return nueva;
        });

        SesionUsuario nueva = servicio.crearSesionUnica(usuario);

        assertNotNull(anterior.getFechaRevocacion());
        assertNotNull(nueva.getId());
        assertTrue(nueva.getFechaExpiracion().isAfter(LocalDateTime.now()));
    }

    @Test
    void sesionValidaPermitePeticion() {
        SesionUsuario sesion = sesion(LocalDateTime.now().plusMinutes(5), null);
        when(repository.findById(sesion.getId())).thenReturn(Optional.of(sesion));
        assertTrue(servicio.esSesionActiva(sesion.getId(), "USUARIO@example.test"));
    }

    @Test
    void sesionRevocadaRechazaPeticion() {
        SesionUsuario sesion = sesion(LocalDateTime.now().plusMinutes(5), LocalDateTime.now());
        when(repository.findById(sesion.getId())).thenReturn(Optional.of(sesion));
        assertFalse(servicio.esSesionActiva(sesion.getId(), usuario.getCorreoElectronico()));
    }

    @Test
    void sesionInexistenteRechazaPeticion() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertFalse(servicio.esSesionActiva(id, usuario.getCorreoElectronico()));
    }

    @Test
    void sesionExpiradaRechazaPeticion() {
        SesionUsuario sesion = sesion(LocalDateTime.now().minusSeconds(1), null);
        when(repository.findById(sesion.getId())).thenReturn(Optional.of(sesion));
        assertFalse(servicio.esSesionActiva(sesion.getId(), usuario.getCorreoElectronico()));
    }

    @Test
    void logoutRevocaSesionActual() {
        SesionUsuario sesion = sesion(LocalDateTime.now().plusMinutes(5), null);
        when(repository.findById(sesion.getId())).thenReturn(Optional.of(sesion));
        servicio.revocarSesion(sesion.getId(), usuario.getCorreoElectronico());
        assertNotNull(sesion.getFechaRevocacion());
    }

    @Test
    void bloqueoPesimistaDeUsuarioSerializaLoginsConcurrentes() throws Exception {
        Lock lock = UsuarioRepository.class
                .getMethod("findByCorreoElectronicoIgnoreCaseForUpdate", String.class)
                .getAnnotation(Lock.class);
        assertNotNull(lock);
        assertEquals(LockModeType.PESSIMISTIC_WRITE, lock.value());
    }

    private SesionUsuario sesion(LocalDateTime expiracion, LocalDateTime revocacion) {
        SesionUsuario sesion = SesionUsuario.builder().usuario(usuario)
                .fechaExpiracion(expiracion).fechaRevocacion(revocacion).build();
        ReflectionTestUtils.setField(sesion, "id", UUID.randomUUID());
        return sesion;
    }
}
