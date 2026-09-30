package bo.uajms.eventos.modulos.reportes.controladores;

import bo.uajms.eventos.modulos.reportes.dtos.*;
import bo.uajms.eventos.modulos.reportes.servicios.DashboardService;
import bo.uajms.eventos.modulos.reportes.servicios.ReporteArchivoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Dashboards y reportes",
        description = "Dashboards y reportes del contrato canonico /api/v1.")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;
    private final ReporteArchivoService reporteArchivoService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Obtener dashboard general",
            description = "Alias funcional del dashboard ejecutivo. Para ORGANIZADOR, las metricas se limitan a eventos propios.")
    public ResponseEntity<DashboardEjecutivoResponse> general() {
        return ResponseEntity.ok(dashboardService.obtenerDashboardEjecutivo());
    }

    @GetMapping("/dashboard/ejecutivo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Obtener dashboard ejecutivo",
            description = "Resume eventos, participantes, inscripciones, pagos y certificados. ORGANIZADOR recibe solo datos de eventos propios.")
    public ResponseEntity<DashboardEjecutivoResponse> ejecutivo() {
        return ResponseEntity.ok(dashboardService.obtenerDashboardEjecutivo());
    }

    @GetMapping("/dashboard/academico")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Obtener dashboard academico",
            description = "Devuelve indicadores academicos; para ORGANIZADOR aplica ownership de eventos.")
    public ResponseEntity<DashboardAcademicoResponse> academico() {
        return ResponseEntity.ok(dashboardService.obtenerDashboardAcademico());
    }

    @GetMapping("/dashboard/operativo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Obtener dashboard operativo",
            description = "Devuelve indicadores operativos; para ORGANIZADOR aplica ownership de eventos.")
    public ResponseEntity<DashboardOperativoResponse> operativo() {
        return ResponseEntity.ok(dashboardService.obtenerDashboardOperativo());
    }

    @GetMapping("/reportes/eventos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Generar reporte de eventos",
            description = "Devuelve datos JSON del reporte. ORGANIZADOR recibe solamente eventos propios.")
    public ResponseEntity<ReporteDataResponse> reporteEventos() {
        return ResponseEntity.ok(dashboardService.generarReporte("eventos"));
    }

    @GetMapping("/reportes/participantes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Generar reporte de participantes",
            description = "Devuelve datos JSON del reporte. ORGANIZADOR recibe participantes de eventos propios.")
    public ResponseEntity<ReporteDataResponse> reporteParticipantes() {
        return ResponseEntity.ok(dashboardService.generarReporte("participantes"));
    }

    @GetMapping("/reportes/pagos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Generar reporte de pagos",
            description = "Devuelve datos JSON del reporte. ORGANIZADOR recibe pagos de eventos propios.")
    public ResponseEntity<ReporteDataResponse> reportePagos() {
        return ResponseEntity.ok(dashboardService.generarReporte("pagos"));
    }

    @GetMapping("/reportes/certificados")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Generar reporte de certificados",
            description = "Devuelve datos JSON del reporte. ORGANIZADOR recibe certificados de eventos propios.")
    public ResponseEntity<ReporteDataResponse> reporteCertificados() {
        return ResponseEntity.ok(dashboardService.generarReporte("certificados"));
    }

    @GetMapping("/reportes/exportar/pdf")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Exportar reporte PDF",
            description = "Genera un documento PDF real con las filas del reporte. ORGANIZADOR conserva alcance de eventos propios.")
    @ApiResponse(responseCode = "200", description = "Documento PDF generado",
            content = @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary")))
    public ResponseEntity<byte[]> exportarPdf(
            @Parameter(description = "Tipo soportado: eventos, participantes, pagos o certificados",
                    example = "eventos") @RequestParam String tipo) {
        // Simulación lógica de generación de archivos binarios institucionales
        ReporteDataResponse reporte = dashboardService.generarReporte(tipo);
        byte[] pdf = reporteArchivoService.generarPdf(reporte);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_" + tipo + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/reportes/exportar/excel")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Exportar reporte Excel XLSX",
            description = "Genera un libro XLSX real con las filas del reporte. ORGANIZADOR conserva alcance de eventos propios.")
    @ApiResponse(responseCode = "200", description = "Libro XLSX generado",
            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    schema = @Schema(type = "string", format = "binary")))
    public ResponseEntity<byte[]> exportarExcel(
            @Parameter(description = "Tipo soportado: eventos, participantes, pagos o certificados",
                    example = "eventos") @RequestParam String tipo) {
        ReporteDataResponse reporte = dashboardService.generarReporte(tipo);
        byte[] excel = reporteArchivoService.generarXlsx(reporte);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_" + tipo + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
