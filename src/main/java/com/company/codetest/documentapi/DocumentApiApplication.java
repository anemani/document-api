package com.company.codetest.documentapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


/*@EntityScan("com.company.codetest.documentapi.domain")
@EnableJpaRepositories("com.company.codetest.documentapi.repository")*/
@SpringBootApplication
public class DocumentApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentApiApplication.class, args);
	}

}
