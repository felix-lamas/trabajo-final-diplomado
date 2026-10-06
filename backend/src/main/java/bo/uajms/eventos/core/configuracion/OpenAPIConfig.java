package bo.uajms.eventos.core.configuracion;

import bo.uajms.eventos.core.controladores.SaludController;
import bo.uajms.eventos.core.excepciones.ErrorRespuesta;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.validation.Valid;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class OpenAPIConfig {

    private static final Pattern ROL_EN_EXPRESION =
            Pattern.compile("'(ADMINISTRADOR|ORGANIZADOR|USUARIO)'");

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"));
        ModelConverters.getInstance().read(ErrorRespuesta.class).forEach(components::addSchemas);

        return new OpenAPI()
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Desarrollo local"),
                        new Server().url("https://trabajo-final-diplomado.onrender.com").description("Produccion")
                ))
                .info(new Info()
                        .title("Vidia - API de Gestion de Eventos UAJMS")
                        .version("1.0")
                        .description("Contrato canonico bajo /api/v1. La interfaz Swagger es publica, "
                                + "pero las operaciones funcionales conservan autenticacion JWT, roles y ownership.")
                        .contact(new Contact()
                                .name("Soporte Tecnico UAJMS")
                                .email("soporte@uajms.edu.bo")));
    }

    @Bean
    public OperationCustomizer respuestasErrorComunes() {
        return (operation, handlerMethod) -> {
            boolean publico = handlerMethod.hasMethodAnnotation(
                    io.swagger.v3.oas.annotations.security.SecurityRequirements.class)
                    || handlerMethod.getBeanType().isAnnotationPresent(
                    io.swagger.v3.oas.annotations.security.SecurityRequirements.class);
            boolean validaEntrada = Arrays.stream(handlerMethod.getMethodParameters())
                    .anyMatch(parameter -> parameter.hasParameterAnnotation(Valid.class));

            documentarAcceso(operation, handlerMethod, publico);
            if (SaludController.class.isAssignableFrom(handlerMethod.getBeanType())) {
                operation.setSecurity(List.of());
            }

            if (validaEntrada) {
                agregarRespuesta(operation, "400", "Datos de entrada o regla de negocio invalidos");
            }
            if (!publico) {
                agregarRespuesta(operation, "401", "Autenticacion requerida o token invalido");
                agregarRespuesta(operation, "403", "Rol u ownership insuficiente");
            }
            agregarRespuesta(operation, "500", "Error interno sin detalles tecnicos");
            operation.getResponses().forEach((codigo, respuesta) -> {
                if (codigo.matches("[45]\\d\\d")
                        && (respuesta.getContent() == null || respuesta.getContent().isEmpty())) {
                    respuesta.setContent(contenidoError());
                }
            });
            return operation;
        };
    }

    private void agregarRespuesta(io.swagger.v3.oas.models.Operation operation, String codigo, String descripcion) {
        if (operation.getResponses().containsKey(codigo)) {
            return;
        }
        operation.getResponses().addApiResponse(codigo,
                new ApiResponse().description(descripcion).content(contenidoError()));
    }

    private Content contenidoError() {
        return new Content().addMediaType("application/json",
                new io.swagger.v3.oas.models.media.MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ErrorRespuesta")));
    }

    private void documentarAcceso(io.swagger.v3.oas.models.Operation operation,
                                  org.springframework.web.method.HandlerMethod handlerMethod,
                                  boolean publico) {
        String acceso;
        if (publico) {
            acceso = "Acceso publico; no requiere JWT.";
        } else {
            PreAuthorize autorizacion = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getMethod(), PreAuthorize.class);
            if (autorizacion == null) {
                autorizacion = AnnotatedElementUtils.findMergedAnnotation(
                        handlerMethod.getBeanType(), PreAuthorize.class);
            }
            Set<String> roles = new LinkedHashSet<>();
            if (autorizacion != null) {
                Matcher matcher = ROL_EN_EXPRESION.matcher(autorizacion.value());
                while (matcher.find()) {
                    roles.add(matcher.group(1));
                }
            }
            acceso = roles.isEmpty()
                    ? "Requiere JWT. Roles: ADMINISTRADOR, ORGANIZADOR o USUARIO."
                    : "Requiere JWT. Roles: " + String.join(", ", roles) + ".";
        }

        String descripcion = operation.getDescription();
        if (descripcion == null || descripcion.isBlank()) {
            descripcion = operation.getSummary() == null
                    ? "Operacion del contrato API Vidia."
                    : operation.getSummary() + ".";
        }
        operation.setDescription(descripcion + "\n\n" + acceso);
    }
}
