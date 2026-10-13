package bo.uajms.eventos.core.seguridad;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
class IntentosAutenticacionTest {
 static class Reloj extends Clock {
  Instant ahora=Instant.EPOCH;
  public ZoneId getZone(){return ZoneOffset.UTC;}
  public Clock withZone(ZoneId z){return this;}
  public Instant instant(){return ahora;}
 }
 @Test void normalizaSinConsultarUsuarios(){
  var l=new IntentosAutenticacion(new Reloj(),2,20,60,10);
  l.comprobarCorreo("login"," ANONIMO@example.test ");
  l.comprobarCorreo("login","anonimo@EXAMPLE.test");
  var e=assertThrows(DemasiadosIntentosException.class,()->l.comprobarCorreo("login","anonimo@example.test"));
  assertEquals(60,e.getSegundos()); assertFalse(e.getMessage().contains("example.test"));
  l.comprobarCorreo("login","otra@example.test"); l.comprobarCorreo("recuperacion","anonimo@example.test");
 }
 @Test void expiraSinBloqueoPermanente(){
  var r=new Reloj();var l=new IntentosAutenticacion(r,1,20,60,10);l.comprobar("verificacion","temporal");
  r.ahora=Instant.EPOCH.plusSeconds(59);
  assertEquals(1,assertThrows(DemasiadosIntentosException.class,()->l.comprobar("verificacion","temporal")).getSegundos());
  r.ahora=Instant.EPOCH.plusSeconds(60);assertDoesNotThrow(()->l.comprobar("verificacion","temporal"));
 }
 @Test void rotacionNoEvadeAgregado(){
  var l=new IntentosAutenticacion(new Reloj(),10,2,60,10);l.comprobar("login","uno");l.comprobar("login","dos");
  assertThrows(DemasiadosIntentosException.class,()->l.comprobar("login","tres"));
 }
 @Test void memoriaAcotada(){
  var r=new Reloj();var l=new IntentosAutenticacion(r,2,20,60,1);l.comprobar("login","uno");
  assertThrows(DemasiadosIntentosException.class,()->l.comprobar("login","dos"));
  r.ahora=Instant.EPOCH.plusSeconds(60);assertDoesNotThrow(()->l.comprobar("login","dos"));
 }
 @Test void limiteAtomico() throws Exception {
  var l=new IntentosAutenticacion(new Reloj(),5,100,60,10);var n=new AtomicInteger();
  try(var pool=Executors.newFixedThreadPool(8)){
   for(int i=0;i<40;i++)pool.submit(()->{try{l.comprobar("login","igual");n.incrementAndGet();}catch(DemasiadosIntentosException ignored){}});
   pool.shutdown();assertTrue(pool.awaitTermination(5,TimeUnit.SECONDS));
  }assertEquals(5,n.get());
 }
}
