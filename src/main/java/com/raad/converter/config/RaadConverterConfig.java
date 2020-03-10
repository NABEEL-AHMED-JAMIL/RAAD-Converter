package com.raad.converter.config;

import com.raad.converter.batch.AsyncDALTaskExecutor;
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

}
