package com.raad.converter.domain.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;


@Entity
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RaadConverter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String inputType;
    private String outPutType;
    private Timestamp requestTime;
    private Timestamp startTime;
    private Timestamp finishTime;
    private String elapsedTime;
    private String status;
    private String actions;
    private String originalFile;
    private String convertFile;

    public RaadConverter() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInputType() { return inputType; }
    public void setInputType(String inputType) { this.inputType = inputType; }

    public String getOutPutType() { return outPutType; }
    public void setOutPutType(String outPutType) { this.outPutType = outPutType; }

    public Timestamp getRequestTime() { return requestTime; }
    public void setRequestTime(Timestamp requestTime) { this.requestTime = requestTime; }

    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

    public Timestamp getFinishTime() { return finishTime; }
    public void setFinishTime(Timestamp finishTime) { this.finishTime = finishTime; }

    public String getElapsedTime() { return elapsedTime; }
    public void setElapsedTime(String elapsedTime) { this.elapsedTime = elapsedTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getActions() { return actions; }
    public void setActions(String actions) { this.actions = actions; }

    public String getOriginalFile() { return originalFile; }
    public void setOriginalFile(String originalFile) { this.originalFile = originalFile; }

    public String getConvertFile() { return convertFile; }
    public void setConvertFile(String convertFile) { this.convertFile = convertFile; }

    @Override
    public String toString() { return new Gson().toJson(this); }

}
