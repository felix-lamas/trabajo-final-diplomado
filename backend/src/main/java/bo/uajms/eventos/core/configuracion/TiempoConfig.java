package bo.uajms.eventos.core.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TiempoConfig {
    public static final ZoneId ZONA_OFICIAL = ZoneId.of("America/La_Paz");

    @Bean
    public Clock relojOficial() {
        return Clock.system(ZONA_OFICIAL);
    }
}
