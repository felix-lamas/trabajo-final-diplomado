package bo.uajms.eventos.modulos.codigo_qr.controladores;

import bo.uajms.eventos.modulos.codigo_qr.dtos.CodigoQrResponse;
import bo.uajms.eventos.modulos.codigo_qr.entidades.CodigoQr;
import bo.uajms.eventos.modulos.codigo_qr.mappers.CodigoQrMapper;
import bo.uajms.eventos.modulos.codigo_qr.repositorios.CodigoQrRepository;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/codigos-qr")
@RequiredArgsConstructor
@Tag(name = "Codigos QR", description = "Operaciones con códigos QR")
@SecurityRequirement(name = "bearerAuth")
public class CodigoQrController {

    private final CodigoQrRepository codigoQrRepository;
    private final CodigoQrMapper codigoQrMapper;

    @GetMapping("/validar/{contenido}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Validar el contenido de un código QR")
    public ResponseEntity<CodigoQrResponse> validar(@PathVariable String contenido) {
        // En un sistema real, el contenido podría ser un token firmado
        // Por ahora buscamos por contenido (que es el ID de la credencial)
        UUID credencialId = UUID.fromString(contenido);
        CodigoQr qr = codigoQrRepository.findByCredencialId(credencialId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Código QR", credencialId));

        return ResponseEntity.ok(codigoQrMapper.toResponse(qr));
    }
}
