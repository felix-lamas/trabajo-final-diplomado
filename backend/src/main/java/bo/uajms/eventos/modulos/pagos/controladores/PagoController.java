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
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @Operation(summary = "Subir comprobante de pago")
    public ResponseEntity<PagoResponse> subirComprobante(@PathVariable UUID id, @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(pagoService.subirComprobante(id, archivo));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un pago")
    public ResponseEntity<PagoResponse> registrarPago(@Valid @RequestBody RegistrarPagoRequest request) {
        return new ResponseEntity<>(pagoService.registrarPago(request), HttpStatus.CREATED);
    }

    @GetMapping("/mis-pagos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
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
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @Operation(summary = "Obtener detalle de un pago")
    public ResponseEntity<PagoResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/validar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Validar un pago")
    public ResponseEntity<PagoResponse> validarPago(@PathVariable UUID id, @RequestBody ValidarPagoRequest request) {
        return ResponseEntity.ok(pagoService.validarPago(id, request));
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Rechazar un pago")
    public ResponseEntity<PagoResponse> rechazarPago(@PathVariable UUID id, @RequestBody ValidarPagoRequest request) {
        return ResponseEntity.ok(pagoService.rechazarPago(id, request));
    }
}
