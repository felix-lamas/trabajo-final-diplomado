package bo.uajms.eventos.core.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;

/** Límites locales por operación e identificador, independientes de IP y de la existencia de cuentas. */
@Component
public class IntentosAutenticacion {
    private final Clock clock;
    private final int limiteIdentificador;
    private final int limiteOperacion;
    private final long ventanaMs;
    private final int maxIdentificadores;
    private final Map<String, Ventana> identificadores = new HashMap<>();
    private final Map<String, Ventana> operaciones = new HashMap<>();
    private record Ventana(long inicio, int intentos) {}

    @org.springframework.beans.factory.annotation.Autowired
    public IntentosAutenticacion(
            @Value("${app.auth.rate-limit.identifier-limit:10}") int limiteIdentificador,
            @Value("${app.auth.rate-limit.operation-limit:120}") int limiteOperacion,
            @Value("${app.auth.rate-limit.window-seconds:60}") long ventanaSegundos,
            @Value("${app.auth.rate-limit.max-identifiers:10000}") int maxIdentificadores) {
        this(Clock.systemUTC(), limiteIdentificador, limiteOperacion, ventanaSegundos, maxIdentificadores);
    }

    IntentosAutenticacion(Clock clock, int limiteIdentificador, int limiteOperacion,
                         long ventanaSegundos, int maxIdentificadores) {
        if (limiteIdentificador < 1 || limiteOperacion < 1 || ventanaSegundos < 1 || maxIdentificadores < 1)
            throw new IllegalArgumentException("Los límites de autenticación deben ser positivos");
        this.clock = clock;
        this.limiteIdentificador = limiteIdentificador;
        this.limiteOperacion = limiteOperacion;
        this.ventanaMs = Math.multiplyExact(ventanaSegundos, 1000);
        this.maxIdentificadores = maxIdentificadores;
    }

    public synchronized void comprobar(String operacion, String identificador) {
        long ahora = clock.millis();
        identificadores.entrySet().removeIf(e -> ahora - e.getValue().inicio >= ventanaMs);
        String clave = operacion + ":" + hash(identificador);
        Ventana global = vigente(operaciones.get(operacion), ahora);
        Ventana individual = vigente(identificadores.get(clave), ahora);
        if (global.intentos >= limiteOperacion) rechazar(global, ahora);
        if (individual.intentos >= limiteIdentificador) rechazar(individual, ahora);
        if (!identificadores.containsKey(clave) && identificadores.size() >= maxIdentificadores) {
            // No expulsar entradas vigentes: cambiar de identificador no permite evadir el límite.
            long siguiente = identificadores.values().stream().mapToLong(v -> v.inicio + ventanaMs).min().orElse(ahora + ventanaMs);
            throw new DemasiadosIntentosException(Math.max(1, (siguiente - ahora + 999) / 1000));
        }
        operaciones.put(operacion, new Ventana(global.inicio, global.intentos + 1));
        identificadores.put(clave, new Ventana(individual.inicio, individual.intentos + 1));
    }

    private Ventana vigente(Ventana actual, long ahora) {
        return actual == null || ahora - actual.inicio >= ventanaMs ? new Ventana(ahora, 0) : actual;
    }
    private void rechazar(Ventana ventana, long ahora) {
        throw new DemasiadosIntentosException(Math.max(1, (ventana.inicio + ventanaMs - ahora + 999) / 1000));
    }
    private String hash(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 no disponible", e); }
    }
    public void comprobarCorreo(String operacion, String correo) {
        comprobar(operacion, correo.strip().toLowerCase(Locale.ROOT));
    }
}
