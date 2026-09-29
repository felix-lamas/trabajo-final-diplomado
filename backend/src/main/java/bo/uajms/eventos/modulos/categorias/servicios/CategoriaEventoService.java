package bo.uajms.eventos.modulos.categorias.servicios;

import bo.uajms.eventos.core.excepciones.CodigosError;
import bo.uajms.eventos.core.excepciones.ConflictoException;
import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.modulos.categorias.dtos.ActualizarCategoriaEventoRequest;
import bo.uajms.eventos.modulos.categorias.dtos.CategoriaEventoResponse;
import bo.uajms.eventos.modulos.categorias.dtos.CrearCategoriaEventoRequest;
import bo.uajms.eventos.modulos.categorias.entidades.CategoriaEvento;
import bo.uajms.eventos.modulos.categorias.repositorios.CategoriaEventoRepository;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoriaEventoService {

    private final CategoriaEventoRepository categoriaRepository;
    private final EventoRepository eventoRepository;

    @Transactional(readOnly = true)
    public List<CategoriaEventoResponse> listarTodas() {
        return categoriaRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoriaEventoResponse> listarActivas() {
        return categoriaRepository.findByEstado("ACTIVO").stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaEventoResponse buscarPorId(UUID id) {
        return categoriaRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría de evento", id));
    }

    @Transactional
    public CategoriaEventoResponse crear(CrearCategoriaEventoRequest request) {
        String nombre = normalizarYValidarNombre(request.getNombre());
        validarNombreDisponible(nombre, null);

        CategoriaEvento categoria = CategoriaEvento.builder()
                .nombre(nombre)
                .nombreNormalizado(normalizarParaUnicidad(nombre))
                .descripcion(normalizarDescripcion(request.getDescripcion()))
                .estado("ACTIVO")
                .build();

        return guardar(categoria);
    }

    @Transactional
    public CategoriaEventoResponse actualizar(UUID id, ActualizarCategoriaEventoRequest request) {
        CategoriaEvento categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría de evento", id));
        String nombre = normalizarYValidarNombre(request.getNombre());
        validarNombreDisponible(nombre, id);

        categoria.setNombre(nombre);
        categoria.setNombreNormalizado(normalizarParaUnicidad(nombre));
        categoria.setDescripcion(normalizarDescripcion(request.getDescripcion()));
        categoria.setEstado(request.getEstado());

        return guardar(categoria);
    }

    @Transactional
    public void eliminar(UUID id) {
        CategoriaEvento categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría de evento", id));
        if (eventoRepository.existsByCategoriaId(id)) {
            throw new ConflictoException(CodigosError.CONFLICT,
                    "No se puede eliminar la categoría porque está siendo utilizada por eventos");
        }
        categoriaRepository.delete(categoria);
    }

    private void validarNombreDisponible(String nombre, UUID idActual) {
        String nombreNormalizado = normalizarParaUnicidad(nombre);
        boolean existe = idActual == null
                ? categoriaRepository.existsByNombreNormalizado(nombreNormalizado)
                : categoriaRepository.existsByNombreNormalizadoAndIdNot(nombreNormalizado, idActual);
        if (existe) {
            throw conflictoNombre(nombre);
        }
    }

    private CategoriaEventoResponse guardar(CategoriaEvento categoria) {
        try {
            return mapToResponse(categoriaRepository.saveAndFlush(categoria));
        } catch (DataIntegrityViolationException ex) {
            throw conflictoNombre(categoria.getNombre());
        }
    }

    private ConflictoException conflictoNombre(String nombre) {
        return new ConflictoException(CodigosError.CONFLICT,
                "Ya existe una categoría con el nombre: " + nombre);
    }

    private String normalizarYValidarNombre(String nombre) {
        String normalizado = nombre == null ? "" : nombre.trim();
        if (normalizado.length() < 3 || normalizado.length() > 100) {
            throw new NegocioException(CodigosError.VALIDATION_ERROR,
                    "El nombre debe tener entre 3 y 100 caracteres después de eliminar espacios externos");
        }
        return normalizado;
    }

    private String normalizarParaUnicidad(String nombre) {
        return nombre.toLowerCase(Locale.ROOT);
    }

    private String normalizarDescripcion(String descripcion) {
        if (descripcion == null) {
            return null;
        }
        String normalizada = descripcion.trim();
        return normalizada.isEmpty() ? null : normalizada;
    }

    private CategoriaEventoResponse mapToResponse(CategoriaEvento entity) {
        return CategoriaEventoResponse.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .estado(entity.getEstado())
                .build();
    }
}
