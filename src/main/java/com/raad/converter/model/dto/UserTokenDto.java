package com.raad.converter.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserTokenDto {

    private Long memberId;
    private String firstName;
    private String lastName;
    private String jwtToken;
    private Long expiresTime;
    private String topicId;
    private String clientPath;

    public UserTokenDto() { }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getJwtToken() { return jwtToken; }
    public void setJwtToken(String jwtToken) { this.jwtToken = jwtToken; }

    public Long getExpiresTime() { return expiresTime; }
    public void setExpiresTime(Long expiresTime) { this.expiresTime = expiresTime; }

    public String getTopicId() { return topicId; }
    public void setTopicId(String topicId) { this.topicId = topicId; }

    public String getClientPath() { return clientPath; }
    public void setClientPath(String clientPath) { this.clientPath = clientPath; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
