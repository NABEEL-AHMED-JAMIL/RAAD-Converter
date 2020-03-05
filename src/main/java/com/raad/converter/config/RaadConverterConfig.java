package com.raad.converter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

}
