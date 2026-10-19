package bo.uajms.eventos.modulos.sesiones.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;
import bo.uajms.eventos.modulos.sesiones.dtos.GoogleMapsCoordinatesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GoogleMapsLinkResolver {
    private static final int MAX_REDIRECTS = 5;
    private static final Pattern PLACE_COORDINATES = Pattern.compile(
            "!3d([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+))!4d([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+))");
    private static final Pattern EXPLICIT_PAIR = Pattern.compile(
            "^([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)),\\s*([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+))$");
    private static final Pattern MAPS_SEARCH_PATH = Pattern.compile(
            "/maps/search/([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)),\\+([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+))(?:/|$)");
    private static final Pattern MAPS_URL = Pattern.compile(
            "https://(?:www\\.|maps\\.)?google\\.com/maps[^\\s\\\"'<>]*", Pattern.CASE_INSENSITIVE);

    private final HttpTransport transport;

    @Autowired
    public GoogleMapsLinkResolver(GoogleMapsHttpTransport transport) {
        this(transport::get);
    }

    GoogleMapsLinkResolver(HttpTransport transport) {
        this.transport = transport;
    }

    public GoogleMapsCoordinatesResponse resolve(String input) {
        URI current = parseUri(input);
        validateMapsUri(current);
        List<Coordinates> found = coordinatesInUri(current);
        if (!found.isEmpty()) return response(uniqueCoordinates(found));

        for (int redirects = 0; redirects <= MAX_REDIRECTS; redirects++) {
            HopResponse hop;
            try {
                hop = transport.get(current);
            } catch (IOException exception) {
                throw new ServicioNoDisponibleException("MAPS_RESOLUTION_FAILED",
                        "No se pudo consultar el enlace de Google Maps. Intente nuevamente.");
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new ServicioNoDisponibleException("MAPS_RESOLUTION_INTERRUPTED",
                        "Se interrumpió la consulta del enlace de Google Maps.");
            }

            if (isRedirect(hop.status())) {
                if (redirects == MAX_REDIRECTS) {
                    throw new NegocioException("El enlace excede el límite de redirecciones permitido.");
                }
                if (hop.location() == null || hop.location().isBlank()) {
                    throw new NegocioException("Google Maps devolvió una redirección incompleta.");
                }
                URI next;
                try {
                    next = current.resolve(new URI(hop.location().trim()));
                } catch (Exception exception) {
                    throw new NegocioException("Google Maps devolvió una redirección inválida.");
                }
                validateMapsUri(next);
                current = next;
                found = coordinatesInUri(current);
                if (!found.isEmpty()) return response(uniqueCoordinates(found));
                continue;
            }

            if (hop.status() < 200 || hop.status() >= 300) {
                throw new NegocioException("Google Maps no pudo resolver este enlace.");
            }
            found = coordinatesInUri(current);
            found.addAll(coordinatesInText(hop.body()));
            if (found.isEmpty()) {
                throw new NegocioException("El enlace se abrió, pero no contiene coordenadas de ubicación reconocibles.");
            }
            return response(uniqueCoordinates(found));
        }
        throw new NegocioException("El enlace excede el límite de redirecciones permitido.");
    }

    private static URI parseUri(String input) {
        if (input == null || input.isBlank() || input.length() > 2048) {
            throw new NegocioException("Pegue un enlace válido de Google Maps.");
        }
        try {
            return new URI(input.trim());
        } catch (Exception exception) {
            throw new NegocioException("Pegue un enlace válido de Google Maps.");
        }
    }

    private static void validateMapsUri(URI uri) {
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        boolean secure = "https".equalsIgnoreCase(uri.getScheme()) && uri.getUserInfo() == null
                && (uri.getPort() == -1 || uri.getPort() == 443);
        boolean knownHost = GoogleMapsHttpTransport.isAllowedHost(host);
        String path = uri.getPath() == null ? "" : uri.getPath();
        boolean mapsPath = (host.equals("maps.app.goo.gl") && path.length() > 1)
                || (host.equals("goo.gl") && path.startsWith("/maps/"))
                || ((host.equals("google.com") || host.equals("www.google.com") || host.equals("maps.google.com"))
                && (path.equals("/maps") || path.startsWith("/maps/")));
        if (!secure || !knownHost || !mapsPath) {
            throw new NegocioException("Solo se aceptan enlaces HTTPS de Google Maps.");
        }
    }

    private static List<Coordinates> coordinatesInUri(URI uri) {
        List<Coordinates> coordinates = coordinatesInText(uri.getPath());
        Matcher searchPath = MAPS_SEARCH_PATH.matcher(uri.getPath() == null ? "" : uri.getPath());
        while (searchPath.find()) coordinates.add(coordinates(searchPath.group(1), searchPath.group(2)));
        String query = uri.getRawQuery();
        if (query != null) {
            for (String parameter : query.split("&")) {
                String[] parts = parameter.split("=", 2);
                if (parts.length != 2) continue;
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                if (!key.equals("q") && !key.equals("query")) continue;
                String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                Matcher matcher = EXPLICIT_PAIR.matcher(value.trim());
                if (matcher.matches()) coordinates.add(coordinates(matcher.group(1), matcher.group(2)));
            }
        }
        return coordinates;
    }

    private static List<Coordinates> coordinatesInText(String text) {
        List<Coordinates> coordinates = new ArrayList<>();
        if (text == null || text.isEmpty()) return coordinates;
        Matcher marker = PLACE_COORDINATES.matcher(text);
        while (marker.find()) coordinates.add(coordinates(marker.group(1), marker.group(2)));
        Matcher url = MAPS_URL.matcher(text);
        while (url.find()) {
            try {
                coordinates.addAll(coordinatesInUri(new URI(url.group())));
            } catch (Exception ignored) {
                // Ignore embedded text that is not a parseable Maps URL.
            }
        }
        return coordinates;
    }

    private static Coordinates coordinates(String latitude, String longitude) {
        try {
            return new Coordinates(new BigDecimal(latitude), new BigDecimal(longitude));
        } catch (NumberFormatException exception) {
            throw new NegocioException("Google Maps devolvió coordenadas inválidas.");
        }
    }

    private static Coordinates uniqueCoordinates(List<Coordinates> coordinates) {
        Map<String, Coordinates> unique = new LinkedHashMap<>();
        for (Coordinates point : coordinates) {
            if (point.latitude().compareTo(BigDecimal.valueOf(-90)) < 0
                    || point.latitude().compareTo(BigDecimal.valueOf(90)) > 0
                    || point.longitude().compareTo(BigDecimal.valueOf(-180)) < 0
                    || point.longitude().compareTo(BigDecimal.valueOf(180)) > 0) {
                throw new NegocioException("Google Maps devolvió coordenadas fuera de rango.");
            }
            String key = point.latitude().stripTrailingZeros().toPlainString() + ","
                    + point.longitude().stripTrailingZeros().toPlainString();
            unique.putIfAbsent(key, point);
        }
        if (unique.size() != 1) {
            throw new NegocioException("El enlace de Google Maps contiene ubicaciones ambiguas.");
        }
        return unique.values().iterator().next();
    }

    private static GoogleMapsCoordinatesResponse response(Coordinates point) {
        return new GoogleMapsCoordinatesResponse(point.latitude(), point.longitude());
    }

    private static boolean isRedirect(int status) {
        return status == 301 || status == 302 || status == 303 || status == 307 || status == 308;
    }

    record Coordinates(BigDecimal latitude, BigDecimal longitude) {}
    record HopResponse(int status, String location, String body) {}
    @FunctionalInterface interface HttpTransport {
        HopResponse get(URI uri) throws IOException, InterruptedException;
    }
}
