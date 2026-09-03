package bo.uajms.eventos.modulos.credenciales.mappers;

import bo.uajms.eventos.modulos.credenciales.dtos.CredencialResponse;
import bo.uajms.eventos.modulos.credenciales.entidades.Credencial;
import org.springframework.stereotype.Component;

@Component
public class CredencialMapper {

    public CredencialResponse toResponse(Credencial credencial) {
        if (credencial == null) return null;

        return CredencialResponse.builder()
                .id(credencial.getId())
                .usuarioId(credencial.getUsuario().getId())
                .usuarioNombre(credencial.getUsuario().getNombres() + " " + credencial.getUsuario().getApellidos())
                .eventoId(credencial.getEvento().getId())
                .eventoTitulo(credencial.getEvento().getTitulo())
                .inscripcionId(credencial.getInscripcion().getId())
                .codigoParticipante(credencial.getCodigoParticipante())
                .fechaGeneracion(credencial.getFechaGeneracion())
                .estado(credencial.getEstado())
                .build();
    }
}
