package com.raad.converter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = { "com.raad.converter" })
public class RaadConverterApplication {

	public static void main(String[] args) {
		SpringApplication.run(RaadConverterApplication.class, args);
	}

}
