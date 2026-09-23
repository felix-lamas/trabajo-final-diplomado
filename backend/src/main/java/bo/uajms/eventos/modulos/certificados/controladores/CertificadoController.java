package bo.uajms.eventos.modulos.certificados.controladores;

import bo.uajms.eventos.modulos.certificados.dtos.*;
import bo.uajms.eventos.modulos.certificados.servicios.CertificadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api", "/api/v1"})
@RequiredArgsConstructor
public class CertificadoController {

    private final CertificadoService certificadoService;

    @PostMapping("/certificados/generar/{inscripcionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    public ResponseEntity<CertificadoResponse> generar(@PathVariable UUID inscripcionId) {
        return ResponseEntity.ok(certificadoService.generarCertificado(inscripcionId));
    }

    @GetMapping("/certificados/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    public ResponseEntity<CertificadoResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(certificadoService.obtenerPorId(id));
    }

    @GetMapping("/certificados/mis-certificados")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<List<CertificadoResponse>> misCertificados() {
        return ResponseEntity.ok(certificadoService.listarMisCertificados());
    }

    @GetMapping("/eventos/{eventoId}/certificados")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    public ResponseEntity<List<CertificadoResponse>> certificadosPorEvento(@PathVariable UUID eventoId) {
        return ResponseEntity.ok(certificadoService.listarPorEvento(eventoId));
    }

    @GetMapping("/certificados/{id}/descargar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    public ResponseEntity<byte[]> descargar(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificado-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(certificadoService.descargarPdf(id));
    }

    @GetMapping({"/certificados/verificar/{codigo}", "/verificacion-certificados/{codigo}"})
    @SecurityRequirements
    public ResponseEntity<VerificacionCertificadoResponse> verificarPublico(@PathVariable String codigo) {
        return ResponseEntity.ok(certificadoService.verificarCertificadoPublico(codigo));
    }
}
