package edu.cit.camoro.peertayo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PeertayoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PeertayoApplication.class, args);
	}
}
