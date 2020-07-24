package com.raad.converter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.io.File;


@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = { "com.raad.converter.*" })
public class RaadConverterApplication {

	private String FILE_FOLDER = "<p>%s <a href=\"%s\"> %s</a></p>";

	public static void main(String[] args) {
		SpringApplication.run(RaadConverterApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner() {
		return (args) -> {
		};
	}

	public void listFilesForFolder(final File folder) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				System.out.println(
					String.format(FILE_FOLDER, fileEntry.getName(), fileEntry.getName(), fileEntry.getName()));
			}
		}
	}

}
