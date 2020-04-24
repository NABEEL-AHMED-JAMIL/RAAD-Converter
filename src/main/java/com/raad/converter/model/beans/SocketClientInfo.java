package com.raad.converter.model.beans;

import com.google.gson.Gson;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity
public class SocketClientInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String uuid;
    private String token;
    private String sendEventPath;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getSendEventPath() { return sendEventPath; }
    public void setSendEventPath(String sendEventPath) { this.sendEventPath = sendEventPath; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
