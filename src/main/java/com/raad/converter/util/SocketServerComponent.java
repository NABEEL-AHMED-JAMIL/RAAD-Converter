package com.raad.converter.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.annotation.PostConstruct;

import com.raad.converter.domain.FileSocket;
import com.raad.converter.model.beans.SocketClientInfo;
import com.raad.converter.model.repository.SocketClientInfoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.google.common.base.CharMatcher;

@Component
@Scope(value="prototype")
public class SocketServerComponent {

    public Logger logger = LogManager.getLogger(SocketServerComponent.class);

    private static volatile boolean isRDInitialized = false;
    @Autowired
    private SocketIOServer socketIOServer;
    @Autowired
    private SocketClientInfoRepository socketClientInfoRepository;

    @PostConstruct
    public void init(){
        if (!isRDInitialized) {
            logger.info("===========>>>>>>>>>>>>>SocketServerComponent-Start<<<<<<<<==================");
            this.socketIOServer.addConnectListener(onConnected());
            this.socketIOServer.addDisconnectListener(onDisconnected());
            // this will tell the server where to listen
            this.socketIOServer.addEventListener("saveSocketClientInfo", String.class, saveSocketClientInfo());
            this.socketIOServer.start();
            logger.info("===========>>>>>>>>>>>>>>SocketServerComponent-End<<<<<<<<<<==================");
            isRDInitialized = true;
        }
    }

    public SocketServerComponent() { }

    private ConnectListener onConnected() {
        return client -> {
            HandshakeData handshakeData = client.getHandshakeData();
            logger.info("Client[{}] - Connected to socket through '{}'" , client.getSessionId().toString() ,
                    handshakeData.getUrl());
        };
    }

    private DataListener<String> saveSocketClientInfo() {
        return (client, data, ackSender) -> {
            this.saveSocketClientInfo(client.getSessionId().toString(), CharMatcher.is('\"').trimFrom(data));
            logger.info("Client[{}] - Received data {}", client.getSessionId(), data);
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            logger.info("Client[{}] - Disconnected from socket." , client.getSessionId().toString());
        };
    }

    public SocketIOClient sendSocketEventToClient(String token, String jsonMeg) throws Exception {
        logger.info("Send Message With token = {}, message = {}", token, jsonMeg);
        SocketIOClient socketIOClient = null;
        SocketClientInfo socketClientInfo = this.socketClientInfoRepository.findByToken(token);
        if (socketClientInfo != null) {
            socketIOClient = this.socketIOServer.getClient(UUID.fromString(socketClientInfo.getUuid()));
            if (socketIOClient != null) {
                Thread.sleep(100);
                if(socketClientInfo.getSendEventPath() != null && !socketClientInfo.getSendEventPath().equals("")) {
                    socketIOClient.sendEvent(socketClientInfo.getSendEventPath(), decode(jsonMeg));
                } else {
                    socketIOClient.sendEvent("uploadFileMessage", decode(jsonMeg));
                }
            }
        }
        return socketIOClient;
    }

    public SocketIOClient sendSocketEventToClient(String token, byte[] downloadFile) throws Exception {
        logger.info("Send Message With token = {}", token);
        SocketIOClient socketIOClient = null;
        SocketClientInfo socketClientInfo = this.socketClientInfoRepository.findByToken(token);
        if (socketClientInfo != null) {
            socketIOClient = this.socketIOServer.getClient(UUID.fromString(socketClientInfo.getUuid()));
            if (socketIOClient != null) {
                Thread.sleep(100);
                if(socketClientInfo.getSendEventPath() != null && !socketClientInfo.getSendEventPath().equals("")) {
                    socketIOClient.sendEvent(socketClientInfo.getSendEventPath(), downloadFile);
                } else {
                    socketIOClient.sendEvent("uploadFileMessage", downloadFile);
                }
            }
        }
        return socketIOClient;
    }

    public SocketIOClient sendSocketEventToClient(String token, FileSocket fileSocket) throws Exception {
        logger.info("Send Message With token = {}", token);
        SocketIOClient socketIOClient = null;
        SocketClientInfo socketClientInfo = this.socketClientInfoRepository.findByToken(token);
        if (socketClientInfo != null) {
            socketIOClient = this.socketIOServer.getClient(UUID.fromString(socketClientInfo.getUuid()));
            if (socketIOClient != null) {
                Thread.sleep(100);
                if(socketClientInfo.getSendEventPath() != null && !socketClientInfo.getSendEventPath().equals("")) {
                    socketIOClient.sendEvent(socketClientInfo.getSendEventPath(), fileSocket);
                } else {
                    socketIOClient.sendEvent("uploadFileMessage", fileSocket);
                }
            }
        }
        return socketIOClient;
    }

    private void saveSocketClientInfo(String session, String token) {
        SocketClientInfo socketClientInfo = this.socketClientInfoRepository.findByToken(token);
        if (socketClientInfo == null) {
            socketClientInfo = new SocketClientInfo();
            socketClientInfo.setToken(token);
        }
        socketClientInfo.setUuid(session);
        this.socketClientInfoRepository.save(socketClientInfo);
    }

    private String decode(String value) throws Exception {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
    }

    public int i=0;
    //@Scheduled(fixedDelay = 500)
    public void scheduleFixedDelayTask() throws Exception {
        if(i == 101) {
            i=0;
        } else {
            sendSocketEventToClient("9faacaf6-e053-4812-8c7d-6be8b7f91596", i+"");
            i = i+1;
        }
    }

    //@Scheduled(fixedDelay = 1000)
    public void scheduleFixedDelayTaskV2() throws Exception {
        System.out.println("Fixed delay task - " + System.currentTimeMillis() / 1000);
        sendSocketEventToClient("a5f97b77-2279-46f7-a50b-85837709fffd", "Zindabad");
    }

}
