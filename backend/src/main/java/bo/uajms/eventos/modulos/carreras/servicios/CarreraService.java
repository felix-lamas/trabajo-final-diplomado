package bo.uajms.eventos.modulos.carreras.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.modulos.carreras.dtos.ActualizarCarreraRequest;
import bo.uajms.eventos.modulos.carreras.dtos.CarreraResponse;
import bo.uajms.eventos.modulos.carreras.dtos.CrearCarreraRequest;
import bo.uajms.eventos.modulos.carreras.entidades.Carrera;
import bo.uajms.eventos.modulos.carreras.mappers.CarreraMapper;
import bo.uajms.eventos.modulos.carreras.repositorios.CarreraRepository;
import bo.uajms.eventos.modulos.facultades.entidades.Facultad;
import bo.uajms.eventos.modulos.facultades.repositorios.FacultadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarreraService {

    private final CarreraRepository carreraRepository;
    private final FacultadRepository facultadRepository;
    private final CarreraMapper carreraMapper;

    @Transactional(readOnly = true)
    public List<CarreraResponse> listarTodas() {
        return carreraRepository.findAll().stream()
                .map(carreraMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CarreraResponse> listarPorFacultad(UUID facultadId) {
        if (!facultadRepository.existsById(facultadId)) {
            throw new RecursoNoEncontradoException("Facultad", facultadId);
        }
        return carreraRepository.findByFacultadId(facultadId).stream()
                .map(carreraMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CarreraResponse buscarPorId(UUID id) {
        return carreraRepository.findById(id)
                .map(carreraMapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrera", id));
    }

    @Transactional
    public CarreraResponse crear(CrearCarreraRequest request) {
        Facultad facultad = facultadRepository.findById(request.getFacultadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Facultad", request.getFacultadId()));

        if (carreraRepository.existsByNombreIgnoreCaseAndFacultadId(request.getNombre(), request.getFacultadId())) {
            throw new NegocioException("Ya existe una carrera con ese nombre en esta facultad");
        }

        Carrera carrera = Carrera.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .estado("ACTIVO")
                .facultad(facultad)
                .build();

        return carreraMapper.toResponse(carreraRepository.save(carrera));
    }

    @Transactional
    public CarreraResponse actualizar(UUID id, ActualizarCarreraRequest request) {
        Carrera carrera = carreraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrera", id));

        Facultad facultad = facultadRepository.findById(request.getFacultadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Facultad", request.getFacultadId()));

        if (carreraRepository.existsByNombreIgnoreCaseAndFacultadIdAndIdNot(request.getNombre(), request.getFacultadId(), id)) {
            throw new NegocioException("Ya existe otra carrera con ese nombre en esta facultad");
        }

        carrera.setNombre(request.getNombre());
        carrera.setDescripcion(request.getDescripcion());
        carrera.setEstado(request.getEstado());
        carrera.setFacultad(facultad);

        return carreraMapper.toResponse(carreraRepository.save(carrera));
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!carreraRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Carrera", id);
        }
        carreraRepository.deleteById(id);
    }
}
