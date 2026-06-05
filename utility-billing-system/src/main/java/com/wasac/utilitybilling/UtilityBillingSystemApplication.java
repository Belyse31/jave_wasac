package com.wasac.utilitybilling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class UtilityBillingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(UtilityBillingSystemApplication.class, args);
	}

}
