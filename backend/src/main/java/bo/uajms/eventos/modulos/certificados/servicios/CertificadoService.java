package bo.uajms.eventos.modulos.certificados.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.certificados.dtos.*;
import bo.uajms.eventos.modulos.certificados.entidades.Certificado;
import bo.uajms.eventos.modulos.certificados.repositorios.CertificadoRepository;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificadoService {

    private final CertificadoRepository certificadoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public CertificadoResponse generarCertificado(UUID inscripcionId) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada"));

        Evento evento = inscripcion.getEvento();

        // Regla 1: Solo eventos finalizados
        if (evento.getEstado() != EstadoEvento.FINALIZADO) {
            throw new NegocioException("No se pueden emitir certificados para un evento que no esté FINALIZADO.");
        }

        // Regla 2: Unicidad por inscripción
        if (certificadoRepository.existsByInscripcionId(inscripcionId)) {
            throw new NegocioException("Ya existe un certificado generado para esta inscripción.");
        }

        // Regla 3: Validar asistencia (mínimo el % requerido o tiene al menos un registro de asistencia en este bloque simplificado)
        boolean tieneAsistencia = asistenciaRepository.existsByInscripcionIdAndFechaEliminacionIsNull(inscripcionId);
        if (!tieneAsistencia) {
            throw new NegocioException("El participante no cumple con el requisito mínimo de asistencia para recibir la certificación.");
        }

        // Generar código único correlativo
        String codigoCertificado = "UAJMS-CERT-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Certificado certificado = Certificado.builder()
                .usuario(inscripcion.getUsuario())
                .evento(evento)
                .inscripcion(inscripcion)
                .codigoCertificado(codigoCertificado)
                .fechaEmision(LocalDateTime.now())
                .urlVerificacion("http://localhost:4200/publico/verificacion/" + codigoCertificado)
                .archivoPdfUrl("https://storage.uajms.edu.bo/certificados/" + codigoCertificado + ".pdf")
                .estado(Certificado.EstadoCertificado.GENERADO)
                .build();

        certificadoRepository.save(certificado);
        return mapearACertificadoResponse(certificado);
    }

    public CertificadoResponse obtenerPorId(UUID id) {
        Certificado c = certificadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Certificado no encontrado"));
        return mapearACertificadoResponse(c);
    }

    @Transactional
    public CertificadoResponse descargarCertificado(UUID id) {
        Certificado c = certificadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Certificado no encontrado"));
        
        if (c.getEstado() == Certificado.EstadoCertificado.GENERADO) {
            c.setEstado(Certificado.EstadoCertificado.DESCARGADO);
            certificadoRepository.save(c);
        }
        return mapearACertificadoResponse(c);
    }

    public List<CertificadoResponse> listarMisCertificados() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCorreoElectronico(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario actual no encontrado"));

        return certificadoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(this::mapearACertificadoResponse)
                .collect(Collectors.toList());
    }

    public VerificacionCertificadoResponse verificarCertificadoPúblico(String codigo) {
        return certificadoRepository.findByCodigoCertificado(codigo)
                .map(c -> {
                    if (c.getEstado() == Certificado.EstadoCertificado.ANULADO) {
                        return VerificacionCertificadoResponse.builder()
                                .valido(false)
                                .mensaje("ALERTA: El certificado con este código ha sido ANULADO oficialmente por la institución.")
                                .codigoCertificado(codigo)
                                .estado("ANULADO")
                                .build();
                    }
                    Usuario u = c.getUsuario();
                    return VerificacionCertificadoResponse.builder()
                            .valido(true)
                            .mensaje("Certificado VÁLIDO y autenticado por la Universidad Autónoma Juan Misael Saracho.")
                            .nombreCompleto(u.getNombres() + " " + u.getApellidos())
                            .ci(u.getCi())
                            .evento(c.getEvento().getTitulo())
                            .cargaHoraria(c.getEvento().getCargaHoraria())
                            .fechaEmision(c.getFechaEmision())
                            .codigoCertificado(c.getCodigoCertificado())
                            .estado(c.getEstado().name())
                            .build();
                })
                .orElse(VerificacionCertificadoResponse.builder()
                        .valido(false)
                        .mensaje("ERROR: No se encontró ningún certificado registrado bajo el código proporcionado.")
                        .codigoCertificado(codigo)
                        .estado("NO_REGISTRADO")
                        .build());
    }

    private CertificadoResponse mapearACertificadoResponse(Certificado c) {
        Usuario u = c.getUsuario();
        return CertificadoResponse.builder()
                .id(c.getId())
                .nombreCompleto(u.getNombres() + " " + u.getApellidos())
                .ci(u.getCi())
                .evento(c.getEvento().getTitulo())
                .cargaHoraria(c.getEvento().getCargaHoraria())
                .codigoCertificado(c.getCodigoCertificado())
                .fechaEmision(c.getFechaEmision())
                .urlVerificacion(c.getUrlVerificacion())
                .estado(c.getEstado().name())
                .archivoPdfUrl(c.getArchivoPdfUrl())
                .build();
    }
}
