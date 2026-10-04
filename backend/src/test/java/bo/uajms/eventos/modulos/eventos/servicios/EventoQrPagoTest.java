package bo.uajms.eventos.modulos.eventos.servicios;

import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.categorias.repositorios.CategoriaEventoRepository;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.mappers.EventoMapper;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.pagos.servicios.AlmacenamientoArchivos;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventoQrPagoTest {
    @Mock EventoRepository eventoRepository;
    @Mock CategoriaEventoRepository categoriaRepository;
    @Mock EventoMapper eventoMapper;
    @Mock UsuarioAutenticadoService usuarioAutenticadoService;
    @Mock AlmacenamientoArchivos storage;
    @InjectMocks EventoService service;

    private UUID eventoId;
    private UUID ownerId;
    private Evento evento;
    private Usuario owner;

    @BeforeEach
    void setup() {
        eventoId = UUID.randomUUID(); ownerId = UUID.randomUUID();
        owner = Usuario.builder().correoElectronico("owner@example.test").nombres("Owner").apellidos("Test")
                .estadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.APROBADA).build();
        owner.setId(ownerId);
        evento = Evento.builder().organizador(owner).tipoInscripcion(TipoInscripcion.PAGO)
                .estado(EstadoEvento.BORRADOR).build();
        evento.setId(eventoId);
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(usuarioAutenticadoService.tieneRol("ORGANIZADOR")).thenReturn(true);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(owner);
        when(eventoRepository.findByIdForUpdate(eventoId)).thenReturn(Optional.of(evento));
        when(eventoRepository.saveAndFlush(any(Evento.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void cargaPngValidoConKeyGeneradaPorBackendYResolucionOriginal() throws Exception {
        byte[] png = png();
        service.subirQrPago(eventoId, new MockMultipartFile("archivo", "cuenta.png", "image/png", png));
        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(storage).guardar(key.capture(), eq(png), eq("image/png"));
        assertTrue(AlmacenamientoArchivos.esClaveQrPago(key.getValue()));
        assertEquals(key.getValue(), evento.getQrPagoStorageKey());
        assertNull(evento.getQrPagoUrl());
        verify(eventoRepository).saveAndFlush(evento);
    }

    @Test
    void aceptaExtensionJpegConMimeRealJpeg() throws Exception {
        service.subirQrPago(eventoId, new MockMultipartFile("archivo", "cuenta.jpeg", "image/jpeg", jpeg()));
        verify(storage).guardar(anyString(), any(byte[].class), eq("image/jpeg"));
        assertTrue(evento.getQrPagoStorageKey().endsWith(".jpeg"));
    }

    @Test
    void rechazaMimeIncompatibleAntesDeAlmacenar() throws Exception {
        assertThrows(RuntimeException.class, () -> service.subirQrPago(eventoId,
                new MockMultipartFile("archivo", "cuenta.png", "image/jpeg", png())));
        verifyNoInteractions(storage);
    }

    @Test
    void rechazaContenidoQueNoEsImagen() {
        assertThrows(RuntimeException.class, () -> service.subirQrPago(eventoId,
                new MockMultipartFile("archivo", "cuenta.png", "image/png", "texto".getBytes())));
        verifyNoInteractions(storage);
    }

    @Test
    void rechazaMasDeCincoMb() {
        byte[] bytes = new byte[5 * 1024 * 1024 + 1];
        assertThrows(RuntimeException.class, () -> service.subirQrPago(eventoId,
                new MockMultipartFile("archivo", "cuenta.png", "image/png", bytes)));
        verifyNoInteractions(storage);
    }

    @Test
    void ownershipSeCompruebaAntesDeContactarStorage() {
        Usuario ajeno = Usuario.builder().nombres("Otro").apellidos("Organizador")
                .estadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.APROBADA).build();
        ajeno.setId(UUID.randomUUID());
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(ajeno);
        assertThrows(AccessDeniedException.class, () -> service.subirQrPago(eventoId,
                new MockMultipartFile("archivo", "cuenta.png", "image/png", new byte[]{1})));
        verifyNoInteractions(storage);
    }

    @Test
    void administradorPuedeSubirQrSegunSuAlcance() throws Exception {
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(true);
        service.subirQrPago(eventoId, new MockMultipartFile("archivo", "cuenta.jpg", "image/jpeg", jpeg()));
        verify(storage).guardar(startsWith("eventos/" + eventoId + "/qr-pago/"), any(byte[].class), eq("image/jpeg"));
    }

    @Test
    void usuarioNoPuedeSubirQr() throws Exception {
        when(usuarioAutenticadoService.tieneRol("ORGANIZADOR")).thenReturn(false);
        when(usuarioAutenticadoService.tieneRol("ADMINISTRADOR")).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.subirQrPago(eventoId,
                new MockMultipartFile("archivo", "cuenta.png", "image/png", png())));
        verifyNoInteractions(storage);
    }

    @Test
    void eventoPublicadoNoAdmiteReemplazoDeQr() throws Exception {
        evento.setEstado(EstadoEvento.PUBLICADO);
        assertThrows(RuntimeException.class, () -> service.subirQrPago(eventoId,
                new MockMultipartFile("archivo", "cuenta.png", "image/png", png())));
        verifyNoInteractions(storage);
    }

    @Test
    void reemplazoConservaQrAnteriorHastaCommit() throws Exception {
        String anterior = "eventos/" + eventoId + "/qr-pago/" + UUID.randomUUID() + ".png";
        evento.setQrPagoStorageKey(anterior);
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.subirQrPago(eventoId, new MockMultipartFile("archivo", "nuevo.png", "image/png", png()));
            verify(storage, never()).eliminar(anterior);
            TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCommit());
            verify(storage).eliminar(anterior);
            TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCompletion(0));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void falloPersistenciaCompensaElNuevoObjeto() throws Exception {
        when(eventoRepository.saveAndFlush(any(Evento.class))).thenThrow(new IllegalStateException("db"));
        assertThrows(IllegalStateException.class, () -> service.subirQrPago(eventoId,
                new MockMultipartFile("archivo", "nuevo.png", "image/png", png())));
        verify(storage).guardar(anyString(), any(byte[].class), eq("image/png"));
        verify(storage).eliminar(argThat(AlmacenamientoArchivos::esClaveQrPago));
    }

    @Test
    void lecturaDePublicadoDescargaSoloLaKeyYMarcaCachePublico() throws Exception {
        evento.setEstado(EstadoEvento.PUBLICADO);
        String key = "eventos/" + eventoId + "/qr-pago/" + UUID.randomUUID() + ".png";
        evento.setQrPagoStorageKey(key);
        when(usuarioAutenticadoService.tieneRol("ORGANIZADOR")).thenReturn(false);
        when(eventoRepository.findByIdAndEstado(eventoId, EstadoEvento.PUBLICADO)).thenReturn(Optional.of(evento));
        when(storage.descargar(key)).thenReturn(Optional.of(new ByteArrayResource(png())));
        EventoService.QrPagoArchivo result = service.obtenerQrPago(eventoId);
        assertTrue(result.publico());
        assertEquals("image/png", result.tipoContenido().toString());
        verify(storage).descargar(key);
    }

    @Test
    void eliminarQrLimpiaReferenciaYEliminaObjetoDespuesDeCommit() {
        String key = "eventos/" + eventoId + "/qr-pago/" + UUID.randomUUID() + ".jpg";
        evento.setQrPagoStorageKey(key);
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.eliminarQrPago(eventoId);
            assertNull(evento.getQrPagoStorageKey());
            verify(storage, never()).eliminar(key);
            TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCommit());
            verify(storage).eliminar(key);
            TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCompletion(0));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void eventoGratuitoNoAdmiteQr() throws Exception {
        evento.setTipoInscripcion(TipoInscripcion.GRATUITO);
        assertThrows(RuntimeException.class, () -> service.subirQrPago(eventoId,
                new MockMultipartFile("archivo", "cuenta.png", "image/png", png())));
        verifyNoInteractions(storage);
    }

    @Test
    void keyQrYComprobanteSonNamespacesSeparados() {
        assertTrue(AlmacenamientoArchivos.esClavePermitida("comprobantes/" + UUID.randomUUID() + "/" + UUID.randomUUID() + ".pdf"));
        assertTrue(AlmacenamientoArchivos.esClaveQrPago("eventos/" + UUID.randomUUID() + "/qr-pago/" + UUID.randomUUID() + ".jpeg"));
        assertFalse(AlmacenamientoArchivos.esClavePermitida("eventos/../secret.png"));
        assertFalse(AlmacenamientoArchivos.esClaveQrPago("eventos/" + UUID.randomUUID() + "/qr-pago/" + UUID.randomUUID() + ".pdf"));
    }

    private byte[] png() throws Exception {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private byte[] jpeg() throws Exception {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        return output.toByteArray();
    }
}
