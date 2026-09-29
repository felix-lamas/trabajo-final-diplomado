package bo.uajms.eventos.modulos.categorias.servicios;

import bo.uajms.eventos.core.excepciones.ConflictoException;
import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.modulos.categorias.dtos.ActualizarCategoriaEventoRequest;
import bo.uajms.eventos.modulos.categorias.dtos.CategoriaEventoResponse;
import bo.uajms.eventos.modulos.categorias.dtos.CrearCategoriaEventoRequest;
import bo.uajms.eventos.modulos.categorias.entidades.CategoriaEvento;
import bo.uajms.eventos.modulos.categorias.repositorios.CategoriaEventoRepository;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaEventoServiceTest {

    @Mock CategoriaEventoRepository categoriaRepository;
    @Mock EventoRepository eventoRepository;
    private CategoriaEventoService service;
    private UUID id;
    private CategoriaEvento categoria;

    @BeforeEach
    void setUp() {
        service = new CategoriaEventoService(categoriaRepository, eventoRepository);
        id = UUID.randomUUID();
        categoria = CategoriaEvento.builder()
                .nombre("Conferencia")
                .nombreNormalizado("conferencia")
                .descripcion("Académica")
                .estado("ACTIVO")
                .build();
        categoria.setId(id);
        lenient().when(categoriaRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void listaCategoriasComoDtos() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));
        List<CategoriaEventoResponse> respuesta = service.listarTodas();
        assertEquals(1, respuesta.size());
        assertEquals(id, respuesta.getFirst().getId());
        assertEquals("Conferencia", respuesta.getFirst().getNombre());
    }

    @Test
    void listaSoloCategoriasActivas() {
        when(categoriaRepository.findByEstado("ACTIVO")).thenReturn(List.of(categoria));
        assertEquals(1, service.listarActivas().size());
        verify(categoriaRepository).findByEstado("ACTIVO");
    }

    @Test
    void obtieneCategoriaComoDto() {
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoria));
        CategoriaEventoResponse respuesta = service.buscarPorId(id);
        assertEquals(id, respuesta.getId());
        assertEquals(CategoriaEventoResponse.class, respuesta.getClass());
    }

    @Test
    void obtenerIdInexistenteFalla() {
        when(categoriaRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.buscarPorId(id));
    }

    @Test
    void creaConNombreYDescripcionRecortados() {
        CrearCategoriaEventoRequest request = crear("  Conferencia  ");
        request.setDescripcion("  Actividad académica  ");
        CategoriaEventoResponse respuesta = service.crear(request);

        ArgumentCaptor<CategoriaEvento> captor = ArgumentCaptor.forClass(CategoriaEvento.class);
        verify(categoriaRepository).saveAndFlush(captor.capture());
        assertEquals("Conferencia", captor.getValue().getNombre());
        assertEquals("conferencia", captor.getValue().getNombreNormalizado());
        assertEquals("Actividad académica", respuesta.getDescripcion());
        assertEquals("ACTIVO", respuesta.getEstado());
    }

    @Test
    void nombreVacioDespuesDeTrimFalla() {
        assertThrows(NegocioException.class, () -> service.crear(crear("   ")));
        verify(categoriaRepository, never()).saveAndFlush(any());
    }

    @Test
    void nombreMenorAlMinimoDespuesDeTrimFalla() {
        assertThrows(NegocioException.class, () -> service.crear(crear(" ab ")));
    }

    @Test
    void duplicadoPorMayusculasFalla() {
        when(categoriaRepository.existsByNombreNormalizado("conferencia")).thenReturn(true);
        assertThrows(ConflictoException.class, () -> service.crear(crear("CONFERENCIA")));
    }

    @Test
    void duplicadoConEspaciosFalla() {
        when(categoriaRepository.existsByNombreNormalizado("conferencia")).thenReturn(true);
        assertThrows(ConflictoException.class, () -> service.crear(crear(" conferencia ")));
    }

    @Test
    void restriccionFisicaResuelveCarreraConcurrente() {
        when(categoriaRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unique"));
        assertThrows(ConflictoException.class, () -> service.crear(crear("Conferencia")));
    }

    @Test
    void actualizaYRespetaUnicidadNormalizada() {
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoria));
        ActualizarCategoriaEventoRequest request = actualizar("  Taller  ");
        CategoriaEventoResponse respuesta = service.actualizar(id, request);
        assertEquals("Taller", respuesta.getNombre());
        assertEquals("taller", categoria.getNombreNormalizado());
        verify(categoriaRepository).existsByNombreNormalizadoAndIdNot("taller", id);
    }

    @Test
    void actualizacionDuplicadaFalla() {
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreNormalizadoAndIdNot("taller", id)).thenReturn(true);
        assertThrows(ConflictoException.class, () -> service.actualizar(id, actualizar("TALLER")));
    }

    @Test
    void actualizacionInexistenteFalla() {
        when(categoriaRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(id, actualizar("Taller")));
    }

    @Test
    void eliminaCategoriaNoReferenciada() {
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoria));
        service.eliminar(id);
        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void eliminacionInexistenteFalla() {
        when(categoriaRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(id));
    }

    @Test
    void categoriaReferenciadaPorEventoNoSeElimina() {
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoria));
        when(eventoRepository.existsByCategoriaId(id)).thenReturn(true);
        assertThrows(ConflictoException.class, () -> service.eliminar(id));
        verify(categoriaRepository, never()).delete(any());
    }

    private CrearCategoriaEventoRequest crear(String nombre) {
        CrearCategoriaEventoRequest request = new CrearCategoriaEventoRequest();
        request.setNombre(nombre);
        return request;
    }

    private ActualizarCategoriaEventoRequest actualizar(String nombre) {
        ActualizarCategoriaEventoRequest request = new ActualizarCategoriaEventoRequest();
        request.setNombre(nombre);
        request.setDescripcion("Descripción");
        request.setEstado("ACTIVO");
        return request;
    }
}
