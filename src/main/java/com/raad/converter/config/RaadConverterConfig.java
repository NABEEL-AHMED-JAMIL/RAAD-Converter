package com.raad.converter.config;

import com.raad.converter.worker.AsyncDALTaskExecutor;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

@Configuration
public class RaadConverterConfig {

    public Logger logger = LoggerFactory.getLogger(RaadConverterConfig.class);

    @Value("${socket.host}")
    private String host;
    @Value("${socket.port}")
    private Integer port;

    @Bean
    public SocketIOServer socketIOServer() {
        logger.info("===============SocketIOServer-INIT===============");
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(this.host);
        config.setPort(this.port);
        logger.info("SocketIOServer-IO Detail :- " + config);
        logger.info("===============SocketIOServer-End===============");
        return new SocketIOServer(config);
    }

    @Bean
    @Scope("singleton")
    public AsyncDALTaskExecutor asyncDALTaskExecutor() throws Exception {
        AsyncDALTaskExecutor taskExecutor = null;
        logger.info("===============Application-DAL-INIT===============");
        taskExecutor = new AsyncDALTaskExecutor(20, 50, 1);
        logger.info("===============Application-DAL-END===============");
        return taskExecutor;
    }

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

}
