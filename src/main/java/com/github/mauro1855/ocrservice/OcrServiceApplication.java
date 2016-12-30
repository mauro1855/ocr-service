package com.github.mauro1855.ocrservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class OcrServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OcrServiceApplication.class, args);
	}

}
