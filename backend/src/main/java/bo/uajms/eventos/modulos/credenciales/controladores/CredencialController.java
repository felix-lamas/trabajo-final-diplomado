package bo.uajms.eventos.modulos.credenciales.controladores;

import bo.uajms.eventos.modulos.credenciales.dtos.CredencialResponse;
import bo.uajms.eventos.modulos.credenciales.servicios.CredencialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Credenciales", description = "Gestión de credenciales y códigos QR de participantes")
@SecurityRequirement(name = "bearerAuth")
public class CredencialController {

    private final CredencialService credencialService;

    @PostMapping("/credenciales/generar/{inscripcionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generar una nueva credencial para una inscripción")
    public ResponseEntity<CredencialResponse> generar(@PathVariable UUID inscripcionId) {
        return new ResponseEntity<>(credencialService.generarCredencial(inscripcionId), HttpStatus.CREATED);
    }

    @GetMapping("/credenciales/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @Operation(summary = "Obtener detalle de una credencial")
    public ResponseEntity<CredencialResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(credencialService.obtenerPorId(id));
    }

    @GetMapping("/usuarios/mis-credenciales")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'PARTICIPANTE_EXTERNO', 'ADMINISTRADOR')")
    @Operation(summary = "Listar credenciales del usuario autenticado")
    public ResponseEntity<List<CredencialResponse>> listarMisCredenciales() {
        return ResponseEntity.ok(credencialService.listarMisCredenciales());
    }

    @GetMapping("/credenciales/{id}/qr")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @Operation(summary = "Obtener la imagen del código QR de una credencial")
    public ResponseEntity<byte[]> obtenerQr(@PathVariable UUID id) {
        byte[] qrImage = credencialService.obtenerQrImagen(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }

    @GetMapping("/credenciales/{id}/descargar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @Operation(summary = "Descargar credencial en formato PDF")
    public ResponseEntity<byte[]> descargar(@PathVariable UUID id) {
        byte[] pdf = credencialService.descargarPdf(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "credencial_" + id + ".pdf");
        
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
