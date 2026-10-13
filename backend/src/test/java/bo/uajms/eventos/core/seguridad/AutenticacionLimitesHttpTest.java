package bo.uajms.eventos.core.seguridad;
import bo.uajms.eventos.modulos.usuarios.controladores.AutenticacionControlador;
import bo.uajms.eventos.modulos.usuarios.servicios.AutenticacionServicio;
import bo.uajms.eventos.core.excepciones.ManejadorGlobalExcepciones;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class AutenticacionLimitesHttpTest {
 @ParameterizedTest @CsvSource({"login,correoElectronico","recuperar-contrasena,correoElectronico","reenviar-verificacion,correoElectronico","verificar-correo,token","restablecer-contrasena,token"})
 void responde429AntesDelNegocio(String ruta,String campo) throws Exception {
  var servicio=mock(AutenticacionServicio.class);var limites=new IntentosAutenticacion(1,100,60,100);
  var mvc=MockMvcBuilders.standaloneSetup(new AutenticacionControlador(servicio,limites)).setControllerAdvice(new ManejadorGlobalExcepciones()).build();
  String body=campo.equals("token")?"{\"token\":\"temporal\"}":"{\"correoElectronico\":\"anonimo@example.test\",\"contrasena\":\"PruebaSegura9!\"}";
  if(ruta.equals("restablecer-contrasena")) body="{\"token\":\"temporal\",\"nuevaContrasena\":\"NuevaSegura2026!\",\"confirmacion\":\"NuevaSegura2026!\"}";
  mvc.perform(post("/api/v1/auth/"+ruta).contentType("application/json").content(body)).andExpect(status().isOk());
  clearInvocations(servicio);
  mvc.perform(post("/api/v1/auth/"+ruta).contentType("application/json").content(body)).andExpect(status().isTooManyRequests()).andExpect(header().exists("Retry-After")).andExpect(jsonPath("$.codigo").value("AUTH_RATE_LIMITED"));
  verifyNoInteractions(servicio);
 }
}
