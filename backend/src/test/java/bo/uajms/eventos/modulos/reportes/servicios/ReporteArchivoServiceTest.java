package bo.uajms.eventos.modulos.reportes.servicios;

import bo.uajms.eventos.modulos.reportes.dtos.ReporteDataResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReporteArchivoServiceTest {

    private final ReporteArchivoService service = new ReporteArchivoService();
    private final ReporteDataResponse reporte = ReporteDataResponse.builder()
            .tipoReporte("EVENTOS")
            .totalRegistros(1)
            .filas(List.of(Map.of("titulo", "Jornada UAJMS & Ciencia", "estado", "PUBLICADO")))
            .build();

    @Test
    void generaPdfLegibleConFirmaPdf() {
        byte[] archivo = service.generarPdf(reporte);

        assertTrue(archivo.length > 100);
        assertArrayEquals("%PDF-".getBytes(StandardCharsets.US_ASCII),
                java.util.Arrays.copyOf(archivo, 5));
    }

    @Test
    void generaXlsxComoPaqueteOpenXmlConDatosEscapados() throws Exception {
        byte[] archivo = service.generarXlsx(reporte);
        boolean tieneTipos = false;
        boolean tieneLibro = false;
        boolean tieneHojaConReporte = false;

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archivo), StandardCharsets.UTF_8)) {
            java.util.zip.ZipEntry entrada;
            while ((entrada = zip.getNextEntry()) != null) {
                byte[] contenido = zip.readAllBytes();
                String texto = new String(contenido, StandardCharsets.UTF_8);
                if (entrada.getName().equals("[Content_Types].xml")) tieneTipos = true;
                if (entrada.getName().equals("xl/workbook.xml")) tieneLibro = true;
                if (entrada.getName().equals("xl/worksheets/sheet1.xml")) {
                    tieneHojaConReporte = texto.contains("Jornada UAJMS &amp; Ciencia")
                            && texto.contains("PUBLICADO");
                }
            }
        }

        assertTrue(tieneTipos);
        assertTrue(tieneLibro);
        assertTrue(tieneHojaConReporte);
    }
}
