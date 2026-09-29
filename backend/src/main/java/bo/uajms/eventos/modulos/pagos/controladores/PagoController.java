package bo.uajms.eventos.modulos.pagos.controladores;

import bo.uajms.eventos.modulos.pagos.dtos.PagoResponse;
import bo.uajms.eventos.modulos.pagos.dtos.RegistrarPagoRequest;
import bo.uajms.eventos.modulos.pagos.dtos.ValidarPagoRequest;
import bo.uajms.eventos.modulos.pagos.servicios.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Registro, comprobantes y validacion manual de pagos")
@SecurityRequirement(name = "bearerAuth")
public class PagoController {

    private final PagoService pagoService;

    @PostMapping(value = "/{id}/comprobante", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USUARIO')")
    @Operation(summary = "Subir comprobante de pago",
            description = "El USUARIO adjunta JPG, PNG o PDF a un pago propio que admita comprobante.")
    @ApiResponse(responseCode = "404", description = "Pago inexistente o fuera del alcance")
    public ResponseEntity<PagoResponse> subirComprobante(
            @Parameter(description = "Identificador del pago") @PathVariable UUID id,
            @Parameter(description = "Comprobante JPG, PNG o PDF; maximo 5 MB", required = true)
            @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(pagoService.subirComprobante(id, archivo));
    }

    @PostMapping
    @PreAuthorize("hasRole('USUARIO')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar pago",
            description = "Inicia el pago de una inscripcion propia correspondiente a un evento de pago.")
    @ApiResponse(responseCode = "201", description = "Pago registrado", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "404", description = "Inscripcion inexistente o fuera del alcance")
    public ResponseEntity<PagoResponse> registrarPago(@Valid @RequestBody RegistrarPagoRequest request) {
        return new ResponseEntity<>(pagoService.registrarPago(request), HttpStatus.CREATED);
    }

    @GetMapping("/mis-pagos")
    @PreAuthorize("hasRole('USUARIO')")
    @Operation(summary = "Listar mis pagos",
            description = "Devuelve exclusivamente los pagos del usuario autenticado.")
    public ResponseEntity<List<PagoResponse>> listarMisPagos() {
        return ResponseEntity.ok(pagoService.listarMisPagos());
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Listar pagos pendientes",
            description = "ORGANIZADOR recibe pagos pendientes de eventos propios; ADMINISTRADOR recibe el alcance global.")
    public ResponseEntity<List<PagoResponse>> listarPendientes() {
        return ResponseEntity.ok(pagoService.listarPendientes());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar todos los pagos",
            description = "Consulta global exclusiva de ADMINISTRADOR.")
    public ResponseEntity<List<PagoResponse>> listarTodos() {
        return ResponseEntity.ok(pagoService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    @Operation(summary = "Obtener pago",
            description = "USUARIO consulta pagos propios; ORGANIZADOR, pagos de eventos propios; ADMINISTRADOR, cualquier pago.")
    @ApiResponse(responseCode = "404", description = "Pago inexistente o fuera del alcance")
    public ResponseEntity<PagoResponse> obtenerPorId(
            @Parameter(description = "Identificador del pago") @PathVariable UUID id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    @GetMapping("/{id}/comprobante")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    @Operation(summary = "Descargar comprobante",
            description = "Devuelve el archivo real con su media type original. USUARIO requiere propiedad; ORGANIZADOR requiere ownership del evento.")
    @ApiResponse(responseCode = "200", description = "Archivo JPG, PNG o PDF",
            content = {
                    @Content(mediaType = "image/jpeg", schema = @Schema(type = "string", format = "binary")),
                    @Content(mediaType = "image/png", schema = @Schema(type = "string", format = "binary")),
                    @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary"))
            })
    @ApiResponse(responseCode = "404", description = "Pago, comprobante o recurso fuera del alcance")
    public ResponseEntity<Resource> descargarComprobante(
            @Parameter(description = "Identificador del pago") @PathVariable UUID id) {
        PagoService.ComprobanteDescarga descarga = pagoService.descargarComprobante(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(descarga.nombreArchivo(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(descarga.tipoContenido()))
                .body(descarga.recurso());
    }

    @PatchMapping("/{id}/validar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Aprobar pago",
            description = "Aprueba un pago pendiente. ORGANIZADOR solo puede validar pagos de eventos propios.")
    @ApiResponse(responseCode = "404", description = "Pago inexistente o fuera del alcance")
    public ResponseEntity<PagoResponse> validarPago(
            @Parameter(description = "Identificador del pago") @PathVariable UUID id,
            @Valid @RequestBody ValidarPagoRequest request) {
        return ResponseEntity.ok(pagoService.validarPago(id, request));
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Rechazar pago",
            description = "Rechaza un pago pendiente con observacion. ORGANIZADOR solo opera sobre eventos propios.")
    @ApiResponse(responseCode = "404", description = "Pago inexistente o fuera del alcance")
    public ResponseEntity<PagoResponse> rechazarPago(
            @Parameter(description = "Identificador del pago") @PathVariable UUID id,
            @Valid @RequestBody ValidarPagoRequest request) {
        return ResponseEntity.ok(pagoService.rechazarPago(id, request));
    }
}
