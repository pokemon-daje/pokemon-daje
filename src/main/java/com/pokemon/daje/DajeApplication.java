package com.pokemon.daje;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DajeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DajeApplication.class, args);
	}

}
