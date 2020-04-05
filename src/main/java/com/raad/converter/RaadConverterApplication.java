package com.raad.converter;

import com.raad.converter.model.beans.SocketClientInfo;
import com.raad.converter.model.repository.SocketClientInfoRepository;
import com.raad.converter.worker.AsyncDALTaskExecutor;
import com.raad.converter.worker.FileDownloadByRunnableWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.io.File;
import java.util.Random;
import java.util.UUID;


@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = { "com.raad.converter.*" })
public class RaadConverterApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(RaadConverterApplication.class, args);
		//listFilesForFolder(new File("C:\\Users\\Nabeel.Ahmed\\Downloads\\File"));
	}

	public void listFilesForFolder(final File folder) {
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

	//@Autowired
	//SocketClientInfoRepository socketClientInfoRepository;
//	@Autowired
//	AsyncDALTaskExecutor asyncDALTaskExecutor;
//	@Autowired
//	FileDownloadByRunnableWorker fileDownloadByRunnableWorker;

	@Bean
	public CommandLineRunner commandLineRunner() {
		return (args) -> {
//			Random r = new Random();
//			for(int i=0; i<100; i++) {
//				int randomInt = r.nextInt(100) + 1;
//				this.fileDownloadByRunnableWorker.setPriority(randomInt);
//				this.fileDownloadByRunnableWorker.setWorkerName("Nabeel-"+randomInt);
//				this.asyncDALTaskExecutor.addTask(fileDownloadByRunnableWorker);
//			}
//			for(int i=0; i<5; i++){
//				SocketClientInfo socketClientInfo = new SocketClientInfo();
//				socketClientInfo.setToken(UUID.randomUUID().toString());
//				socketClientInfoRepository.save(socketClientInfo);
//			}
		};
	}

}
