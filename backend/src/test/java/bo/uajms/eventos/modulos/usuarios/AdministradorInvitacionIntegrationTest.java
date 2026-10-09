package bo.uajms.eventos.modulos.usuarios;

import bo.uajms.eventos.modulos.usuarios.entidades.*;
import bo.uajms.eventos.modulos.usuarios.repositorios.*;
import bo.uajms.eventos.modulos.usuarios.servicios.ProveedorCorreo;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Runs ONLY against an explicitly enabled disposable local PostgreSQL instance. */
@EnabledIfEnvironmentVariable(named="VIDIA_TEST_PG_URL", matches="jdbc:postgresql://127\\.0\\.0\\.1:55439/postgres")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
    "spring.config.import=", "spring.jpa.hibernate.ddl-auto=validate", "spring.jpa.show-sql=false",
    "app.seed.demo-password=OnlyLocalTest9!", "app.jwt.secreto=QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUE=",
    "app.frontend.verify-email-url=https://example.test/auth/verificar-correo", "app.frontend.admin-activation-url=",
    "app.storage.provider=local"})
@ActiveProfiles("admin-invitation-test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdministradorInvitacionIntegrationTest {
    static final String DB="admin_e2e_"+UUID.randomUUID().toString().replace("-", "");
    static final String CLAVE="OnlyLocalTest9!";
    @DynamicPropertySource static void propiedades(DynamicPropertyRegistry r) throws Exception {
        try(var c=conexion(); var s=c.createStatement()){s.execute("CREATE DATABASE "+DB);}
        r.add("spring.datasource.url",()->url(DB));r.add("spring.datasource.username",()->"vidia_test");r.add("spring.datasource.password",()->"");
    }
    static Connection conexion() throws SQLException {return DriverManager.getConnection(System.getenv("VIDIA_TEST_PG_URL"),"vidia_test","");}
    static String url(String db){return "jdbc:postgresql://127.0.0.1:55439/"+db;}
    @Autowired org.springframework.context.ApplicationContext contexto;
    @Autowired TestRestTemplate http; @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles; @Autowired UsuarioRolRepository usuarioRoles;
    @Autowired PasswordEncoder encoder; @Autowired InvitacionAdministradorRepository invitaciones;
    @MockBean ProveedorCorreo correo;

    String crearCuenta(String nombre,String... asignaciones){
        Usuario u=Usuario.builder().correoElectronico(nombre+"@example.test").nombres("E2E").apellidos("Local")
            .ci("E2E-"+nombre).celular("00000000").tipoUsuario(Usuario.TipoUsuario.EXTERNO)
            .correoVerificado(true).contrasena(encoder.encode(CLAVE)).build();usuarios.saveAndFlush(u);
        for(String n:asignaciones){Rol r=roles.findByNombre(n).orElseGet(()->roles.saveAndFlush(Rol.builder().nombre(n).build()));usuarioRoles.saveAndFlush(UsuarioRol.builder().usuario(u).rol(r).build());}
        return login(u.getCorreoElectronico());
    }
    String login(String email){var response=http.postForEntity("/api/v1/auth/login",Map.of("correoElectronico",email,"contrasena",CLAVE),Map.class);assertEquals(200,response.getStatusCode().value());return (String)response.getBody().get("token");}
    ResponseEntity<Map> request(HttpMethod method,String path,Object body,String jwt){var h=new HttpHeaders();if(jwt!=null)h.setBearerAuth(jwt);return http.exchange(path,method,new HttpEntity<>(body,h),Map.class);}
    Map<String,Object> datos(String nombre){return Map.of("nombres","E2E","apellidos","Administrador","correo",nombre+"@example.test","ci","E2E-"+nombre,"celular","00000000");}
    String tokenCorreo(){var cap=ArgumentCaptor.forClass(String.class);verify(correo,atLeastOnce()).enviar(any(),any(),cap.capture());String texto=cap.getValue();var matcher=java.util.regex.Pattern.compile("token=([A-Za-z0-9_-]{43})").matcher(texto);assertTrue(matcher.find());return matcher.group(1);}
    Map<String,Object> activacion(String nombre,String token){return Map.of("token",token,"nombres","E2E","apellidos","Administrador","ci","E2E-"+nombre,"celular","00000000","contrasena",CLAVE,"confirmacionContrasena",CLAVE);}

    @Test @Order(1) void desactivacionesConcurrentesConservanUltimoAdministrador() throws Exception {
        String jwtA=crearCuenta("primeradmin","ADMINISTRADOR"), jwtB=crearCuenta("segundoadmin","ADMINISTRADOR");
        UUID idA=usuarios.findByCorreoElectronico("primeradmin@example.test").orElseThrow().getId();
        UUID idB=usuarios.findByCorreoElectronico("segundoadmin@example.test").orElseThrow().getId();
        var pool=Executors.newFixedThreadPool(2); var gate=new CountDownLatch(1);
        try {
            var a=pool.submit(()->{gate.await();return request(HttpMethod.PATCH,"/api/v1/administradores/"+idA+"/desactivar",null,jwtA).getStatusCode().value();});
            var b=pool.submit(()->{gate.await();return request(HttpMethod.PATCH,"/api/v1/administradores/"+idB+"/desactivar",null,jwtB).getStatusCode().value();});
            gate.countDown(); var codes=new ArrayList<>(List.of(a.get(30,TimeUnit.SECONDS),b.get(30,TimeUnit.SECONDS)));Collections.sort(codes);
            assertEquals(List.of(204,400),codes);
            assertEquals(1,usuarios.findAdministradores().stream().filter(Usuario::isActivo).count());
        } finally {pool.shutdownNow();}
    }
    @Test void seedDemoSigueCreandoAdministradorQuePuedeIniciarSesion() {
        contexto.getAutowireCapableBeanFactory().createBean(bo.uajms.eventos.core.configuracion.DatosInicialesSeed.class).run();
        String jwt=login("admin@demo.local");Usuario u=usuarios.findByCorreoElectronico("admin@demo.local").orElseThrow();
        assertTrue(u.isActivo());assertTrue(u.isCorreoVerificado());
        assertEquals(1,usuarioRoles.findByUsuarioId(u.getId()).size());
        assertTrue(usuarioRoles.existsByUsuarioIdAndRolNombre(u.getId(),"ADMINISTRADOR"));
        assertEquals(400,request(HttpMethod.PATCH,"/api/v1/administradores/"+u.getId()+"/desactivar",null,jwt).getStatusCode().value());
        assertTrue(usuarios.findById(u.getId()).orElseThrow().isActivo());
    }
    @Test void flujoHttpRealLoginRolesPermisosSwaggerYDesactivacion() {
        String actor="a"+UUID.randomUUID().toString().substring(0,6), nuevo="n"+UUID.randomUUID().toString().substring(0,6);
        String jwt=crearCuenta(actor,"ADMINISTRADOR");
        var creada=request(HttpMethod.POST,"/api/v1/administradores/invitaciones",datos(nuevo),jwt);
        assertEquals(201,creada.getStatusCode().value());assertFalse(creada.getBody().containsKey("token"));assertFalse(creada.getBody().containsKey("tokenHash"));
        String token=tokenCorreo();
        assertEquals(Map.of("estado","PENDIENTE"),request(HttpMethod.POST,"/api/v1/auth/invitaciones-administrador/consultar",Map.of("token",token),null).getBody());
        assertEquals(204,request(HttpMethod.POST,"/api/v1/auth/invitaciones-administrador/aceptar",activacion(nuevo,token),null).getStatusCode().value());
        assertEquals(400,request(HttpMethod.POST,"/api/v1/auth/invitaciones-administrador/aceptar",activacion(nuevo,token),null).getStatusCode().value());
        Usuario u=usuarios.findByCorreoElectronico(nuevo+"@example.test").orElseThrow();assertTrue(u.isActivo());assertTrue(u.isCorreoVerificado());
        assertEquals(1,usuarioRoles.findByUsuarioId(u.getId()).size());
        assertTrue(usuarioRoles.existsByUsuarioIdAndRolNombre(u.getId(),"ADMINISTRADOR"));
        assertFalse(usuarioRoles.existsByUsuarioIdAndRolNombre(u.getId(),"USUARIO"));
        assertFalse(usuarioRoles.existsByUsuarioIdAndRolNombre(u.getId(),"ORGANIZADOR"));
        String nuevoJwt=login(u.getCorreoElectronico());
        var headers=new HttpHeaders();headers.setBearerAuth(nuevoJwt);
        assertEquals(200,http.exchange("/api/v1/administradores",HttpMethod.GET,new HttpEntity<>(headers),String.class).getStatusCode().value());
        assertEquals(403,request(HttpMethod.POST,"/api/v1/inscripciones",Map.of("eventoId",UUID.randomUUID()),nuevoJwt).getStatusCode().value());
        assertEquals(403,request(HttpMethod.POST,"/api/v1/asistencias",Map.of("token","QR-ficticio"),nuevoJwt).getStatusCode().value());
        // Newly activated admin can invite another admin through the same mechanism.
        assertEquals(201,request(HttpMethod.POST,"/api/v1/administradores/invitaciones",datos("z"+actor),nuevoJwt).getStatusCode().value());
        assertEquals(204,request(HttpMethod.PATCH,"/api/v1/administradores/"+u.getId()+"/desactivar",null,jwt).getStatusCode().value());
        assertEquals(401,request(HttpMethod.GET,"/api/v1/usuarios/perfil",null,nuevoJwt).getStatusCode().value());
        assertEquals(401,http.postForEntity("/api/v1/auth/login",Map.of("correoElectronico",u.getCorreoElectronico(),"contrasena",CLAVE),Map.class).getStatusCode().value());
        var openapi=http.getForEntity("/v3/api-docs",Map.class);assertEquals(200,openapi.getStatusCode().value());
        assertTrue(((Map<?,?>)openapi.getBody().get("paths")).containsKey("/api/v1/auth/invitaciones-administrador/aceptar"));
        var operation=(Map<?,?>)((Map<?,?>)((Map<?,?>)openapi.getBody().get("paths")).get("/api/v1/auth/invitaciones-administrador/aceptar")).get("post");
        assertEquals(List.of(),operation.get("security"));
        assertTrue(operation.get("description").toString().contains("Acceso publico"));
        assertEquals(200,http.getForEntity("/swagger-ui/index.html",String.class).getStatusCode().value());
    }
    @ParameterizedTest @ValueSource(strings={"USUARIO","ORGANIZADOR","DUAL"})
    void participanteYOrganizadorReciben403(String tipo){String n="p"+UUID.randomUUID().toString().substring(0,6);String jwt=tipo.equals("DUAL")?crearCuenta(n,"USUARIO","ORGANIZADOR"):crearCuenta(n,tipo);
        assertEquals(403,request(HttpMethod.POST,"/api/v1/administradores/invitaciones",datos("x"+n),jwt).getStatusCode().value());
        assertEquals(403,request(HttpMethod.PATCH,"/api/v1/administradores/"+UUID.randomUUID()+"/desactivar",null,jwt).getStatusCode().value());
        assertEquals(403,http.exchange("/api/v1/administradores",HttpMethod.GET,new HttpEntity<>(headers(jwt)),String.class).getStatusCode().value());
        assertEquals(403,http.exchange("/api/v1/administradores/invitaciones",HttpMethod.GET,new HttpEntity<>(headers(jwt)),String.class).getStatusCode().value());
        assertEquals(403,request(HttpMethod.POST,"/api/v1/administradores/invitaciones/"+UUID.randomUUID()+"/reenviar",null,jwt).getStatusCode().value());
        assertEquals(403,request(HttpMethod.PATCH,"/api/v1/administradores/invitaciones/"+UUID.randomUUID()+"/revocar",null,jwt).getStatusCode().value());
    }
    HttpHeaders headers(String jwt){var h=new HttpHeaders();h.setBearerAuth(jwt);return h;}
    @Test void falloDeCorreoRevierteInvitacionPersistida() {
        String n="m"+UUID.randomUUID().toString().substring(0,6);String jwt=crearCuenta("a"+n,"ADMINISTRADOR");
        doThrow(new bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException("MAIL_UNAVAILABLE","Correo no disponible"))
            .when(correo).enviar(any(),any(),any());
        assertEquals(503,request(HttpMethod.POST,"/api/v1/administradores/invitaciones",datos(n),jwt).getStatusCode().value());
        assertTrue(invitaciones.findByEstadoAndCorreo(InvitacionAdministrador.Estado.PENDIENTE,n+"@example.test").isEmpty());
        assertFalse(usuarios.existsByCorreoElectronicoIgnoreCase(n+"@example.test"));
    }
    @Test void revocacionYReenvioInvalidanTokenAnterior() {
        String n="r"+UUID.randomUUID().toString().substring(0,6);String jwt=crearCuenta("a"+n,"ADMINISTRADOR");
        var creada=request(HttpMethod.POST,"/api/v1/administradores/invitaciones",datos(n),jwt);
        String tokenAnterior=tokenCorreo();String id=(String)creada.getBody().get("id");
        var i=invitaciones.findById(UUID.fromString(id)).orElseThrow();i.setFechaUltimoEnvio(java.time.LocalDateTime.now().minusMinutes(10));invitaciones.saveAndFlush(i);
        assertEquals(200,request(HttpMethod.POST,"/api/v1/administradores/invitaciones/"+id+"/reenviar",null,jwt).getStatusCode().value());
        String tokenNuevo=tokenCorreo();assertNotEquals(tokenAnterior,tokenNuevo);
        assertEquals(400,request(HttpMethod.POST,"/api/v1/auth/invitaciones-administrador/aceptar",activacion(n,tokenAnterior),null).getStatusCode().value());
        assertEquals(200,request(HttpMethod.PATCH,"/api/v1/administradores/invitaciones/"+id+"/revocar",null,jwt).getStatusCode().value());
        assertEquals(400,request(HttpMethod.POST,"/api/v1/auth/invitaciones-administrador/aceptar",activacion(n,tokenNuevo),null).getStatusCode().value());
    }
    @Test void expiracionYUuidAjenoNoCreanNiModificanCuentas() {
        String n="e"+UUID.randomUUID().toString().substring(0,6);String jwt=crearCuenta("a"+n,"ADMINISTRADOR");
        var creada=request(HttpMethod.POST,"/api/v1/administradores/invitaciones",datos(n),jwt);String token=tokenCorreo();
        var i=invitaciones.findById(UUID.fromString((String)creada.getBody().get("id"))).orElseThrow();i.setFechaExpiracion(java.time.LocalDateTime.now().minusSeconds(1));invitaciones.saveAndFlush(i);
        assertEquals(Map.of("estado","EXPIRADA"),request(HttpMethod.POST,"/api/v1/auth/invitaciones-administrador/consultar",Map.of("token",token),null).getBody());
        assertEquals(400,request(HttpMethod.POST,"/api/v1/auth/invitaciones-administrador/aceptar",activacion(n,token),null).getStatusCode().value());
        String p="u"+n;crearCuenta(p,"USUARIO");UUID participante=usuarios.findByCorreoElectronico(p+"@example.test").orElseThrow().getId();
        assertEquals(404,request(HttpMethod.PATCH,"/api/v1/administradores/"+participante+"/desactivar",null,jwt).getStatusCode().value());
        assertTrue(usuarios.findById(participante).orElseThrow().isActivo());
        assertEquals(404,request(HttpMethod.PATCH,"/api/v1/administradores/invitaciones/"+UUID.randomUUID()+"/revocar",null,jwt).getStatusCode().value());
    }
    @Test void dosAceptacionesConcurrentesSoloCreanUnaCuenta() throws Exception {
        String n="c"+UUID.randomUUID().toString().substring(0,6);String jwt=crearCuenta("a"+n,"ADMINISTRADOR");
        assertEquals(201,request(HttpMethod.POST,"/api/v1/administradores/invitaciones",datos(n),jwt).getStatusCode().value());String token=tokenCorreo();
        var pool=Executors.newFixedThreadPool(2);try{
            var gate=new CountDownLatch(1);Callable<Integer> aceptar=()->{gate.await();return request(HttpMethod.POST,"/api/v1/auth/invitaciones-administrador/aceptar",activacion(n,token),null).getStatusCode().value();};
            var a=pool.submit(aceptar);var b=pool.submit(aceptar);gate.countDown();var statuses=new ArrayList<>(List.of(a.get(30,TimeUnit.SECONDS),b.get(30,TimeUnit.SECONDS)));Collections.sort(statuses);assertEquals(List.of(204,400),statuses);
            var u=usuarios.findByCorreoElectronico(n+"@example.test").orElseThrow();assertEquals(1,usuarioRoles.findByUsuarioId(u.getId()).size());
        }finally{pool.shutdownNow();}
    }
    @Test void revocacionConcurrenteNoPermiteActivarEnlaceRevocado() throws Exception {
        String n="v"+UUID.randomUUID().toString().substring(0,6);String jwt=crearCuenta("a"+n,"ADMINISTRADOR");
        var creada=request(HttpMethod.POST,"/api/v1/administradores/invitaciones",datos(n),jwt);
        String token=tokenCorreo(),id=(String)creada.getBody().get("id");
        var pool=Executors.newFixedThreadPool(2);var gate=new CountDownLatch(1);
        try {
            var a=pool.submit(()->{gate.await();return request(HttpMethod.POST,"/api/v1/auth/invitaciones-administrador/aceptar",activacion(n,token),null).getStatusCode().value();});
            var b=pool.submit(()->{gate.await();return request(HttpMethod.PATCH,"/api/v1/administradores/invitaciones/"+id+"/revocar",null,jwt).getStatusCode().value();});
            gate.countDown();int aceptacion=a.get(30,TimeUnit.SECONDS),revocacion=b.get(30,TimeUnit.SECONDS);
            assertTrue((aceptacion==204 && revocacion==400)||(aceptacion==400 && revocacion==200));
            var i=invitaciones.findById(UUID.fromString(id)).orElseThrow();
            assertEquals(aceptacion==204?InvitacionAdministrador.Estado.ACEPTADA:InvitacionAdministrador.Estado.REVOCADA,i.getEstado());
            assertEquals(aceptacion==204,usuarios.existsByCorreoElectronicoIgnoreCase(n+"@example.test"));
        }finally{pool.shutdownNow();}
    }
    @ParameterizedTest @ValueSource(strings={"clean","1","2","3","historical"})
    void migracionForwardOnlyConservaDatos(String punto) throws Exception {
        String db="mig_"+UUID.randomUUID().toString().replace("-","");try(var c=conexion();var s=c.createStatement()){s.execute("CREATE DATABASE "+db);}
        var config=Flyway.configure().dataSource(url(db),"vidia_test","").locations("classpath:db/migration");
        if(!punto.equals("clean"))config.target(punto.equals("historical")?"3":punto).load().migrate();
        UUID historico=UUID.randomUUID();
        if(punto.equals("historical")){try(var c=DriverManager.getConnection(url(db),"vidia_test","");var s=c.prepareStatement("insert into usuario(id,correo_electronico,contrasena,nombres,apellidos,ci,tipo_usuario,correo_verificado,estado_solicitud_organizador) values(?, 'historico@example.test','hash','E2E','Historico','E2E-HIST','EXTERNO',true,'NINGUNA')")){s.setObject(1,historico);s.executeUpdate();
            for(String rol:List.of("USUARIO","ORGANIZADOR")){UUID rolId=UUID.randomUUID();try(var rs=c.prepareStatement("insert into rol(id,nombre) values (?,?)")){rs.setObject(1,rolId);rs.setString(2,rol);rs.executeUpdate();}try(var us=c.prepareStatement("insert into usuario_rol(id,usuario_id,rol_id) values (?,?,?)")){us.setObject(1,UUID.randomUUID());us.setObject(2,historico);us.setObject(3,rolId);us.executeUpdate();}}
        }}
        Flyway.configure().dataSource(url(db),"vidia_test","").locations("classpath:db/migration").load().migrate();
        try(var c=DriverManager.getConnection(url(db),"vidia_test","");var s=c.createStatement()){
            assertTrue(s.executeQuery("select activo from usuario limit 0").getMetaData().getColumnCount()==1);
            s.executeQuery("select token_hash from invitacion_administrador limit 0");
            if(punto.equals("historical")){var r=s.executeQuery("select id,activo from usuario");assertTrue(r.next());assertEquals(historico,r.getObject(1));assertTrue(r.getBoolean(2));
                var assignments=s.executeQuery("select r.nombre from usuario_rol ur join rol r on ur.rol_id=r.id order by r.nombre");var names=new ArrayList<String>();while(assignments.next())names.add(assignments.getString(1));assertEquals(List.of("ORGANIZADOR","USUARIO"),names);
            }
        }
    }
}
