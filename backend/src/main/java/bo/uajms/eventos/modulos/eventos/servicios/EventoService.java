package bo.uajms.eventos.modulos.eventos.servicios;

import bo.uajms.eventos.core.excepciones.CodigosError;
import bo.uajms.eventos.core.excepciones.ConflictoException;
import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.categorias.entidades.CategoriaEvento;
import bo.uajms.eventos.modulos.categorias.repositorios.CategoriaEventoRepository;
import bo.uajms.eventos.modulos.eventos.dtos.*;
import bo.uajms.eventos.modulos.eventos.entidades.*;
import bo.uajms.eventos.modulos.eventos.mappers.EventoMapper;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventoService {

    private static final String CATEGORIA_ACTIVA = "ACTIVO";
    private static final ZoneId ZONA_OFICIAL = ZoneId.of("America/La_Paz");

    private final EventoRepository eventoRepository;
    private final CategoriaEventoRepository categoriaRepository;
    private final EventoMapper eventoMapper;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    @Transactional(readOnly = true)
    public List<EventoResponse> listarTodos() {
        List<Evento> eventos;
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            eventos = eventoRepository.findAll();
        } else if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            eventos = eventoRepository.findByOrganizadorId(usuarioAutenticadoService.obtenerUsuario().getId());
        } else {
            eventos = eventoRepository.findByEstado(EstadoEvento.PUBLICADO);
        }
        return mapear(eventos);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarEnRevision() {
        exigirAdministrador();
        return mapear(eventoRepository.findByEstado(EstadoEvento.EN_REVISION));
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarPublicados() {
        return mapear(eventoRepository.findByEstado(EstadoEvento.PUBLICADO));
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> buscarPublicados(String texto, UUID categoriaId,
                                                  TipoInscripcion tipo, Modalidad modalidad) {
        String filtroTexto = texto == null || texto.isBlank() ? null : texto.trim();
        return mapear(eventoRepository.buscarPublicados(
                EstadoEvento.PUBLICADO, filtroTexto, categoriaId, tipo, modalidad));
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarPorCategoria(UUID categoriaId) {
        List<Evento> eventos;
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            eventos = eventoRepository.findByCategoriaId(categoriaId);
        } else if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            eventos = eventoRepository.findByCategoriaIdAndOrganizadorId(
                    categoriaId, usuarioAutenticadoService.obtenerUsuario().getId());
        } else {
            eventos = eventoRepository.findByCategoriaIdAndEstado(categoriaId, EstadoEvento.PUBLICADO);
        }
        return mapear(eventos);
    }

    @Transactional(readOnly = true)
    public EventoDetalleResponse buscarPorId(UUID id) {
        return buscarEventoVisible(id).map(eventoMapper::toDetalleResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", id));
    }

    @Transactional
    public EventoDetalleResponse crear(CrearEventoRequest request) {
        Usuario organizador = exigirCreadorEvento();
        CategoriaEvento categoria = obtenerCategoriaActiva(request.getCategoriaId());
        validarDatos(request, false);
        Evento evento = Evento.builder().categoria(categoria).organizador(organizador)
                .estado(EstadoEvento.BORRADOR).build();
        aplicarDatos(evento, request, 0);
        return eventoMapper.toDetalleResponse(eventoRepository.save(evento));
    }

    @Transactional
    public EventoDetalleResponse actualizar(UUID id, ActualizarEventoRequest request) {
        Evento evento = obtenerEventoGestionable(id);
        if (evento.getEstado() != EstadoEvento.BORRADOR && evento.getEstado() != EstadoEvento.RECHAZADO) {
            throw new NegocioException("Solo se pueden editar eventos BORRADOR o RECHAZADO");
        }
        CategoriaEvento categoria = obtenerCategoriaActiva(request.getCategoriaId());
        validarDatos(request, false);
        int ocupados = calcularCuposOcupados(evento);
        evento.setCategoria(categoria);
        aplicarDatos(evento, request, ocupados);
        return eventoMapper.toDetalleResponse(eventoRepository.save(evento));
    }

    @Transactional
    public void enviarARevision(UUID id) {
        exigirOrganizadorAprobado();
        Evento evento = obtenerEventoGestionable(id);
        exigirEstado(evento, EstadoEvento.BORRADOR, "enviar a revision");
        validarEventoCompleto(evento);
        evento.setEstado(EstadoEvento.EN_REVISION);
        evento.setFechaEnvioRevision(LocalDateTime.now(ZONA_OFICIAL));
        eventoRepository.save(evento);
    }

    @Transactional
    public void publicar(UUID id) {
        exigirAdministrador();
        Evento evento = obtenerEventoGlobalParaActualizar(id);
        exigirEstado(evento, EstadoEvento.EN_REVISION, "publicar");
        validarEventoCompleto(evento);
        resolver(evento, EstadoEvento.PUBLICADO, null);
    }

    @Transactional
    public void rechazar(UUID id, String motivo) {
        exigirAdministrador();
        validarMotivo(motivo, "rechazo");
        Evento evento = obtenerEventoGlobalParaActualizar(id);
        exigirEstado(evento, EstadoEvento.EN_REVISION, "rechazar");
        resolver(evento, EstadoEvento.RECHAZADO, motivo.trim());
    }

    @Transactional
    public void volverABorrador(UUID id) {
        exigirOrganizadorAprobado();
        Evento evento = obtenerEventoGestionable(id);
        exigirEstado(evento, EstadoEvento.RECHAZADO, "volver a borrador");
        evento.setEstado(EstadoEvento.BORRADOR);
        eventoRepository.save(evento);
    }

    @Transactional
    public void cancelar(UUID id, String motivo) {
        exigirAdministrador();
        validarMotivo(motivo, "cancelacion");
        Evento evento = obtenerEventoGlobalParaActualizar(id);
        exigirEstado(evento, EstadoEvento.PUBLICADO, "cancelar");
        evento.setEstado(EstadoEvento.CANCELADO);
        evento.setMotivoCancelacion(motivo.trim());
        evento.setFechaResolucion(LocalDateTime.now(ZONA_OFICIAL));
        evento.setResueltoPor(usuarioAutenticadoService.obtenerUsuario());
        eventoRepository.save(evento);
    }

    @Transactional
    public void finalizar(UUID id) {
        exigirAdministrador();
        Evento evento = obtenerEventoGlobalParaActualizar(id);
        exigirEstado(evento, EstadoEvento.PUBLICADO, "finalizar");
        LocalDateTime fin = LocalDateTime.of(evento.getFechaFin(), evento.getHoraFin());
        if (LocalDateTime.now(ZONA_OFICIAL).isBefore(fin)) {
            throw new NegocioException("No se puede finalizar el evento antes de su fecha y hora de fin");
        }
        resolver(evento, EstadoEvento.FINALIZADO, null);
    }

    @Transactional
    public void eliminar(UUID id) {
        Evento evento = obtenerEventoGestionable(id);
        exigirEstado(evento, EstadoEvento.BORRADOR, "eliminar");
        eventoRepository.delete(evento);
    }

    private void aplicarDatos(Evento evento, EventoDatosRequest request, int ocupados) {
        evento.setTitulo(request.getTitulo().trim());
        evento.setDescripcion(limpiar(request.getDescripcion()));
        evento.setObjetivos(limpiar(request.getObjetivos()));
        evento.setModalidad(request.getModalidad());
        evento.setTipoInscripcion(request.getTipoInscripcion());
        evento.setCosto(request.getTipoInscripcion() == TipoInscripcion.GRATUITO
                ? BigDecimal.ZERO : request.getCosto());
        evento.setFechaInicio(request.getFechaInicio());
        evento.setFechaFin(request.getFechaFin());
        evento.setHoraInicio(request.getHoraInicio());
        evento.setHoraFin(request.getHoraFin());
        boolean presencial = request.getModalidad() == Modalidad.PRESENCIAL;
        evento.setUbicacion(presencial ? limpiar(request.getUbicacion()) : null);
        evento.setDireccion(presencial ? limpiar(request.getDireccion()) : null);
        evento.setLatitud(presencial ? request.getLatitud() : null);
        evento.setLongitud(presencial ? request.getLongitud() : null);
        evento.setRadioMetros(presencial ? request.getRadioMetros() : null);
        evento.setEnlaceVirtual(presencial ? null : limpiar(request.getEnlaceVirtual()));
        evento.setRequiereInscripcion(request.getRequiereInscripcion());
        boolean limitado = Boolean.TRUE.equals(request.getRequiereInscripcion())
                && Boolean.TRUE.equals(request.getCupoLimitado());
        evento.setCupoLimitado(limitado);
        if (limitado) {
            if (request.getCupoMaximo() < ocupados) {
                throw new NegocioException("El cupo maximo es menor que las inscripciones existentes");
            }
            evento.setCupoMaximo(request.getCupoMaximo());
            evento.setCupoDisponible(request.getCupoMaximo() - ocupados);
        } else {
            evento.setCupoMaximo(null);
            evento.setCupoDisponible(null);
        }
        evento.setEmiteCertificado(request.getEmiteCertificado());
        evento.setTipoCertificado(Boolean.TRUE.equals(request.getEmiteCertificado())
                ? request.getTipoCertificado() : null);
        evento.setHorasAcademicas(Boolean.TRUE.equals(request.getEmiteCertificado())
                && request.getTipoCertificado() == TipoCertificadoEvento.CURRICULAR
                ? request.getHorasAcademicas() : null);
        evento.setPublicoObjetivo(request.getPublicoObjetivo());
        evento.setTelefonoContacto(limpiar(request.getTelefonoContacto()));
        evento.setEmailContacto(limpiar(request.getEmailContacto()));
        evento.setWhatsappContacto(limpiar(request.getWhatsappContacto()));
        evento.setImagenPortada(limpiar(request.getImagenPortada()));
        boolean pagado = request.getTipoInscripcion() == TipoInscripcion.PAGO;
        evento.setQrPagoUrl(pagado ? limpiar(request.getQrPagoUrl()) : null);
        evento.setInstruccionesPago(pagado ? limpiar(request.getInstruccionesPago()) : null);
    }

    private void validarDatos(EventoDatosRequest request, boolean completo) {
        if (request.getFechaInicio() == null || request.getFechaFin() == null
                || request.getHoraInicio() == null || request.getHoraFin() == null
                || request.getModalidad() == null || request.getTipoInscripcion() == null
                || request.getRequiereInscripcion() == null || request.getCupoLimitado() == null
                || request.getEmiteCertificado() == null || request.getPublicoObjetivo() == null) {
            throw new NegocioException("Faltan datos obligatorios del evento");
        }
        LocalDateTime inicio = LocalDateTime.of(request.getFechaInicio(), request.getHoraInicio());
        LocalDateTime fin = LocalDateTime.of(request.getFechaFin(), request.getHoraFin());
        if (!inicio.isBefore(fin)) {
            throw new NegocioException("La fecha y hora de inicio debe ser anterior al fin");
        }
        validarCosto(request.getTipoInscripcion(), request.getCosto());
        if (request.getModalidad() == Modalidad.PRESENCIAL
                && (esVacio(request.getUbicacion()) || esVacio(request.getDireccion())
                || request.getLatitud() == null || request.getLongitud() == null
                || request.getRadioMetros() == null)) {
            throw new NegocioException("Un evento PRESENCIAL requiere ubicacion, direccion, coordenadas y radio");
        }
        if (request.getModalidad() == Modalidad.VIRTUAL && esVacio(request.getEnlaceVirtual())) {
            throw new NegocioException("Un evento VIRTUAL requiere enlace de acceso");
        }
        if (request.getModalidad() == Modalidad.VIRTUAL) validarUrl(request.getEnlaceVirtual(), "enlace virtual");
        validarUrl(request.getImagenPortada(), "imagen de portada");
        if (request.getTipoInscripcion() == TipoInscripcion.PAGO) validarUrl(request.getQrPagoUrl(), "QR de pago");
        if ((request.getLatitud() == null) != (request.getLongitud() == null)) {
            throw new NegocioException("Latitud y longitud deben informarse juntas");
        }
        if (request.getRadioMetros() != null && (request.getRadioMetros() <= 0 || request.getRadioMetros() > 500)) {
            throw new NegocioException("El radio permitido debe estar entre 1 y 500 metros");
        }
        if (request.getModalidad() == Modalidad.VIRTUAL
                && (request.getLatitud() != null || request.getRadioMetros() != null)) {
            throw new NegocioException("Un evento VIRTUAL no utiliza coordenadas ni radio");
        }
        if (Boolean.TRUE.equals(request.getRequiereInscripcion())
                && Boolean.TRUE.equals(request.getCupoLimitado())
                && (request.getCupoMaximo() == null || request.getCupoMaximo() <= 0)) {
            throw new NegocioException("Un evento con cupo limitado requiere capacidad mayor a cero");
        }
        if (Boolean.TRUE.equals(request.getEmiteCertificado())) {
            if (request.getTipoCertificado() == null) {
                throw new NegocioException("Debe especificar el tipo de certificado");
            }
            if (request.getTipoCertificado() == TipoCertificadoEvento.CURRICULAR
                    && (request.getHorasAcademicas() == null || request.getHorasAcademicas() <= 0)) {
                throw new NegocioException("Un certificado CURRICULAR requiere horas academicas mayores a cero");
            }
        }
        if (request.getTipoInscripcion() == TipoInscripcion.PAGO
                && esVacio(request.getInstruccionesPago()) && esVacio(request.getQrPagoUrl())) {
            throw new NegocioException("Un evento de PAGO requiere instrucciones o QR de pago");
        }
        if (completo && (esVacio(request.getDescripcion()) || esVacio(request.getObjetivos()))) {
            throw new NegocioException("El evento requiere descripcion y objetivos para revision");
        }
    }

    private void validarEventoCompleto(Evento evento) {
        if (esVacio(evento.getTitulo()) || esVacio(evento.getDescripcion()) || esVacio(evento.getObjetivos())
                || evento.getCategoria() == null || !CATEGORIA_ACTIVA.equalsIgnoreCase(evento.getCategoria().getEstado())
                || evento.getModalidad() == null || evento.getTipoInscripcion() == null
                || evento.getFechaInicio() == null || evento.getFechaFin() == null
                || evento.getHoraInicio() == null || evento.getHoraFin() == null
                || evento.getRequiereInscripcion() == null || evento.getCupoLimitado() == null
                || evento.getEmiteCertificado() == null || evento.getPublicoObjetivo() == null) {
            throw new NegocioException("El evento no tiene todos los datos obligatorios para revision");
        }
        if (!LocalDateTime.of(evento.getFechaInicio(), evento.getHoraInicio())
                .isBefore(LocalDateTime.of(evento.getFechaFin(), evento.getHoraFin()))) {
            throw new NegocioException("La fecha y hora de inicio debe ser anterior al fin");
        }
        if (evento.getModalidad() == Modalidad.PRESENCIAL
                && (esVacio(evento.getUbicacion()) || esVacio(evento.getDireccion())
                || evento.getLatitud() == null || evento.getLongitud() == null || evento.getRadioMetros() == null)) {
            throw new NegocioException("El evento PRESENCIAL no tiene ubicacion completa");
        }
        if (evento.getModalidad() == Modalidad.PRESENCIAL
                && (evento.getRadioMetros() <= 0 || evento.getRadioMetros() > 500)) {
            throw new NegocioException("El radio permitido debe estar entre 1 y 500 metros");
        }
        if (evento.getModalidad() == Modalidad.VIRTUAL && esVacio(evento.getEnlaceVirtual())) {
            throw new NegocioException("El evento VIRTUAL no tiene enlace de acceso");
        }
        if (evento.getModalidad() == Modalidad.VIRTUAL) validarUrl(evento.getEnlaceVirtual(), "enlace virtual");
        validarUrl(evento.getImagenPortada(), "imagen de portada");
        validarCosto(evento.getTipoInscripcion(), evento.getCosto());
        if (evento.getTipoInscripcion() == TipoInscripcion.PAGO
                && esVacio(evento.getInstruccionesPago()) && esVacio(evento.getQrPagoUrl())) {
            throw new NegocioException("El evento de PAGO no tiene instrucciones ni QR de pago");
        }
        if (evento.getTipoInscripcion() == TipoInscripcion.PAGO) validarUrl(evento.getQrPagoUrl(), "QR de pago");
        if (Boolean.TRUE.equals(evento.getRequiereInscripcion()) && Boolean.TRUE.equals(evento.getCupoLimitado())
                && (evento.getCupoMaximo() == null || evento.getCupoMaximo() <= 0)) {
            throw new NegocioException("El evento con cupo limitado no tiene capacidad valida");
        }
        if (Boolean.TRUE.equals(evento.getEmiteCertificado()) && evento.getTipoCertificado() == null) {
            throw new NegocioException("El evento no tiene tipo de certificado");
        }
        if (evento.getTipoCertificado() == TipoCertificadoEvento.CURRICULAR
                && (evento.getHorasAcademicas() == null || evento.getHorasAcademicas() <= 0)) {
            throw new NegocioException("El certificado CURRICULAR requiere horas academicas");
        }
    }

    private void validarCosto(TipoInscripcion tipo, BigDecimal costo) {
        if (tipo == TipoInscripcion.GRATUITO && costo != null && costo.compareTo(BigDecimal.ZERO) != 0) {
            throw new NegocioException("Un evento GRATUITO debe tener costo cero o nulo");
        }
        if (tipo == TipoInscripcion.PAGO && (costo == null || costo.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new NegocioException("Un evento de PAGO requiere costo mayor a cero");
        }
    }

    private int calcularCuposOcupados(Evento evento) {
        if (!Boolean.TRUE.equals(evento.getCupoLimitado()) || evento.getCupoMaximo() == null
                || evento.getCupoDisponible() == null) return 0;
        return evento.getCupoMaximo() - evento.getCupoDisponible();
    }

    private CategoriaEvento obtenerCategoriaActiva(UUID id) {
        return categoriaRepository.findByIdAndEstado(id, CATEGORIA_ACTIVA)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria", id));
    }

    private List<EventoResponse> mapear(List<Evento> eventos) {
        return eventos.stream().map(eventoMapper::toResponse).toList();
    }

    private Optional<Evento> buscarEventoVisible(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) return eventoRepository.findById(id);
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            UUID usuarioId = usuarioAutenticadoService.obtenerUsuario().getId();
            return eventoRepository.findByIdAndOrganizadorId(id, usuarioId)
                    .or(() -> eventoRepository.findByIdAndEstado(id, EstadoEvento.PUBLICADO));
        }
        return eventoRepository.findByIdAndEstado(id, EstadoEvento.PUBLICADO);
    }

    private Evento obtenerEventoGestionable(UUID id) {
        Evento evento = obtenerEventoGlobalParaActualizar(id);
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) return evento;
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            Usuario usuario = exigirOrganizadorAprobado();
            if (!evento.getOrganizador().getId().equals(usuario.getId())) {
                throw new AccessDeniedException("El evento pertenece a otro organizador");
            }
            return evento;
        }
        throw new AccessDeniedException("El rol no permite gestionar eventos");
    }

    private Evento obtenerEventoPropio(UUID id) {
        UUID usuarioId = usuarioAutenticadoService.obtenerUsuario().getId();
        return eventoRepository.findByIdAndOrganizadorId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", id));
    }

    private Evento obtenerEventoGlobal(UUID id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", id));
    }

    private Evento obtenerEventoGlobalParaActualizar(UUID id) {
        return eventoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", id));
    }

    private void resolver(Evento evento, EstadoEvento estado, String motivo) {
        evento.setEstado(estado);
        evento.setMotivoRechazo(motivo);
        evento.setFechaResolucion(LocalDateTime.now(ZONA_OFICIAL));
        evento.setResueltoPor(usuarioAutenticadoService.obtenerUsuario());
        eventoRepository.save(evento);
    }

    private void exigirEstado(Evento evento, EstadoEvento esperado, String accion) {
        if (evento.getEstado() != esperado) {
            throw new ConflictoException(CodigosError.EVENT_INVALID_STATE,
                    "No se puede " + accion + " un evento en estado " + evento.getEstado());
        }
    }

    private void exigirAdministrador() {
        if (!usuarioAutenticadoService.tieneRol("ADMINISTRADOR"))
            throw new AccessDeniedException("Se requiere rol ADMINISTRADOR");
    }

    private Usuario exigirOrganizadorAprobado() {
        if (!usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            throw new AccessDeniedException("Se requiere rol ORGANIZADOR");
        }
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();
        if (usuario.getEstadoSolicitudOrganizador() != Usuario.EstadoSolicitudOrganizador.APROBADA) {
            throw new AccessDeniedException("La solicitud de organizador debe estar APROBADA");
        }
        return usuario;
    }

    private Usuario exigirCreadorEvento() {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return usuarioAutenticadoService.obtenerUsuario();
        }
        return exigirOrganizadorAprobado();
    }

    private void validarMotivo(String motivo, String tipo) {
        if (motivo == null || motivo.isBlank()) throw new NegocioException("El motivo de " + tipo + " es obligatorio");
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String limpiar(String valor) {
        return esVacio(valor) ? null : valor.trim();
    }

    private void validarUrl(String valor, String campo) {
        if (esVacio(valor)) return;
        try {
            URI uri = new URI(valor.trim());
            if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null) {
                throw new NegocioException("El " + campo + " debe ser una URL HTTP(S) valida");
            }
        } catch (URISyntaxException ex) {
            throw new NegocioException("El " + campo + " debe ser una URL HTTP(S) valida");
        }
    }
}
