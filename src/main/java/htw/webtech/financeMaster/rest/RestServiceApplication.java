package htw.webtech.financeMaster.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "htw.webtech.financeMaster")
@EntityScan(basePackages = "htw.webtech.financeMaster.persistence.entity")
@EnableJpaRepositories(basePackages = "htw.webtech.financeMaster.persistence.repository")
public class RestServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestServiceApplication.class, args);
	}
}