package com.raad.converter;

import com.raad.converter.model.beans.Location;
import com.raad.converter.model.beans.StockPrice;
import com.raad.converter.model.repository.StockPriceRepository;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.io.File;
import java.io.IOException;
import java.util.Date;


@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = { "com.raad.converter.*" })
public class RaadConverterApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(RaadConverterApplication.class, args);
		//listFilesForFolder(new File("C:\\Users\\Nabeel.Ahmed\\Downloads\\File"));
	}

	public void listFilesForFolder(final File folder) throws IOException, TesseractException {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				System.out.println(
					String.format("<p>%s <a href=\"%s\"> %s</a></p>", fileEntry.getName(),
						fileEntry.getName(),fileEntry.getName()));
			}
		}
	}

	@Bean
	public CommandLineRunner commandLineRunner() {
		return (args) -> {};
	}

}
