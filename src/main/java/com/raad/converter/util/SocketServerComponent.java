package com.raad.converter.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.annotation.PostConstruct;

import com.raad.converter.model.beans.SocketClientInfo;
import com.raad.converter.model.repository.SocketClientInfoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.google.common.base.CharMatcher;


@Component
public class SocketServerComponent {

    public Logger logger = LogManager.getLogger(SocketServerComponent.class);

    @Autowired
    private SocketIOServer socketIOServer;
    @Autowired
    private SocketClientInfoRepository socketClientInfoRepository;

    @PostConstruct
    public void init(){
        logger.info("===========>>>>>>>>>>>>>>>>>>>>>SocketServerComponent-Start<<<<<<<<<<<<<<<<<<<<<<<==================");
        this.socketIOServer.addConnectListener(onConnected());
        this.socketIOServer.addDisconnectListener(onDisconnected());
        // this will tell the server where to listen
        this.socketIOServer.addEventListener("saveSocketClientInfo", String.class, saveSocketClientInfo());
        this.socketIOServer.start();
        logger.info("===========>>>>>>>>>>>>>>>>>>>>>SocketServerComponent-End<<<<<<<<<<<<<<<<<<<<<<<==================");
    }

    public SocketServerComponent() { }

    private ConnectListener onConnected() {
        return client -> {
            HandshakeData handshakeData = client.getHandshakeData();
            logger.info("Client[{}] - Connected to socket through '{}'" + client.getSessionId().toString() + " " + handshakeData.getUrl());
        };
    }

    private DataListener<String> saveSocketClientInfo() {
        return (client, data, ackSender) -> {
            saveSocketClientInfo(client.getSessionId().toString(), CharMatcher.is('\"').trimFrom(data));
            logger.info("Client[{}] - Received data {}", client, data);
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            logger.info("Client[{}] - Disconnected from socket." + client.getSessionId().toString());
        };
    }

    public SocketIOClient sendSocketEventToClient(String token, String jsonMeg) throws Exception {
        logger.info("Send Message With id = {}, message = {}", token, jsonMeg);
        SocketIOClient socketIOClient = null;
        SocketClientInfo socketClientInfo = this.socketClientInfoRepository.findByToken(token);
        if (socketClientInfo != null) {
            socketIOClient = this.socketIOServer.getClient(UUID.fromString(socketClientInfo.getUuid()));
            if (socketIOClient != null) {
                Thread.sleep(100);
                if(socketClientInfo.getSendEventPath() != null && !socketClientInfo.getSendEventPath().equals("")) {
                    socketIOClient.sendEvent(socketClientInfo.getSendEventPath(), decode(jsonMeg));
                } else {
                    socketIOClient.sendEvent("connectionMessage", decode(jsonMeg));
                }
            }
        }
        return socketIOClient;
    }

    private void saveSocketClientInfo(String uuid, String token) throws Exception {
        SocketClientInfo socketClientInfo = this.socketClientInfoRepository.findByToken(token);
        if (socketClientInfo == null) {
            socketClientInfo = new SocketClientInfo();
            socketClientInfo.setToken(token);
        }
        socketClientInfo.setUuid(uuid);
        this.socketClientInfoRepository.save(socketClientInfo);
    }

    private String decode(String value) throws Exception {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
    }

}
