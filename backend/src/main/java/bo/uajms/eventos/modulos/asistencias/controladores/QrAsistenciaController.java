package bo.uajms.eventos.modulos.asistencias.controladores;

import bo.uajms.eventos.modulos.asistencias.dtos.QrAsistenciaResponse;
import bo.uajms.eventos.modulos.asistencias.servicios.QrAsistenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sesiones/{sesionId}/qr")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR','ORGANIZADOR')")
@Tag(name = "QR de asistencia", description = "Emision y consulta de QR temporales por sesion")
@SecurityRequirement(name = "bearerAuth")
public class QrAsistenciaController {
    private final QrAsistenciaService qrService;

    @GetMapping
    @Operation(summary = "Obtener QR activo",
            description = "El ADMINISTRADOR consulta cualquier sesion; el ORGANIZADOR solo sesiones de eventos propios.")
    @ApiResponse(responseCode = "404", description = "Sesion o QR activo inexistente o fuera del alcance")
    public ResponseEntity<QrAsistenciaResponse> obtenerActivo(
            @Parameter(description = "Identificador de la sesion") @PathVariable UUID sesionId) {
        return ResponseEntity.ok(qrService.obtenerActivo(sesionId));
    }

    @PostMapping("/generar")
    @Operation(summary = "Generar QR temporal",
            description = "Genera un QR de asistencia con vigencia de dos minutos. El ORGANIZADOR solo puede operar sobre eventos propios.")
    @ApiResponse(responseCode = "201", description = "QR temporal generado", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "404", description = "Sesion inexistente o fuera del alcance")
    public ResponseEntity<QrAsistenciaResponse> generar(
            @Parameter(description = "Identificador de la sesion") @PathVariable UUID sesionId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(qrService.generar(sesionId));
    }
}
