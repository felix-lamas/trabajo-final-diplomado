package bo.uajms.eventos.modulos.credenciales.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.modulos.codigo_qr.entidades.CodigoQr;
import bo.uajms.eventos.modulos.codigo_qr.repositorios.CodigoQrRepository;
import bo.uajms.eventos.modulos.codigo_qr.servicios.CodigoQrService;
import bo.uajms.eventos.modulos.credenciales.dtos.CredencialResponse;
import bo.uajms.eventos.modulos.credenciales.entidades.Credencial;
import bo.uajms.eventos.modulos.credenciales.mappers.CredencialMapper;
import bo.uajms.eventos.modulos.credenciales.repositorios.CredencialRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CredencialService {

    private final CredencialRepository credencialRepository;
    private final CodigoQrRepository codigoQrRepository;
    private final InscripcionRepository inscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CodigoQrService codigoQrService;
    private final CredencialMapper credencialMapper;

    @Transactional
    public CredencialResponse generarCredencial(UUID inscripcionId) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción", inscripcionId));

        if (inscripcion.getEstado() != EstadoInscripcion.CONFIRMADA) {
            throw new NegocioException("Solo se pueden generar credenciales para inscripciones CONFIRMADAS");
        }

        if (credencialRepository.existsByInscripcionId(inscripcionId)) {
            throw new NegocioException("Ya existe una credencial para esta inscripción");
        }

        String codigoParticipante = "PART-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Credencial credencial = Credencial.builder()
                .usuario(inscripcion.getUsuario())
                .evento(inscripcion.getEvento())
                .inscripcion(inscripcion)
                .codigoParticipante(codigoParticipante)
                .fechaGeneracion(LocalDateTime.now())
                .estado("ACTIVA")
                .build();

        credencial = credencialRepository.save(credencial);

        // Generar QR automáticamente
        String contenidoQr = credencial.getId().toString();
        CodigoQr codigoQr = CodigoQr.builder()
                .credencial(credencial)
                .contenido(contenidoQr)
                .fechaGeneracion(LocalDateTime.now())
                .activo(true)
                .build();

        codigoQrRepository.save(codigoQr);

        return credencialMapper.toResponse(credencial);
    }

    @Transactional(readOnly = true)
    public CredencialResponse obtenerPorId(UUID id) {
        return credencialRepository.findById(id)
                .map(credencialMapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Credencial", id));
    }

    @Transactional(readOnly = true)
    public List<CredencialResponse> listarMisCredenciales() {
        Usuario usuario = obtenerUsuarioAutenticado();
        return credencialRepository.findByUsuarioId(usuario.getId()).stream()
                .map(credencialMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] obtenerQrImagen(UUID credencialId) {
        CodigoQr codigoQr = codigoQrRepository.findByCredencialId(credencialId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Código QR", credencialId));
        
        return codigoQrService.generarImagenQr(codigoQr.getContenido(), 300, 300);
    }

    @Transactional(readOnly = true)
    public byte[] descargarPdf(UUID id) {
        Credencial credencial = credencialRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Credencial", id));

        CodigoQr codigoQr = codigoQrRepository.findByCredencialId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Código QR", id));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("CREDENCIAL DE PARTICIPANTE").setBold().setFontSize(20));
            document.add(new Paragraph("Evento: " + credencial.getEvento().getTitulo()));
            document.add(new Paragraph("Participante: " + credencial.getUsuario().getNombres() + " " + credencial.getUsuario().getApellidos()));
            document.add(new Paragraph("Código: " + credencial.getCodigoParticipante()));
            
            byte[] qrBytes = codigoQrService.generarImagenQr(codigoQr.getContenido(), 200, 200);
            Image qrImage = new Image(ImageDataFactory.create(qrBytes));
            document.add(qrImage);

            document.add(new Paragraph("Presente este código para su ingreso al evento."));
            
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF de la credencial", e);
        }
    }

    private Usuario obtenerUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByCorreoElectronico(email)
                .orElseThrow(() -> new NegocioException("Usuario autenticado no encontrado"));
    }
}
