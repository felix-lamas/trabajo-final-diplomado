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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Certificados", description = "Generacion, consulta, descarga y verificacion de certificados")
@SecurityRequirement(name = "bearerAuth")
public class CertificadoController {

    private final CertificadoService certificadoService;

    @PostMapping("/certificados/generar/{inscripcionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Generar certificado",
            description = "Genera el certificado si se cumplen las reglas del evento, pago y asistencia. ORGANIZADOR solo puede operar sobre eventos propios.")
    @ApiResponse(responseCode = "404", description = "Inscripcion inexistente o fuera del alcance")
    public ResponseEntity<CertificadoResponse> generar(
            @Parameter(description = "Identificador de la inscripcion") @PathVariable UUID inscripcionId) {
        return ResponseEntity.ok(certificadoService.generarCertificado(inscripcionId));
    }

    @GetMapping("/certificados/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    @Operation(summary = "Obtener certificado",
            description = "USUARIO solo consulta certificados propios; ORGANIZADOR, certificados de eventos propios; ADMINISTRADOR, cualquier certificado.")
    @ApiResponse(responseCode = "404", description = "Certificado inexistente o fuera del alcance")
    public ResponseEntity<CertificadoResponse> obtenerPorId(
            @Parameter(description = "Identificador del certificado") @PathVariable UUID id) {
        return ResponseEntity.ok(certificadoService.obtenerPorId(id));
    }

    @GetMapping("/certificados/mis-certificados")
    @PreAuthorize("hasRole('USUARIO')")
    @Operation(summary = "Listar mis certificados",
            description = "Devuelve exclusivamente certificados del usuario autenticado.")
    public ResponseEntity<List<CertificadoResponse>> misCertificados() {
        return ResponseEntity.ok(certificadoService.listarMisCertificados());
    }

    @GetMapping("/eventos/{eventoId}/certificados")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Listar certificados de un evento",
            description = "ORGANIZADOR solo puede consultar certificados de eventos propios.")
    @ApiResponse(responseCode = "404", description = "Evento inexistente o fuera del alcance")
    public ResponseEntity<List<CertificadoResponse>> certificadosPorEvento(
            @Parameter(description = "Identificador del evento") @PathVariable UUID eventoId) {
        return ResponseEntity.ok(certificadoService.listarPorEvento(eventoId));
    }

    @GetMapping("/certificados/{id}/descargar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    @Operation(summary = "Descargar certificado PDF",
            description = "Descarga un PDF real. USUARIO solo descarga certificados propios; ORGANIZADOR, los de eventos propios.")
    @ApiResponse(responseCode = "200", description = "Documento PDF del certificado",
            content = @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "404", description = "Certificado inexistente o fuera del alcance")
    public ResponseEntity<byte[]> descargar(
            @Parameter(description = "Identificador del certificado") @PathVariable UUID id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificado-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(certificadoService.descargarPdf(id));
    }

    @GetMapping("/certificados/verificar/{codigo}")
    @SecurityRequirements
    @Operation(summary = "Verificar certificado publicamente",
            description = "Comprueba por codigo unico si el certificado existe y es valido, sin exponer datos sensibles adicionales.")
    @ApiResponse(responseCode = "200", description = "Resultado publico de verificacion; un codigo no registrado devuelve valido=false")
    public ResponseEntity<VerificacionCertificadoResponse> verificarPublico(
            @Parameter(description = "Codigo publico del certificado", example = "UAJMS-1234567890ABCDEF")
            @PathVariable String codigo) {
        return ResponseEntity.ok(certificadoService.verificarCertificadoPublico(codigo));
    }

}
