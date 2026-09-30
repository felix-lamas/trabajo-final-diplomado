package bo.uajms.eventos.modulos.reportes.servicios;

import bo.uajms.eventos.modulos.reportes.dtos.ReporteDataResponse;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ReporteArchivoService {

    public byte[] generarPdf(ReporteDataResponse reporte) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(salida);
             PdfDocument pdf = new PdfDocument(writer);
             Document documento = new Document(pdf)) {
            documento.add(new Paragraph("Reporte " + reporte.getTipoReporte()).setBold().setFontSize(18));
            documento.add(new Paragraph("Registros: " + reporte.getTotalRegistros()));

            List<String> columnas = columnas(reporte);
            if (!columnas.isEmpty()) {
                Table tabla = new Table(columnas.size()).useAllAvailableWidth();
                columnas.forEach(columna -> tabla.addHeaderCell(columna));
                for (Map<String, Object> fila : reporte.getFilas()) {
                    columnas.forEach(columna -> tabla.addCell(texto(fila.get(columna))));
                }
                documento.add(tabla);
            }
            documento.close();
            return salida.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible generar el PDF del reporte", exception);
        }
    }

    public byte[] generarXlsx(ReporteDataResponse reporte) {
        List<String> columnas = columnas(reporte);
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(salida, StandardCharsets.UTF_8)) {
            agregar(zip, "[Content_Types].xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                      <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                      <Default Extension="xml" ContentType="application/xml"/>
                      <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                      <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                    </Types>
                    """);
            agregar(zip, "_rels/.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                    </Relationships>
                    """);
            agregar(zip, "xl/workbook.xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                      <sheets><sheet name="Reporte" sheetId="1" r:id="rId1"/></sheets>
                    </workbook>
                    """);
            agregar(zip, "xl/_rels/workbook.xml.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                    </Relationships>
                    """);

            StringBuilder hoja = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
            int numeroFila = 1;
            if (!columnas.isEmpty()) {
                hoja.append("<row r=\"1\">");
                for (int indice = 0; indice < columnas.size(); indice++) {
                    agregarCelda(hoja, indice, numeroFila, columnas.get(indice));
                }
                hoja.append("</row>");
                numeroFila++;
                for (Map<String, Object> fila : reporte.getFilas()) {
                    hoja.append("<row r=\"").append(numeroFila).append("\">");
                    for (int indice = 0; indice < columnas.size(); indice++) {
                        agregarCelda(hoja, indice, numeroFila, texto(fila.get(columnas.get(indice))));
                    }
                    hoja.append("</row>");
                    numeroFila++;
                }
            }
            hoja.append("</sheetData></worksheet>");
            agregar(zip, "xl/worksheets/sheet1.xml", hoja.toString());
            zip.finish();
            return salida.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible generar el XLSX del reporte", exception);
        }
    }

    private List<String> columnas(ReporteDataResponse reporte) {
        LinkedHashSet<String> nombres = new LinkedHashSet<>();
        if (reporte.getFilas() != null) {
            reporte.getFilas().forEach(fila -> nombres.addAll(fila.keySet()));
        }
        return new ArrayList<>(nombres);
    }

    private String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    private void agregarCelda(StringBuilder hoja, int columna, int fila, String valor) {
        hoja.append("<c r=\"").append(referenciaColumna(columna)).append(fila)
                .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                .append(escaparXml(valor)).append("</t></is></c>");
    }

    private String referenciaColumna(int indice) {
        StringBuilder referencia = new StringBuilder();
        int valor = indice + 1;
        while (valor > 0) {
            int resto = (valor - 1) % 26;
            referencia.insert(0, (char) ('A' + resto));
            valor = (valor - 1) / 26;
        }
        return referencia.toString();
    }

    private String escaparXml(String valor) {
        StringBuilder salida = new StringBuilder();
        valor.codePoints().filter(codePoint -> codePoint == 0x9 || codePoint == 0xA || codePoint == 0xD
                        || codePoint >= 0x20 && codePoint <= 0xD7FF
                        || codePoint >= 0xE000 && codePoint <= 0xFFFD
                        || codePoint >= 0x10000 && codePoint <= 0x10FFFF)
                .forEach(codePoint -> {
                    switch (codePoint) {
                        case '&' -> salida.append("&amp;");
                        case '<' -> salida.append("&lt;");
                        case '>' -> salida.append("&gt;");
                        case '"' -> salida.append("&quot;");
                        case '\'' -> salida.append("&apos;");
                        default -> salida.appendCodePoint(codePoint);
                    }
                });
        return salida.toString();
    }

    private void agregar(ZipOutputStream zip, String nombre, String contenido) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre));
        zip.write(contenido.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
