package bo.uajms.eventos.modulos.eventos.servicios;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventoService {

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
            UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
            eventos = eventoRepository.findByOrganizadorIdOrEstado(organizadorId, EstadoEvento.PUBLICADO);
        } else {
            eventos = eventoRepository.findByEstado(EstadoEvento.PUBLICADO);
        }

        return eventos.stream()
                .map(eventoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarPublicados() {
        return eventoRepository.findByEstado(EstadoEvento.PUBLICADO).stream()
                .map(eventoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarPorCategoria(UUID categoriaId) {
        List<Evento> eventos;

        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            eventos = eventoRepository.findByCategoriaId(categoriaId);
        } else if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
            eventos = eventoRepository.findVisiblesPorCategoriaParaOrganizador(
                    categoriaId,
                    organizadorId,
                    EstadoEvento.PUBLICADO
            );
        } else {
            eventos = eventoRepository.findByCategoriaIdAndEstado(categoriaId, EstadoEvento.PUBLICADO);
        }

        return eventos.stream()
                .map(eventoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventoDetalleResponse buscarPorId(UUID id) {
        return buscarEventoVisible(id)
                .map(eventoMapper::toDetalleResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", id));
    }

    @Transactional
    public EventoDetalleResponse crear(CrearEventoRequest request) {
        CategoriaEvento categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", request.getCategoriaId()));

        validarCosto(request.getTipoInscripcion(), request.getCosto());

        Usuario organizador = usuarioAutenticadoService.obtenerUsuario();

        Evento evento = Evento.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .objetivos(request.getObjetivos())
                .categoria(categoria)
                .modalidad(request.getModalidad())
                .tipoInscripcion(request.getTipoInscripcion())
                .costo(request.getCosto())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .ubicacion(request.getUbicacion())
                .enlaceVirtual(request.getEnlaceVirtual())
                .cupoMaximo(request.getCupoMaximo())
                .cupoDisponible(request.getCupoMaximo())
                .estado(EstadoEvento.BORRADOR)
                .imagenPortada(request.getImagenPortada())
                .organizador(organizador)
                .build();

        return eventoMapper.toDetalleResponse(eventoRepository.save(evento));
    }

    @Transactional
    public EventoDetalleResponse actualizar(UUID id, ActualizarEventoRequest request) {
        Evento evento = obtenerEventoGestionable(id);

        if (evento.getEstado() != EstadoEvento.BORRADOR) {
            throw new NegocioException("Solo se pueden editar eventos en estado BORRADOR");
        }

        CategoriaEvento categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", request.getCategoriaId()));

        validarCosto(request.getTipoInscripcion(), request.getCosto());

        evento.setTitulo(request.getTitulo());
        evento.setDescripcion(request.getDescripcion());
        evento.setObjetivos(request.getObjetivos());
        evento.setCategoria(categoria);
        evento.setModalidad(request.getModalidad());
        evento.setTipoInscripcion(request.getTipoInscripcion());
        evento.setCosto(request.getCosto());
        evento.setFechaInicio(request.getFechaInicio());
        evento.setFechaFin(request.getFechaFin());
        evento.setHoraInicio(request.getHoraInicio());
        evento.setHoraFin(request.getHoraFin());
        evento.setUbicacion(request.getUbicacion());
        evento.setEnlaceVirtual(request.getEnlaceVirtual());
        
        // Ajustar cupo disponible si cambia el máximo
        int diferencia = request.getCupoMaximo() - evento.getCupoMaximo();
        evento.setCupoMaximo(request.getCupoMaximo());
        evento.setCupoDisponible(evento.getCupoDisponible() + diferencia);
        
        if (evento.getCupoDisponible() < 0) {
            throw new NegocioException("El nuevo cupo máximo es menor que las inscripciones ya realizadas");
        }

        evento.setImagenPortada(request.getImagenPortada());

        return eventoMapper.toDetalleResponse(eventoRepository.save(evento));
    }

    @Transactional
    public void publicar(UUID id) {
        Evento evento = obtenerEventoAdministrable(id);
        if (evento.getEstado() != EstadoEvento.BORRADOR) {
            throw new NegocioException("Solo se pueden publicar eventos en estado BORRADOR");
        }
        evento.setEstado(EstadoEvento.PUBLICADO);
        eventoRepository.save(evento);
    }

    @Transactional
    public void cancelar(UUID id) {
        Evento evento = obtenerEventoGestionable(id);
        evento.setEstado(EstadoEvento.CANCELADO);
        eventoRepository.save(evento);
    }

    @Transactional
    public void finalizar(UUID id) {
        Evento evento = obtenerEventoGestionable(id);
        evento.setEstado(EstadoEvento.FINALIZADO);
        eventoRepository.save(evento);
    }

    @Transactional
    public void eliminar(UUID id) {
        Evento evento = obtenerEventoGestionable(id);
        if (evento.getEstado() != EstadoEvento.BORRADOR) {
            throw new NegocioException("No se puede eliminar un evento que ya ha sido publicado");
        }
        eventoRepository.delete(evento);
    }

    private void validarCosto(TipoInscripcion tipo, BigDecimal costo) {
        if (tipo == TipoInscripcion.GRATUITO && costo.compareTo(BigDecimal.ZERO) != 0) {
            throw new NegocioException("Si el evento es GRATUITO, el costo debe ser 0");
        }
        if (tipo == TipoInscripcion.PAGO && costo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("Si el evento es de PAGO, el costo debe ser mayor a 0");
        }
    }

    private Optional<Evento> buscarEventoVisible(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return eventoRepository.findById(id);
        }

        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
            return eventoRepository.findByIdAndOrganizadorId(id, organizadorId)
                    .or(() -> eventoRepository.findByIdAndEstado(id, EstadoEvento.PUBLICADO));
        }

        return eventoRepository.findByIdAndEstado(id, EstadoEvento.PUBLICADO);
    }

    private Evento obtenerEventoGestionable(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return eventoRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Evento", id));
        }

        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
            return eventoRepository.findByIdAndOrganizadorId(id, organizadorId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Evento", id));
        }

        throw new RecursoNoEncontradoException("Evento", id);
    }

    private Evento obtenerEventoAdministrable(UUID id) {
        if (!usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            throw new RecursoNoEncontradoException("Evento", id);
        }

        return eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", id));
    }
}
