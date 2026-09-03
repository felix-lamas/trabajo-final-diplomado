package bo.uajms.eventos.modulos.facultades.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.modulos.facultades.dtos.ActualizarFacultadRequest;
import bo.uajms.eventos.modulos.facultades.dtos.CrearFacultadRequest;
import bo.uajms.eventos.modulos.facultades.dtos.FacultadResponse;
import bo.uajms.eventos.modulos.facultades.entidades.Facultad;
import bo.uajms.eventos.modulos.facultades.mappers.FacultadMapper;
import bo.uajms.eventos.modulos.facultades.repositorios.FacultadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacultadService {

    private final FacultadRepository facultadRepository;
    private final FacultadMapper facultadMapper;

    @Transactional(readOnly = true)
    public List<FacultadResponse> listarTodas() {
        return facultadRepository.findAll().stream()
                .map(facultadMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FacultadResponse buscarPorId(UUID id) {
        return facultadRepository.findById(id)
                .map(facultadMapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Facultad", id));
    }

    @Transactional
    public FacultadResponse crear(CrearFacultadRequest request) {
        if (facultadRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new NegocioException("Ya existe una facultad con el nombre: " + request.getNombre());
        }

        Facultad facultad = Facultad.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .estado("ACTIVO")
                .build();

        return facultadMapper.toResponse(facultadRepository.save(facultad));
    }

    @Transactional
    public FacultadResponse actualizar(UUID id, ActualizarFacultadRequest request) {
        Facultad facultad = facultadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Facultad", id));

        if (facultadRepository.existsByNombreIgnoreCaseAndIdNot(request.getNombre(), id)) {
            throw new NegocioException("Ya existe otra facultad con el nombre: " + request.getNombre());
        }

        facultad.setNombre(request.getNombre());
        facultad.setDescripcion(request.getDescripcion());
        facultad.setEstado(request.getEstado());

        return facultadMapper.toResponse(facultadRepository.save(facultad));
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!facultadRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Facultad", id);
        }
        facultadRepository.deleteById(id);
    }
}
