package bo.uajms.eventos.modulos.pagos.servicios;

import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import bo.uajms.eventos.modulos.pagos.mappers.PagoMapper;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoComprobanteDescargaTest {
    @Mock PagoRepository pagoRepository;
    @Mock InscripcionRepository inscripcionRepository;
    @Mock PagoMapper pagoMapper;
    @Mock ArchivoSeguroServicio archivoSeguroServicio;
    @Mock UsuarioAutenticadoService auth;
    @Mock Clock clock;
    @Mock Resource resource;
    @InjectMocks PagoService service;

    private UUID pagoId;
    private Usuario usuario;
    private Usuario organizador;
    private Pago pago;

    @BeforeEach
    void setup() {
        pagoId = UUID.randomUUID();
        usuario = usuario();
        organizador = usuario();
        Evento evento = Evento.builder().organizador(organizador).build();
        evento.setId(UUID.randomUUID());
        Inscripcion inscripcion = Inscripcion.builder().usuario(usuario).evento(evento).build();
        inscripcion.setId(UUID.randomUUID());
        pago = Pago.builder().inscripcion(inscripcion).comprobanteUrl("comprobantes/interno.pdf")
                .comprobanteTipoContenido("application/pdf").build();
        pago.setId(pagoId);
        lenient().when(archivoSeguroServicio.cargarArchivo("comprobantes/interno.pdf")).thenReturn(resource);
    }

    @Test
    void usuarioPropietarioDescargaComprobante() {
        autenticarUsuario();
        when(pagoRepository.findByIdAndInscripcionUsuarioId(pagoId, usuario.getId())).thenReturn(Optional.of(pago));

        PagoService.ComprobanteDescarga descarga = service.descargarComprobante(pagoId);

        assertSame(resource, descarga.recurso());
        assertEquals("application/pdf", descarga.tipoContenido());
        assertFalse(descarga.nombreArchivo().contains("interno"));
    }

    @Test
    void organizadorPropietarioDescargaComprobante() {
        when(auth.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(auth.tieneRol("ORGANIZADOR")).thenReturn(true);
        when(auth.obtenerUsuario()).thenReturn(organizador);
        when(pagoRepository.findByIdAndInscripcionEventoOrganizadorId(pagoId, organizador.getId()))
                .thenReturn(Optional.of(pago));

        assertSame(resource, service.descargarComprobante(pagoId).recurso());
    }

    @Test
    void administradorDescargaComprobanteGlobal() {
        when(auth.tieneRol("ADMINISTRADOR")).thenReturn(true);
        when(pagoRepository.findById(pagoId)).thenReturn(Optional.of(pago));

        assertSame(resource, service.descargarComprobante(pagoId).recurso());
    }

    @Test
    void usuarioAjenoNoResuelveArchivo() {
        autenticarUsuario();
        when(pagoRepository.findByIdAndInscripcionUsuarioId(pagoId, usuario.getId())).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.descargarComprobante(pagoId));
        verify(archivoSeguroServicio, never()).cargarArchivo(anyString());
    }

    @Test
    void organizadorAjenoNoResuelveArchivo() {
        when(auth.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(auth.tieneRol("ORGANIZADOR")).thenReturn(true);
        when(auth.obtenerUsuario()).thenReturn(organizador);
        when(pagoRepository.findByIdAndInscripcionEventoOrganizadorId(pagoId, organizador.getId()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.descargarComprobante(pagoId));
        verify(archivoSeguroServicio, never()).cargarArchivo(anyString());
    }

    @Test
    void comprobanteInexistenteDevuelveNoEncontrado() {
        autenticarUsuario();
        pago.setComprobanteUrl(null);
        when(pagoRepository.findByIdAndInscripcionUsuarioId(pagoId, usuario.getId())).thenReturn(Optional.of(pago));

        assertThrows(RecursoNoEncontradoException.class, () -> service.descargarComprobante(pagoId));
        verify(archivoSeguroServicio, never()).cargarArchivo(anyString());
    }

    private void autenticarUsuario() {
        when(auth.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(auth.tieneRol("ORGANIZADOR")).thenReturn(false);
        when(auth.tieneRol("USUARIO")).thenReturn(true);
        when(auth.obtenerUsuario()).thenReturn(usuario);
    }

    private Usuario usuario() {
        Usuario value = Usuario.builder().build();
        value.setId(UUID.randomUUID());
        return value;
    }
}
