package bo.uajms.eventos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "bo.uajms.eventos")
@EntityScan(basePackages = "bo.uajms.eventos")
@EnableJpaRepositories(basePackages = "bo.uajms.eventos")
public class EventosUajmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventosUajmsApplication.class, args);
    }
}
