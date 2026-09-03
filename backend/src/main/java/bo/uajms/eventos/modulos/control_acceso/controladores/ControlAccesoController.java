package bo.uajms.eventos.modulos.control_acceso.controladores;

import bo.uajms.eventos.modulos.control_acceso.dtos.*;
import bo.uajms.eventos.modulos.control_acceso.servicios.ControlAccesoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/control-acceso", "/api/v1/control-acceso"})
@RequiredArgsConstructor
public class ControlAccesoController {

    private final ControlAccesoService controlAccesoService;

    @PostMapping("/validar-qr")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'PERSONAL_CONTROL')")
    public ResponseEntity<ValidarQrResponse> validarQr(@RequestBody ValidarQrRequest request) {
        return ResponseEntity.ok(controlAccesoService.validarQr(request.getTokenQr()));
    }

    @PostMapping("/autorizar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'PERSONAL_CONTROL')")
    public ResponseEntity<ControlAccesoResponse> autorizar(@RequestBody AutorizarIngresoRequest request) {
        return ResponseEntity.ok(controlAccesoService.autorizarIngreso(request));
    }

    @PostMapping("/denegar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'PERSONAL_CONTROL')")
    public ResponseEntity<ControlAccesoResponse> denegar(@RequestBody AutorizarIngresoRequest request) {
        return ResponseEntity.ok(controlAccesoService.denegarIngreso(request));
    }

    @GetMapping("/historial/{eventoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'PERSONAL_CONTROL')")
    public ResponseEntity<List<ControlAccesoResponse>> historial(@PathVariable UUID eventoId) {
        return ResponseEntity.ok(controlAccesoService.obtenerHistorial(eventoId));
    }

    @GetMapping("/codigo/{codigoParticipante}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'PERSONAL_CONTROL')")
    public ResponseEntity<ValidarQrResponse> buscarPorCodigo(@PathVariable String codigoParticipante) {
        return ResponseEntity.ok(controlAccesoService.buscarPorCodigoParticipante(codigoParticipante));
    }

    @GetMapping("/documento/{documento}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'PERSONAL_CONTROL')")
    public ResponseEntity<ValidarQrResponse> buscarPorDocumento(
            @PathVariable String documento,
            @RequestParam UUID eventoId) {
        return ResponseEntity.ok(controlAccesoService.buscarPorDocumentoIdentidad(documento, eventoId));
    }
}
