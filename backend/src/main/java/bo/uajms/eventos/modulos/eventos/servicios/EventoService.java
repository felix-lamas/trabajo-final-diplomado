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
import bo.uajms.eventos.modulos.pagos.servicios.AlmacenamientoArchivos;
import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Iterator;
import java.util.Optional;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoService {

    private static final String CATEGORIA_ACTIVA = "ACTIVO";
    private static final ZoneId ZONA_OFICIAL = ZoneId.of("America/La_Paz");

    private final EventoRepository eventoRepository;
    private final CategoriaEventoRepository categoriaRepository;
    private final EventoMapper eventoMapper;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final AlmacenamientoArchivos almacenamientoArchivos;

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
        return mapear(eventoRepository.findByCategoriaIdAndEstado(categoriaId, EstadoEvento.PUBLICADO));
    }

    @Transactional(readOnly = true)
    public EventoDetalleResponse buscarPorId(UUID id) {
        return buscarEventoVisible(id).map(eventoMapper::toDetalleResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", id));
    }

    @Transactional
    public void subirQrPago(UUID id, MultipartFile archivo) {
        Evento evento = obtenerEventoGestionable(id);
        validarEstadoEditableQr(evento);
        if (evento.getTipoInscripcion() != TipoInscripcion.PAGO) {
            throw new NegocioException("Solo los eventos de pago pueden tener un QR");
        }
        QrProcesado procesado = validarQr(archivo);
        String nuevaClave = "eventos/" + id + "/qr-pago/" + UUID.randomUUID() + "." + procesado.extension();
        String claveAnterior = evento.getQrPagoStorageKey();
        almacenamientoArchivos.guardar(nuevaClave, procesado.bytes(), procesado.mime());
        evento.setQrPagoStorageKey(nuevaClave);
        evento.setQrPagoUrl(null);
        try {
            eventoRepository.saveAndFlush(evento);
        } catch (RuntimeException exception) {
            eliminarQrSeguro(nuevaClave);
            throw exception;
        }
        registrarLimpiezaQr(claveAnterior, nuevaClave);
    }

    @Transactional
    public void eliminarQrPago(UUID id) {
        Evento evento = obtenerEventoGestionable(id);
        validarEstadoEditableQr(evento);
        String anterior = evento.getQrPagoStorageKey();
        evento.setQrPagoStorageKey(null);
        evento.setQrPagoUrl(null);
        eventoRepository.saveAndFlush(evento);
        registrarLimpiezaQr(anterior, null);
    }

    @Transactional(readOnly = true)
    public QrPagoArchivo obtenerQrPago(UUID id) {
        Evento evento = buscarEventoVisible(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", id));
        String clave = evento.getQrPagoStorageKey();
        if (clave == null) throw new RecursoNoEncontradoException("QR de pago", id);
        Resource recurso = almacenamientoArchivos.descargar(clave)
                .orElseThrow(() -> new RecursoNoEncontradoException("QR de pago", id));
        String extension = clave.substring(clave.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        MediaType mime = "png".equals(extension) ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        try {
            return new QrPagoArchivo(recurso, mime, recurso.contentLength(), evento.getEstado() == EstadoEvento.PUBLICADO);
        } catch (IOException exception) {
            throw new ServicioNoDisponibleException("STORAGE_UNAVAILABLE", "No fue posible leer el QR del evento");
        }
    }

    private void validarEstadoEditableQr(Evento evento) {
        if (evento.getEstado() != EstadoEvento.BORRADOR && evento.getEstado() != EstadoEvento.RECHAZADO) {
            throw new ConflictoException(CodigosError.EVENT_INVALID_STATE,
                    "Solo se puede modificar el QR de un evento BORRADOR o RECHAZADO");
        }
    }

    private QrProcesado validarQr(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) throw new NegocioException("La imagen QR es obligatoria");
        if (archivo.getSize() > 5L * 1024 * 1024) throw new NegocioException("La imagen QR supera el máximo de 5 MB");
        String nombre = archivo.getOriginalFilename();
        int punto = nombre == null ? -1 : nombre.lastIndexOf('.');
        if (punto < 0 || punto == nombre.length() - 1) throw new NegocioException("Solo se permiten PNG, JPG o JPEG");
        String extension = nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
        if (!List.of("png", "jpg", "jpeg").contains(extension)) throw new NegocioException("Solo se permiten PNG, JPG o JPEG");
        try {
            byte[] bytes = archivo.getBytes();
            if (bytes.length == 0 || bytes.length > 5L * 1024 * 1024) {
                throw new NegocioException("La imagen QR debe pesar como máximo 5 MB");
            }
            String mimeReal = new org.apache.tika.Tika().detect(bytes, nombre);
            String mimeEsperado = "png".equals(extension) ? "image/png" : "image/jpeg";
            if (!mimeEsperado.equals(mimeReal) || (archivo.getContentType() != null
                    && !archivo.getContentType().isBlank() && !mimeEsperado.equalsIgnoreCase(archivo.getContentType()))) {
                throw new NegocioException("La extensión y el tipo de imagen no coinciden con el contenido");
            }
            if (!imagenValida(bytes)) {
                throw new NegocioException("El archivo no contiene una imagen válida");
            }
            return new QrProcesado(bytes, extension, mimeEsperado);
        } catch (IOException exception) {
            throw new NegocioException("No fue posible leer la imagen QR");
        }
    }

    private boolean imagenValida(byte[] bytes) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (input == null) return false;
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) return false;
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                long pixeles = (long) reader.getWidth(0) * reader.getHeight(0);
                return pixeles > 0 && pixeles <= 25_000_000L && reader.read(0) != null;
            } finally {
                reader.dispose();
            }
        }
    }

    private void registrarLimpiezaQr(String anterior, String nueva) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                if (anterior != null && !anterior.equals(nueva)) eliminarQrSeguro(anterior);
            }
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED && nueva != null) eliminarQrSeguro(nueva);
            }
        });
    }

    private void eliminarQrSeguro(String clave) {
        if (!AlmacenamientoArchivos.esClaveQrPago(clave)) return;
        try { almacenamientoArchivos.eliminar(clave); }
        catch (RuntimeException exception) { log.warn("No se pudo limpiar un QR reemplazado o compensatorio"); }
    }

    public record QrPagoArchivo(Resource recurso, MediaType tipoContenido, long longitud, boolean publico) {}
    private record QrProcesado(byte[] bytes, String extension, String mime) {}

    @Transactional
    public EventoDetalleResponse crear(CrearEventoRequest request) {
        Usuario organizador = exigirOrganizadorAprobado();
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
        if (!pagado) {
            String qrAnterior = evento.getQrPagoStorageKey();
            evento.setQrPagoStorageKey(null);
            evento.setQrPagoUrl(null);
            registrarLimpiezaQr(qrAnterior, null);
        }
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
                && esVacio(evento.getInstruccionesPago()) && evento.getQrPagoStorageKey() == null
                && esVacio(evento.getQrPagoUrl())) {
            throw new NegocioException("El evento de PAGO no tiene instrucciones ni QR de pago");
        }
        if (evento.getTipoInscripcion() == TipoInscripcion.PAGO && evento.getQrPagoStorageKey() == null)
            validarUrl(evento.getQrPagoUrl(), "QR de pago");
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
