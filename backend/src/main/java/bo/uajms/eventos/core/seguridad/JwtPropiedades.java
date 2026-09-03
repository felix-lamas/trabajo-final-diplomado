package bo.uajms.eventos.core.seguridad;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtPropiedades {
    private String secreto;
    private Long expiracionMs;
}
