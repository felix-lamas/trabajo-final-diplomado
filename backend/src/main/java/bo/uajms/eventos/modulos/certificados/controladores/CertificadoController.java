package bo.uajms.eventos.modulos.certificados.controladores;

import bo.uajms.eventos.modulos.certificados.dtos.*;
import bo.uajms.eventos.modulos.certificados.servicios.CertificadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'PARTICIPANTE')")
    public ResponseEntity<CertificadoResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(certificadoService.obtenerPorId(id));
    }

    @GetMapping("/certificados/mis-certificados")
    @PreAuthorize("hasRole('PARTICIPANTE')")
    public ResponseEntity<List<CertificadoResponse>> misCertificados() {
        return ResponseEntity.ok(certificadoService.listarMisCertificados());
    }

    @GetMapping("/certificados/{id}/descargar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'PARTICIPANTE')")
    public ResponseEntity<CertificadoResponse> descargar(@PathVariable UUID id) {
        return ResponseEntity.ok(certificadoService.descargarCertificado(id));
    }

    // Ruta de Validación Pública accesible SIN autenticación
    @GetMapping("/verificacion-certificados/{codigo}")
    public ResponseEntity<VerificacionCertificadoResponse> verificarPúblico(@PathVariable String codigo) {
        return ResponseEntity.ok(certificadoService.verificarCertificadoPúblico(codigo));
    }
}
