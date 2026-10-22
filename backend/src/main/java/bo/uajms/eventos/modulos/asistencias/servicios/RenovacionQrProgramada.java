package bo.uajms.eventos.modulos.asistencias.servicios;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RenovacionQrProgramada {
    private final QrAsistenciaService qrService;

    public RenovacionQrProgramada(QrAsistenciaService qrService) {
        this.qrService = qrService;
    }

    @Scheduled(fixedDelayString = "${app.asistencia.qr-renovacion-ms:10000}")
    public void renovarExpirados() {
        qrService.renovarExpirados();
    }
}
