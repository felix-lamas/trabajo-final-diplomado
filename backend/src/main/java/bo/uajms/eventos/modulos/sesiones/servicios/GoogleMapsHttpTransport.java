package bo.uajms.eventos.modulos.sesiones.servicios;

import jakarta.annotation.PreDestroy;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.util.Timeout;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;

@Component
public class GoogleMapsHttpTransport {
    private static final int MAX_RESPONSE_BYTES = 256 * 1024;
    private static final DnsResolver SAFE_DNS_RESOLVER = new DnsResolver() {
        @Override
        public InetAddress[] resolve(String host) throws UnknownHostException {
            if (!isAllowedHost(host)) throw new UnknownHostException("Host no permitido");
            InetAddress[] addresses = SystemDefaultDnsResolver.INSTANCE.resolve(host);
            if (addresses.length == 0 || Arrays.stream(addresses).anyMatch(address -> !isPublicAddress(address))) {
                throw new UnknownHostException("El host resuelve a una dirección no pública");
            }
            return addresses;
        }

        @Override
        public String resolveCanonicalHostname(String host) throws UnknownHostException {
            resolve(host);
            return host;
        }
    };

    private final PoolingHttpClientConnectionManager connectionManager;
    private final CloseableHttpClient client;

    public GoogleMapsHttpTransport() {
        connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDnsResolver(SAFE_DNS_RESOLVER)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofSeconds(3))
                        .setSocketTimeout(Timeout.ofSeconds(5))
                        .build())
                .build();
        client = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .disableRedirectHandling()
                .disableAutomaticRetries()
                .build();
    }

    GoogleMapsLinkResolver.HopResponse get(URI uri) throws IOException {
        if (!isAllowedHost(uri.getHost())) throw new IOException("Host no permitido");
        HttpGet request = new HttpGet(uri);
        request.setHeader("Accept", "text/html,application/xhtml+xml,text/plain;q=0.8");
        request.setHeader("User-Agent", "Vidia-Maps-Resolver/1.0");
        request.setConfig(RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(2))
                .setResponseTimeout(Timeout.ofSeconds(5))
                .build());
        return client.execute(request, response -> {
            String location = response.getFirstHeader("Location") == null
                    ? null : response.getFirstHeader("Location").getValue();
            String body = readLimited(response.getEntity());
            return new GoogleMapsLinkResolver.HopResponse(response.getCode(), location, body);
        });
    }

    static boolean isAllowedHost(String host) {
        if (host == null) return false;
        return switch (host.toLowerCase(java.util.Locale.ROOT)) {
            case "maps.app.goo.gl", "goo.gl", "google.com", "www.google.com", "maps.google.com" -> true;
            default -> false;
        };
    }

    private static String readLimited(HttpEntity entity) throws IOException {
        if (entity == null) return "";
        if (entity.getContentLength() > MAX_RESPONSE_BYTES) throw new IOException("Respuesta demasiado grande");
        try (InputStream stream = entity.getContent()) {
            byte[] bytes = stream.readNBytes(MAX_RESPONSE_BYTES + 1);
            if (bytes.length > MAX_RESPONSE_BYTES) throw new IOException("Respuesta demasiado grande");
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    static boolean isPublicAddress(InetAddress address) {
        byte[] bytes = address.getAddress();
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) return false;
        if (address instanceof Inet4Address) return isPublicIpv4(bytes);
        if (address instanceof Inet6Address) return isPublicIpv6(bytes);
        return false;
    }

    private static boolean isPublicIpv4(byte[] address) {
        int a = Byte.toUnsignedInt(address[0]);
        int b = Byte.toUnsignedInt(address[1]);
        int c = Byte.toUnsignedInt(address[2]);
        if (a == 0 || a == 10 || a == 127 || a >= 224) return false;
        if (a == 100 && b >= 64 && b <= 127) return false;
        if (a == 169 && b == 254) return false;
        if (a == 172 && b >= 16 && b <= 31) return false;
        if (a == 192 && (b == 0 || b == 168)) return false;
        if (a == 192 && b == 88 && c == 99) return false;
        if (a == 192 && b == 0 && c == 2) return false;
        if (a == 198 && (b == 18 || b == 19 || b == 51 && c == 100)) return false;
        if (a == 203 && b == 0 && c == 113) return false;
        return true;
    }

    private static boolean isPublicIpv6(byte[] address) {
        // Only global-unicast 2000::/3 is routable for this resolver; this rejects ULA,
        // link-local, multicast, IPv4-mapped and the remaining special-purpose ranges.
        if ((Byte.toUnsignedInt(address[0]) & 0xe0) != 0x20) return false;
        if (prefix(address, new byte[]{0x20, 0x01, 0x0d, (byte) 0xb8}, 32)) return false; // documentation
        if (prefix(address, new byte[]{0x20, 0x01, 0x00, 0x00}, 23)) return false; // IETF special use
        if (prefix(address, new byte[]{0x20, 0x01, 0x00, 0x10}, 28)) return false; // ORCHID
        if (prefix(address, new byte[]{0x20, 0x01, 0x00, 0x20}, 28)) return false; // ORCHIDv2
        if (prefix(address, new byte[]{0x20, 0x02}, 16)) return false; // 6to4 transition
        return true;
    }

    private static boolean prefix(byte[] address, byte[] prefix, int bits) {
        int wholeBytes = bits / 8;
        int remainingBits = bits % 8;
        for (int i = 0; i < wholeBytes; i++) if (address[i] != prefix[i]) return false;
        if (remainingBits == 0) return true;
        int mask = 0xff << (8 - remainingBits);
        return (Byte.toUnsignedInt(address[wholeBytes]) & mask)
                == (Byte.toUnsignedInt(prefix[wholeBytes]) & mask);
    }

    @PreDestroy
    public void close() throws IOException {
        client.close();
        connectionManager.close();
    }
}
