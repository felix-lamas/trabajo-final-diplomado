package bo.uajms.eventos.modulos.pagos.controladores;

import bo.uajms.eventos.modulos.pagos.dtos.PagoResponse;
import bo.uajms.eventos.modulos.pagos.dtos.RegistrarPagoRequest;
import bo.uajms.eventos.modulos.pagos.dtos.ValidarPagoRequest;
import bo.uajms.eventos.modulos.pagos.servicios.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Gestión de pagos de inscripciones")
@SecurityRequirement(name = "bearerAuth")
public class PagoController {

    private final PagoService pagoService;

    @PostMapping("/{id}/comprobante")
    @PreAuthorize("hasRole('USUARIO')")
    @Operation(summary = "Subir comprobante de pago")
    public ResponseEntity<PagoResponse> subirComprobante(@PathVariable UUID id, @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(pagoService.subirComprobante(id, archivo));
    }

    @PostMapping
    @PreAuthorize("hasRole('USUARIO')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un pago")
    public ResponseEntity<PagoResponse> registrarPago(@Valid @RequestBody RegistrarPagoRequest request) {
        return new ResponseEntity<>(pagoService.registrarPago(request), HttpStatus.CREATED);
    }

    @GetMapping("/mis-pagos")
    @PreAuthorize("hasRole('USUARIO')")
    @Operation(summary = "Listar pagos del usuario autenticado")
    public ResponseEntity<List<PagoResponse>> listarMisPagos() {
        return ResponseEntity.ok(pagoService.listarMisPagos());
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Listar pagos pendientes de validación")
    public ResponseEntity<List<PagoResponse>> listarPendientes() {
        return ResponseEntity.ok(pagoService.listarPendientes());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar todos los pagos (Solo Admin)")
    public ResponseEntity<List<PagoResponse>> listarTodos() {
        return ResponseEntity.ok(pagoService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    @Operation(summary = "Obtener detalle de un pago")
    public ResponseEntity<PagoResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    @GetMapping("/{id}/comprobante")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    @Operation(summary = "Descargar comprobante con autorizacion por propietario o evento")
    public ResponseEntity<Resource> descargarComprobante(@PathVariable UUID id) {
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
    @Operation(summary = "Validar un pago")
    public ResponseEntity<PagoResponse> validarPago(@PathVariable UUID id, @Valid @RequestBody ValidarPagoRequest request) {
        return ResponseEntity.ok(pagoService.validarPago(id, request));
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Rechazar un pago")
    public ResponseEntity<PagoResponse> rechazarPago(@PathVariable UUID id, @Valid @RequestBody ValidarPagoRequest request) {
        return ResponseEntity.ok(pagoService.rechazarPago(id, request));
    }
}
