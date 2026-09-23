package bo.uajms.eventos.modulos.reportes.servicios;

import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.certificados.repositorios.CertificadoRepository;
import bo.uajms.eventos.modulos.certificados.entidades.Certificado;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.reportes.dtos.DashboardAcademicoResponse;
import bo.uajms.eventos.modulos.reportes.dtos.DashboardEjecutivoResponse;
import bo.uajms.eventos.modulos.reportes.dtos.DashboardOperativoResponse;
import bo.uajms.eventos.modulos.reportes.dtos.ReporteDataResponse;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscripcionRepository inscripcionRepository;
    private final CertificadoRepository certificadoRepository;
    private final PagoRepository pagoRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public DashboardEjecutivoResponse obtenerDashboardEjecutivo() {
        BigDecimal totalIngresos = obtenerPagosSegunAlcance().stream()
                .filter(pago -> pago.getEstado() == EstadoPago.APROBADO)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalAsistentes = asistenciaRepository.findAll().stream()
                .map(asistencia -> asistencia.getInscripcion().getId())
                .distinct()
                .count();

        return DashboardEjecutivoResponse.builder()
                .totalEventos(eventoRepository.count())
                .totalUsuarios(usuarioRepository.count())
                .totalParticipantes(usuarioRepository.count())
                .totalInscripciones(inscripcionRepository.count())
                .totalCertificados(contarCertificadosSegunAlcance())
                .ingresosGenerados(totalIngresos)
                .nivelSatisfaccion(0.0)
                .participacionEncuestas(0.0)
                .promedioSatisfaccionPorEvento(List.of())
                .build();
    }

    public DashboardAcademicoResponse obtenerDashboardAcademico() {
        Map<String, Long> eventosPorCategoria = eventoRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        evento -> evento.getCategoria().getNombre(),
                        Collectors.counting()
                ));
        List<Map<String, Object>> categorias = eventosPorCategoria.entrySet().stream()
                .map(entry -> Map.<String, Object>of("name", entry.getKey(), "value", entry.getValue()))
                .toList();

        return DashboardAcademicoResponse.builder()
                .participacionPorFacultad(List.of())
                .participacionPorCarrera(List.of())
                .participacionPorCategoria(categorias)
                .participacionPorPeriodo(List.of())
                .build();
    }

    public DashboardOperativoResponse obtenerDashboardOperativo() {
        List<Pago> pagos = obtenerPagosSegunAlcance();
        long pagosPendientes = pagos.stream()
                .filter(pago -> pago.getEstado() == EstadoPago.PENDIENTE_VALIDACION)
                .count();
        long pagosValidados = pagos.stream()
                .filter(pago -> pago.getEstado() == EstadoPago.APROBADO)
                .count();
        long activos = eventoRepository.findAll().stream()
                .filter(evento -> evento.getEstado() == EstadoEvento.PUBLICADO)
                .count();
        long finalizados = eventoRepository.findAll().stream()
                .filter(evento -> evento.getEstado() == EstadoEvento.FINALIZADO)
                .count();

        return DashboardOperativoResponse.builder()
                .eventosActivos(activos)
                .eventosFinalizados(finalizados)
                .pagosPendientes(pagosPendientes)
                .pagosValidados(pagosValidados)
                .qrUtilizados(0)
                .asistenciasRegistradas(contarAsistenciasSegunAlcance())
                .build();
    }

    public ReporteDataResponse generarReporte(String tipo) {
        List<Map<String, Object>> filas = new ArrayList<>();
        if ("eventos".equalsIgnoreCase(tipo)) {
            eventoRepository.findAll().forEach(evento -> filas.add(Map.of(
                    "titulo", evento.getTitulo(), "fecha", evento.getFechaInicio().toString(),
                    "cupos", evento.getCupoMaximo(), "estado", evento.getEstado().name())));
        } else if ("pagos".equalsIgnoreCase(tipo)) {
            obtenerPagosSegunAlcance().forEach(pago -> filas.add(Map.of(
                    "referencia", pago.getNumeroReferencia(), "monto", pago.getMonto(),
                    "estado", pago.getEstado().name(), "inscripcionId", pago.getInscripcion().getId().toString())));
        } else if ("certificados".equalsIgnoreCase(tipo)) {
            obtenerCertificadosSegunAlcance().forEach(certificado -> filas.add(Map.of(
                    "codigo", certificado.getCodigoCertificado(),
                    "participante", certificado.getUsuario().getNombres() + " " + certificado.getUsuario().getApellidos(),
                    "evento", certificado.getEvento().getTitulo(), "estado", certificado.getEstado().name())));
        } else {
            inscripcionRepository.findAll().forEach(inscripcion -> filas.add(Map.of(
                    "participante", inscripcion.getUsuario().getNombres() + " " + inscripcion.getUsuario().getApellidos(),
                    "ci", inscripcion.getUsuario().getCi(), "evento", inscripcion.getEvento().getTitulo(),
                    "codigo", inscripcion.getCodigoParticipante() != null ? inscripcion.getCodigoParticipante() : "N/A")));
        }
        return ReporteDataResponse.builder()
                .tipoReporte(tipo.toUpperCase()).totalRegistros(filas.size()).filas(filas).build();
    }

    private List<Pago> obtenerPagosSegunAlcance() {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return pagoRepository.findAll();
        }
        UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
        return pagoRepository.findByInscripcionEventoOrganizadorId(organizadorId);
    }

    private long contarAsistenciasSegunAlcance() {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return asistenciaRepository.count();
        }
        UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
        return asistenciaRepository.countBySesionEventoEventoOrganizadorId(organizadorId);
    }

    private List<Certificado> obtenerCertificadosSegunAlcance() {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return certificadoRepository.findAll();
        }
        return certificadoRepository.findByEventoOrganizadorId(usuarioAutenticadoService.obtenerUsuario().getId());
    }

    private long contarCertificadosSegunAlcance() {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return certificadoRepository.count();
        }
        return certificadoRepository.countByEventoOrganizadorId(usuarioAutenticadoService.obtenerUsuario().getId());
    }
}
