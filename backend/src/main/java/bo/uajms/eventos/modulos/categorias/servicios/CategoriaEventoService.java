package bo.uajms.eventos.modulos.categorias.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.modulos.categorias.dtos.ActualizarCategoriaEventoRequest;
import bo.uajms.eventos.modulos.categorias.dtos.CategoriaEventoResponse;
import bo.uajms.eventos.modulos.categorias.dtos.CrearCategoriaEventoRequest;
import bo.uajms.eventos.modulos.categorias.entidades.CategoriaEvento;
import bo.uajms.eventos.modulos.categorias.repositorios.CategoriaEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaEventoService {

    private final CategoriaEventoRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaEventoResponse> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaEventoResponse> listarActivas() {
        return categoriaRepository.findByEstado("ACTIVO").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoriaEventoResponse buscarPorId(UUID id) {
        return categoriaRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría de Evento", id));
    }

    @Transactional
    public CategoriaEventoResponse crear(CrearCategoriaEventoRequest request) {
        if (categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new NegocioException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        CategoriaEvento categoria = CategoriaEvento.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .estado("ACTIVO")
                .build();

        return mapToResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaEventoResponse actualizar(UUID id, ActualizarCategoriaEventoRequest request) {
        CategoriaEvento categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría de Evento", id));

        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(request.getNombre(), id)) {
            throw new NegocioException("Ya existe otra categoría con el nombre: " + request.getNombre());
        }

        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        categoria.setEstado(request.getEstado());

        return mapToResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Categoría de Evento", id);
        }
        categoriaRepository.deleteById(id);
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
