package bo.uajms.eventos.core.configuracion;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.core.converter.ModelConverters;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import bo.uajms.eventos.core.excepciones.ErrorRespuesta;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"));
        ModelConverters.getInstance().read(ErrorRespuesta.class)
                .forEach(components::addSchemas);
        return new OpenAPI()
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("API de Gestión de Eventos UAJMS")
                        .version("1.0")
                        .description("Documentación de los servicios REST para la plataforma de eventos universitarios.")
                        .contact(new Contact()
                                .name("Soporte Técnico UAJMS")
                                .email("soporte@uajms.edu.bo")));
    }

    @Bean
    public OperationCustomizer respuestasErrorComunes() {
        return (operation, handlerMethod) -> {
            boolean publico = handlerMethod.hasMethodAnnotation(
                    io.swagger.v3.oas.annotations.security.SecurityRequirements.class)
                    || handlerMethod.getBeanType().isAnnotationPresent(
                    io.swagger.v3.oas.annotations.security.SecurityRequirements.class);
            agregarRespuesta(operation, "400", "Solicitud o regla de negocio invalida");
            if (!publico) {
                agregarRespuesta(operation, "401", "Autenticacion requerida o token invalido");
                agregarRespuesta(operation, "403", "Rol sin permisos suficientes");
            }
            agregarRespuesta(operation, "404", "Recurso inexistente o fuera del alcance");
            agregarRespuesta(operation, "409", "Conflicto con el estado actual del recurso");
            agregarRespuesta(operation, "500", "Error interno sin detalles tecnicos");
            return operation;
        };
    }

    private void agregarRespuesta(io.swagger.v3.oas.models.Operation operation, String codigo, String descripcion) {
        if (operation.getResponses().containsKey(codigo)) return;
        Content content = new Content().addMediaType("application/json",
                new io.swagger.v3.oas.models.media.MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ErrorRespuesta")));
        operation.getResponses().addApiResponse(codigo,
                new ApiResponse().description(descripcion).content(content));
    }
}
