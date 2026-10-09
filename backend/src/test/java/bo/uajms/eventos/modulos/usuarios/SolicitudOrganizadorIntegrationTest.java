package bo.uajms.eventos.modulos.usuarios;

import bo.uajms.eventos.modulos.usuarios.entidades.*;
import bo.uajms.eventos.modulos.usuarios.repositorios.*;
import bo.uajms.eventos.modulos.usuarios.servicios.ProveedorCorreo;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.*;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in: only disposable databases on the dedicated local test cluster. */
@EnabledIfEnvironmentVariable(named="VIDIA_TEST_PG_URL", matches="jdbc:postgresql://127\\.0\\.0\\.1:55439/postgres")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
    "spring.config.import=", "spring.jpa.hibernate.ddl-auto=validate", "spring.jpa.show-sql=false",
    "app.seed.demo-password=OnlyLocalTest9!", "app.jwt.secreto=QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUE=",
    "app.frontend.verify-email-url=https://example.test/auth/verificar-correo", "app.frontend.admin-activation-url=",
    "app.storage.provider=local"})
@ActiveProfiles("organizer-application-test")
class SolicitudOrganizadorIntegrationTest {
    static final String DB="organizer_e2e_"+UUID.randomUUID().toString().replace("-", "");
    static final String CLAVE="OnlyLocalTest9!";
    @DynamicPropertySource static void propiedades(DynamicPropertyRegistry registry) throws Exception {
        try(var c=conexion();var s=c.createStatement()){s.execute("CREATE DATABASE "+DB);}
        registry.add("spring.datasource.url",()->url(DB));
        registry.add("spring.datasource.username",()->"vidia_test");registry.add("spring.datasource.password",()->"");
    }
    static Connection conexion() throws SQLException {return DriverManager.getConnection(System.getenv("VIDIA_TEST_PG_URL"),"vidia_test","");}
    static String url(String db){return "jdbc:postgresql://127.0.0.1:55439/"+db;}
    @Autowired TestRestTemplate http;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired UsuarioRolRepository usuarioRoles;
    @Autowired PasswordEncoder encoder;
    @MockBean ProveedorCorreo correo;
    record Cuenta(UUID id, String jwt) {}
    Cuenta cuenta(String... asignaciones) {
        String nombre="organizer_"+UUID.randomUUID().toString().replace("-", "");
        var usuario=Usuario.builder().correoElectronico(nombre+"@example.test").nombres("E2E").apellidos("Local")
            .ci(nombre.substring(0,20)).tipoUsuario(Usuario.TipoUsuario.EXTERNO).correoVerificado(true)
            .contrasena(encoder.encode(CLAVE)).build();
        usuarios.saveAndFlush(usuario);
        for(var nombreRol:List.of("USUARIO", "ORGANIZADOR", "ADMINISTRADOR")) { roles.findByNombre(nombreRol).orElseGet(() -> roles.saveAndFlush(Rol.builder().nombre(nombreRol).build())); }
        for(var nombreRol:asignaciones){var rol=roles.findByNombre(nombreRol).orElseGet(()->roles.saveAndFlush(Rol.builder().nombre(nombreRol).build()));
            usuarioRoles.saveAndFlush(UsuarioRol.builder().usuario(usuario).rol(rol).build());}
        var login=http.postForEntity("/api/v1/auth/login",Map.of("correoElectronico",usuario.getCorreoElectronico(),"contrasena",CLAVE),Map.class);
        assertEquals(200,login.getStatusCode().value());return new Cuenta(usuario.getId(),(String)login.getBody().get("token"));
    }
    ResponseEntity<Map> request(HttpMethod metodo,String path,Object body,Cuenta cuenta){
        var headers=new HttpHeaders();if(cuenta!=null)headers.setBearerAuth(cuenta.jwt());
        return http.exchange(path,metodo,new HttpEntity<>(body,headers),Map.class);
    }
    Map<String,Object> datos(){return new HashMap<>(Map.of("motivoSolicitud","Deseo organizar actividades educativas para la universidad", "tiposEventos",List.of("CURSOS_TALLERES","CONFERENCIAS_CHARLAS")));}
    ResponseEntity<Map> enviar(Cuenta cuenta,Map<String,Object> datos){return request(HttpMethod.POST,"/api/v1/usuarios/solicitud-organizador",datos,cuenta);}
    @Test void solicitudValidaPersisteDatosYEstadoPropio() {
        var participante=cuenta("USUARIO");var body=datos();body.put("informacionAdicional","Experiencia en talleres");
        var response=enviar(participante,body);assertEquals(200,response.getStatusCode().value());
        assertEquals("PENDIENTE",response.getBody().get("estado"));assertEquals(participante.id().toString(),response.getBody().get("usuarioId"));
        var propia=request(HttpMethod.GET,"/api/v1/usuarios/solicitud-organizador",null,participante);
        assertEquals(body.get("motivoSolicitud"),propia.getBody().get("motivoSolicitud"));
        assertEquals(new HashSet<>((List)body.get("tiposEventos")),new HashSet<>((List)propia.getBody().get("tiposEventos")));
        assertEquals(false,propia.getBody().get("puedeSolicitar"));
        assertEquals("Experiencia en talleres",propia.getBody().get("informacionAdicional"));
    }
    @ParameterizedTest @ValueSource(strings={"vacio","corto","largo","espacios","categoria","sinTipos","tipoNulo","duplicado","adicional"})
    void validaServidor(String caso) {
        var body=datos();switch(caso){
            case "vacio" -> body.put("motivoSolicitud","");
            case "corto" -> body.put("motivoSolicitud","Quiero organizar");
            case "largo" -> body.put("motivoSolicitud","a".repeat(1001));
            case "espacios" -> body.put("motivoSolicitud"," ".repeat(30));
            case "categoria" -> body.put("tiposEventos",List.of("ARBITRARIA"));
            case "sinTipos" -> body.put("tiposEventos",List.of());
            case "tipoNulo" -> body.put("tiposEventos",Arrays.asList((String)null));
            case "duplicado" -> body.put("tiposEventos",List.of("CURSOS_TALLERES","CURSOS_TALLERES"));
            case "adicional" -> body.put("informacionAdicional","a".repeat(1001));
        }
        assertEquals(400,enviar(cuenta("USUARIO"),body).getStatusCode().value());
    }
    @Test void opcionalOmitidoYDuplicadaBloqueada(){var c=cuenta("USUARIO");var r=enviar(c,datos());assertEquals(200,r.getStatusCode().value());assertNull(r.getBody().get("informacionAdicional"));assertEquals(400,enviar(c,datos()).getStatusCode().value());}
    @Test void rechazoYReenvioConservanHistorialYAprobacionDual(){
        var c=cuenta("USUARIO");var admin=cuenta("ADMINISTRADOR");var original=datos();enviar(c,original);
        var base="/api/v1/usuarios/solicitudes-organizador/"+c.id();
        assertEquals(200,request(HttpMethod.PATCH,base+"/rechazar",Map.of("motivo","Completa la propuesta"),admin).getStatusCode().value());
        var propia=request(HttpMethod.GET,"/api/v1/usuarios/solicitud-organizador",null,c);
        assertEquals("Completa la propuesta",propia.getBody().get("motivoRechazo"));assertEquals(true,propia.getBody().get("puedeSolicitar"));
        var nueva=datos();nueva.put("motivoSolicitud","Nueva propuesta para cursos y talleres universitarios");assertEquals(200,enviar(c,nueva).getStatusCode().value());
        var headers=new HttpHeaders();headers.setBearerAuth(admin.jwt());
        var historial=http.exchange("/api/v1/usuarios/solicitudes-organizador?estado=RECHAZADA",HttpMethod.GET,new HttpEntity<>(headers),List.class);
        assertTrue(historial.getBody().stream().anyMatch(item -> c.id().toString().equals(((Map)item).get("usuarioId")) && original.get("motivoSolicitud").equals(((Map)item).get("motivoSolicitud"))));
        assertEquals(200,request(HttpMethod.PATCH,base+"/aprobar",null,admin).getStatusCode().value());
        assertEquals(Set.of("USUARIO","ORGANIZADOR"),new HashSet<>((List)request(HttpMethod.GET,"/api/v1/usuarios/perfil",null,c).getBody().get("roles")));
        assertEquals(c.id().toString(),request(HttpMethod.GET,"/api/v1/usuarios/solicitud-organizador",null,c).getBody().get("usuarioId"));
    }
    @ParameterizedTest @ValueSource(strings={"ADMINISTRADOR","ORGANIZADOR"})
    void rolesNoParticipantesBloqueados(String rol){var c=cuenta(rol);assertEquals(403,enviar(c,datos()).getStatusCode().value());assertEquals(403,request(HttpMethod.GET,"/api/v1/usuarios/solicitud-organizador",null,c).getStatusCode().value());}
    @Test void idorNoAceptaOtroUsuarioNiPermiteAdministrar(){
        var c=cuenta("USUARIO");var otro=cuenta("USUARIO");var body=datos();body.put("usuarioId",otro.id());
        assertEquals(c.id().toString(),enviar(c,body).getBody().get("usuarioId"));
        assertEquals("NINGUNA",request(HttpMethod.GET,"/api/v1/usuarios/solicitud-organizador",null,otro).getBody().get("estado"));
        assertEquals(403,request(HttpMethod.PATCH,"/api/v1/usuarios/solicitudes-organizador/"+otro.id()+"/aprobar",null,c).getStatusCode().value());
        assertEquals(403,request(HttpMethod.GET,"/api/v1/usuarios/"+otro.id(),null,c).getStatusCode().value());
        assertEquals(401,request(HttpMethod.GET,"/api/v1/usuarios/solicitud-organizador",null,null).getStatusCode().value());
    }
    @Test void openApiDocumentaContrato(){var response=http.getForEntity("/v3/api-docs",Map.class);assertEquals(200,response.getStatusCode().value());var paths=(Map)response.getBody().get("paths");assertTrue(paths.containsKey("/api/v1/usuarios/solicitud-organizador"));assertTrue(paths.containsKey("/api/v1/usuarios/solicitud-organizador/tipos-eventos"));assertEquals(200,http.getForEntity("/swagger-ui/index.html",String.class).getStatusCode().value());}
    @ParameterizedTest @ValueSource(strings={"clean","1","2","3","4","historical"})
    void migracionV5LimpiaEHistorica(String punto) throws Exception {
        String db="org_mig_"+UUID.randomUUID().toString().replace("-", "");try(var c=conexion();var s=c.createStatement()){s.execute("CREATE DATABASE "+db);}
        try{
            if(!punto.equals("clean"))Flyway.configure().dataSource(url(db),"vidia_test","").locations("classpath:db/migration").target(punto.equals("historical")?"4":punto).load().migrate();
            UUID id=UUID.randomUUID();
            if(punto.equals("historical"))try(var c=DriverManager.getConnection(url(db),"vidia_test","");var s=c.prepareStatement("INSERT INTO usuario(id,correo_electronico,contrasena,nombres,apellidos,ci,tipo_usuario,correo_verificado,estado_solicitud_organizador,motivo_rechazo_organizador) VALUES (?, 'historico@example.test','hash','E2E','Local','HIST-ORG','EXTERNO',true,'RECHAZADA','Motivo anterior')")){s.setObject(1,id);s.executeUpdate();}
            var flyway=Flyway.configure().dataSource(url(db),"vidia_test","").locations("classpath:db/migration").load();flyway.migrate();flyway.validate();assertEquals("5",flyway.info().current().getVersion().toString());
            try(var c=DriverManager.getConnection(url(db),"vidia_test","");var s=c.createStatement()){
                s.executeQuery("SELECT usuario_id,tipo_evento FROM usuario_solicitud_tipos_evento LIMIT 0");
                s.executeQuery("SELECT detalle FROM solicitud_organizador_historial LIMIT 0");
                if(punto.equals("historical")){var result=s.executeQuery("SELECT id,motivo_solicitud_organizador,informacion_adicional_organizador,motivo_rechazo_organizador,estado_solicitud_organizador FROM usuario");assertTrue(result.next());assertEquals(id,result.getObject(1));assertNull(result.getString(2));assertNull(result.getString(3));assertEquals("Motivo anterior",result.getString(4));assertEquals("RECHAZADA",result.getString(5));}
            }
        }finally{try(var c=conexion();var s=c.createStatement()){s.execute("DROP DATABASE "+db+" WITH (FORCE)");}}
    }
    @AfterAll static void limpiar() throws Exception {try(var c=conexion();var s=c.createStatement()){s.execute("DROP DATABASE "+DB+" WITH (FORCE)");}}
}
