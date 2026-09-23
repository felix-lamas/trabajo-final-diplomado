package bo.uajms.eventos.modulos.reportes.servicios;

import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.certificados.entidades.Certificado;
import bo.uajms.eventos.modulos.certificados.repositorios.CertificadoRepository;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceCertificadoAuthorizationTest {

    @Mock EventoRepository eventoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock InscripcionRepository inscripcionRepository;
    @Mock CertificadoRepository certificadoRepository;
    @Mock PagoRepository pagoRepository;
    @Mock AsistenciaRepository asistenciaRepository;
    @Mock UsuarioAutenticadoService auth;
    @InjectMocks DashboardService service;

    @Test
    void organizadorObtieneSoloCertificadosDeSusEventosEnReporteYConteo() {
        Usuario organizador = Usuario.builder().build();
        organizador.setId(UUID.randomUUID());
        Certificado certificado = certificado(organizador);
        when(auth.tieneRol("ADMINISTRADOR")).thenReturn(false);
        when(auth.obtenerUsuario()).thenReturn(organizador);
        when(certificadoRepository.findByEventoOrganizadorId(organizador.getId())).thenReturn(List.of(certificado));
        when(certificadoRepository.countByEventoOrganizadorId(organizador.getId())).thenReturn(1L);

        assertEquals(1, service.generarReporte("certificados").getTotalRegistros());
        assertEquals(1, service.obtenerDashboardEjecutivo().getTotalCertificados());
        verify(certificadoRepository, never()).findAll();
        verify(certificadoRepository, never()).count();
    }

    private Certificado certificado(Usuario organizador) {
        Usuario participante = Usuario.builder().nombres("Ana").apellidos("Perez").build();
        Evento evento = Evento.builder().titulo("Evento propio").organizador(organizador).build();
        Certificado certificado = Certificado.builder().usuario(participante).evento(evento)
                .codigoCertificado("UAJMS-CODIGO").fechaEmision(LocalDateTime.now())
                .estado(Certificado.EstadoCertificado.GENERADO)
                .tipoCertificado(Certificado.TipoCertificado.NO_CURRICULAR).build();
        certificado.setId(UUID.randomUUID());
        return certificado;
    }
}
