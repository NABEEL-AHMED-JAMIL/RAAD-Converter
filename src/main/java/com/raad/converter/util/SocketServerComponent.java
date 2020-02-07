package com.raad.converter.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;


@Component
public class SocketServerComponent {

    public Logger logger = LogManager.getLogger(SocketServerComponent.class);

    private String END_POINT = "message";
    private String EVENT_NAME = "saveSocketClientInfo";
    private String TOKEN = "92080482-712e-400a-8b21-627137dbd4bd";

    @Autowired
    private SocketIOServer socketIOServer;

    @PostConstruct
    public void init(){
        logger.info("===========>>>>>>>>>>>>>>>>>SocketServerComponent-Start<<<<<<<<<<<<<<==================");
        this.socketIOServer.addConnectListener(onConnected());
        this.socketIOServer.addDisconnectListener(onDisconnected());
        this.socketIOServer.addEventListener(this.EVENT_NAME, String.class, saveSocketClientInfo());
        this.socketIOServer.start();
        logger.info("===========>>>>>>>>>>>>>>>>SocketServerComponent-End<<<<<<<<<<<<<<<<<<==================");
    }

    public SocketServerComponent() { }

    private ConnectListener onConnected() {
        return client -> {
            HandshakeData handshakeData = client.getHandshakeData();
            logger.info("Client[{}] - Connected to socket through '{}'" , client.getSessionId().toString() ,  handshakeData.getUrl());
        };
    }

    private DataListener<String> saveSocketClientInfo() {
        return (client, data, ackSender) -> { logger.info("Client[{}] - Received data {}", client, data); };
    }

    private DisconnectListener onDisconnected() {
        return client -> { logger.info("Client[{}] - Disconnected from socket" , client.getSessionId().toString()); };
    }

    public SocketIOClient sendSocketEventToClient(String message) throws Exception {
        logger.info("Send Message With message = {}", message);
        SocketIOClient socketIOClient = this.socketIOServer.getClient(UUID.fromString(this.TOKEN));
        if (socketIOClient != null) {
            socketIOClient.sendEvent(this.END_POINT, decode(message));
        }
        return socketIOClient;
    }

    private String decode(String value) throws Exception {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
    }

    @Scheduled(fixedDelay=1000)
    public void updateEmployeeInventory(){
        try {
            this.sendSocketEventToClient(UUID.randomUUID().toString());
        } catch (Exception ex) {
            logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
        }
    }

}
