package com.raad.converter.model.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.raad.converter.model.enums.Status;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.sql.Timestamp;


@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseMasterEntity {

    @Column(columnDefinition = "timestamp default CURRENT_TIMESTAMP", nullable = false)
    private Timestamp createdAt;

    @Column(name = "created_by_id")
    private Long createdBy;

    @Column(nullable = false)
    private Status status;

    public BaseMasterEntity() { }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
