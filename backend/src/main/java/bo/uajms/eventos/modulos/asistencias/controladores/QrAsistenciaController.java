package bo.uajms.eventos.modulos.asistencias.controladores;

import bo.uajms.eventos.modulos.asistencias.dtos.QrAsistenciaResponse;
import bo.uajms.eventos.modulos.asistencias.servicios.QrAsistenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sesiones/{sesionId}/qr")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR','ORGANIZADOR')")
public class QrAsistenciaController {
    private final QrAsistenciaService qrService;

    @GetMapping
    public ResponseEntity<QrAsistenciaResponse> obtenerActivo(@PathVariable UUID sesionId) {
        return ResponseEntity.ok(qrService.obtenerActivo(sesionId));
    }

    @PostMapping("/generar")
    public ResponseEntity<QrAsistenciaResponse> generar(@PathVariable UUID sesionId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(qrService.generar(sesionId));
    }
}
