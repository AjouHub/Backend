package sulhoe.ajouhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "sulhoe.ajouhub.repository")
@EntityScan(basePackages = "sulhoe.ajouhub.entity")
public class AjouHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AjouHubApplication.class, args);
    }

}
