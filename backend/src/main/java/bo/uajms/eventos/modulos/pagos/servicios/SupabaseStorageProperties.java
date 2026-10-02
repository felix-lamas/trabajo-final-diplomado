package bo.uajms.eventos.modulos.pagos.servicios;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.supabase")
public class SupabaseStorageProperties {
    private String endpoint;
    private String region;
    private String bucket;
    private String accessKey;
    private String secretKey;
    private int connectionTimeoutMs = 5000;
    private int apiTimeoutMs = 20000;

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public int getConnectionTimeoutMs() { return connectionTimeoutMs; }
    public void setConnectionTimeoutMs(int connectionTimeoutMs) { this.connectionTimeoutMs = connectionTimeoutMs; }
    public int getApiTimeoutMs() { return apiTimeoutMs; }
    public void setApiTimeoutMs(int apiTimeoutMs) { this.apiTimeoutMs = apiTimeoutMs; }
}
