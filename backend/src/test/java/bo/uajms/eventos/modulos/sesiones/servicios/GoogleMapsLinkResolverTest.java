package bo.uajms.eventos.modulos.sesiones.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.modulos.sesiones.dtos.GoogleMapsCoordinatesResponse;
import bo.uajms.eventos.modulos.sesiones.controladores.GoogleMapsController;
import bo.uajms.eventos.modulos.sesiones.dtos.GoogleMapsLinkRequest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GoogleMapsLinkResolverTest {
    @Test
    void springInyectaElResolvedorEnElControladorSinResolverLaRedParaCoordenadasExplicitas() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(GoogleMapsHttpTransport.class);
            context.registerBean(GoogleMapsLinkResolver.class);
            context.registerBean(GoogleMapsController.class);
            context.refresh();

            GoogleMapsLinkResolver resolver = context.getBean(GoogleMapsLinkResolver.class);
            GoogleMapsController controller = context.getBean(GoogleMapsController.class);
            assertNotNull(resolver);

            GoogleMapsCoordinatesResponse result = controller.obtenerCoordenadas(
                    new GoogleMapsLinkRequest("https://www.google.com/maps?q=-21.538558%2C-64.764673"));

            assertEquals("-21.538558", result.latitud().toPlainString());
            assertEquals("-64.764673", result.longitud().toPlainString());
        }
    }

    @Test
    void aceptaEnlaceCompletoConCoordenadasExplicitasSinPeticion() {
        AtomicInteger requests = new AtomicInteger();
        var resolver = new GoogleMapsLinkResolver(uri -> {
            requests.incrementAndGet();
            throw new AssertionError("No debe hacer una petición para coordenadas explícitas");
        });

        GoogleMapsCoordinatesResponse coordinates = resolver.resolve(
                "https://www.google.com/maps?q=-21.538558%2C-64.764673");

        assertEquals("-21.538558", coordinates.latitud().toPlainString());
        assertEquals("-64.764673", coordinates.longitud().toPlainString());
        assertEquals(0, requests.get());
    }

    @Test
    void sigueEnlaceCortoSoloHaciaHostMapsPermitidoYExtraeCoordenadas() {
        Queue<GoogleMapsLinkResolver.HopResponse> responses = new ArrayDeque<>();
        responses.add(new GoogleMapsLinkResolver.HopResponse(302,
                "https://www.google.com/maps/search/-21.538538,+-64.764671?entry=tts", ""));
        var resolver = new GoogleMapsLinkResolver(uri -> responses.remove());

        GoogleMapsCoordinatesResponse coordinates = resolver.resolve("https://maps.app.goo.gl/example");

        assertEquals("-21.538538", coordinates.latitud().toPlainString());
        assertEquals("-64.764671", coordinates.longitud().toPlainString());
    }

    @Test
    void bloqueaRedireccionHaciaHostNoPermitidoAntesDeSeguirla() {
        AtomicInteger requests = new AtomicInteger();
        var resolver = new GoogleMapsLinkResolver(uri -> {
            requests.incrementAndGet();
            return new GoogleMapsLinkResolver.HopResponse(302, "http://127.0.0.1/admin", "");
        });

        assertThrows(NegocioException.class, () -> resolver.resolve("https://maps.app.goo.gl/example"));
        assertEquals(1, requests.get());
    }

    @Test
    void fallaSiLaRespuestaNoContieneCoordenadasYConservaFalloComprensible() {
        var resolver = new GoogleMapsLinkResolver(uri ->
                new GoogleMapsLinkResolver.HopResponse(200, null, "Google Maps location unavailable"));

        NegocioException error = assertThrows(NegocioException.class,
                () -> resolver.resolve("https://maps.app.goo.gl/example"));
        assertTrue(error.getMessage().contains("no contiene coordenadas"));
    }

    @Test
    void limitaElNumeroDeRedirecciones() {
        AtomicInteger requests = new AtomicInteger();
        var resolver = new GoogleMapsLinkResolver(uri -> {
            requests.incrementAndGet();
            return new GoogleMapsLinkResolver.HopResponse(302,
                    "https://maps.google.com/maps/next", "");
        });

        assertThrows(NegocioException.class, () -> resolver.resolve("https://maps.app.goo.gl/example"));
        assertEquals(6, requests.get());
    }

    @Test
    void dnsRechazaDireccionesPrivadasLocalesReservadasYDocumentales() throws Exception {
        assertFalse(GoogleMapsHttpTransport.isPublicAddress(InetAddress.getByName("127.0.0.1")));
        assertFalse(GoogleMapsHttpTransport.isPublicAddress(InetAddress.getByName("10.0.0.1")));
        assertFalse(GoogleMapsHttpTransport.isPublicAddress(InetAddress.getByName("169.254.1.1")));
        assertFalse(GoogleMapsHttpTransport.isPublicAddress(InetAddress.getByName("192.0.2.1")));
        assertFalse(GoogleMapsHttpTransport.isPublicAddress(InetAddress.getByName("fc00::1")));
        assertFalse(GoogleMapsHttpTransport.isPublicAddress(InetAddress.getByName("2001:db8::1")));
        assertTrue(GoogleMapsHttpTransport.isPublicAddress(InetAddress.getByName("8.8.8.8")));
        assertTrue(GoogleMapsHttpTransport.isPublicAddress(InetAddress.getByName("2001:4860:4860::8888")));
        assertTrue(GoogleMapsHttpTransport.isAllowedHost("maps.app.goo.gl"));
        assertFalse(GoogleMapsHttpTransport.isAllowedHost("maps.app.goo.gl.attacker.example"));
    }
}
