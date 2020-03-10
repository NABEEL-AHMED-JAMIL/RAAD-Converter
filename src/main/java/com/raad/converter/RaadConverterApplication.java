package com.raad.converter;

import com.raad.converter.batch.AsyncDALTaskExecutor;
import com.raad.converter.batch.AsyncWorker;
import io.github.bonigarcia.wdm.WebDriverManager;
import net.sourceforge.tess4j.TesseractException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;

@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = { "com.raad.converter" })
public class RaadConverterApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(RaadConverterApplication.class, args);
	}

	@Autowired
	private AsyncWorker asyncWorker;
	@Autowired
	private AsyncDALTaskExecutor asyncDALTaskExecutor;

	@Bean
	public WebDriver webDriver() {
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--no-sandbox"); // Bypass OS security model
		options.addArguments("--ignore-certificate-errors");
		options.addArguments("--start-maximized");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--incognito");
		options.addArguments("--headless");
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		return new ChromeDriver(capabilities);
	}

	//@Scheduled(fixedDelay = 5000)
	public void fixDealyV2() {
		String path = "C:\\Users\\Nabeel.Ahmed\\Downloads\\hwp";
		final File folder = new File(path);
		int i = 0;
		for (final File fileEntry : folder.listFiles()) {
			try {
				i = i+1;
				System.out.println("File Process Start...." + i);
				this.asyncWorker.setFilePath(fileEntry.getAbsolutePath());
				this.asyncWorker.setFileNumber(i);
				//this.asyncDALTaskExecutor.addTask(this.asyncWorker);
				this.asyncWorker.run();
				System.out.println("File Process End....");
			} catch (Exception ex) {
				System.out.println("Exception :- " + ex.getMessage());
			}
		}
	}

	@Override
	public void run(String...args) throws Exception {
		//fixDealyV2();
	}

}
