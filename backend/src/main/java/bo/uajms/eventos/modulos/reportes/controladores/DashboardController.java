package bo.uajms.eventos.modulos.reportes.controladores;

import bo.uajms.eventos.modulos.reportes.dtos.*;
import bo.uajms.eventos.modulos.reportes.servicios.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    public ResponseEntity<DashboardEjecutivoResponse> general() {
        return ResponseEntity.ok(dashboardService.obtenerDashboardEjecutivo());
    }

    @GetMapping("/dashboard/ejecutivo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    public ResponseEntity<DashboardEjecutivoResponse> ejecutivo() {
        return ResponseEntity.ok(dashboardService.obtenerDashboardEjecutivo());
    }

    @GetMapping("/dashboard/academico")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    public ResponseEntity<DashboardAcademicoResponse> academico() {
        return ResponseEntity.ok(dashboardService.obtenerDashboardAcademico());
    }

    @GetMapping("/dashboard/operativo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'PERSONAL_CONTROL')")
    public ResponseEntity<DashboardOperativoResponse> operativo() {
        return ResponseEntity.ok(dashboardService.obtenerDashboardOperativo());
    }

    @GetMapping("/reportes/eventos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    public ResponseEntity<ReporteDataResponse> reporteEventos() {
        return ResponseEntity.ok(dashboardService.generarReporte("eventos"));
    }

    @GetMapping("/reportes/participantes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    public ResponseEntity<ReporteDataResponse> reporteParticipantes() {
        return ResponseEntity.ok(dashboardService.generarReporte("participantes"));
    }

    @GetMapping("/reportes/pagos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    public ResponseEntity<ReporteDataResponse> reportePagos() {
        return ResponseEntity.ok(dashboardService.generarReporte("pagos"));
    }

    @GetMapping("/reportes/certificados")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    public ResponseEntity<ReporteDataResponse> reporteCertificados() {
        return ResponseEntity.ok(dashboardService.generarReporte("certificados"));
    }

    @GetMapping("/reportes/exportar/pdf")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    public ResponseEntity<byte[]> exportarPdf(@RequestParam String tipo) {
        // Simulación lógica de generación de archivos binarios institucionales
        byte[] mockPdf = "CONTENIDO_REPORTE_OFICIAL_UAJMS_PDF_BINARY".getBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_" + tipo + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(mockPdf);
    }

    @GetMapping("/reportes/exportar/excel")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam String tipo) {
        byte[] mockExcel = "CONTENIDO_REPORTE_OFICIAL_UAJMS_EXCEL_BINARY".getBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_" + tipo + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(mockExcel);
    }
}
