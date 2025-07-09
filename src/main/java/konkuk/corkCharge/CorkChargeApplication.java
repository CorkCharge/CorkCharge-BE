package konkuk.corkCharge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CorkChargeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CorkChargeApplication.class, args);
	}

}
