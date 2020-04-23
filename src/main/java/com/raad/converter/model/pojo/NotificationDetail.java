package com.raad.converter.model.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.raad.converter.model.enums.Flag;
import com.raad.converter.model.enums.NotificationType;
import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "notification_detail")
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String detail;

    private Flag flag;

    private Timestamp sendTime;

    private NotificationType notificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    private NotificationClient notificationClient;

    public NotificationDetail() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public Flag getFlag() { return flag; }
    public void setFlag(Flag flag) { this.flag = flag; }

    public Timestamp getSendTime() { return sendTime; }
    public void setSendTime(Timestamp sendTime) { this.sendTime = sendTime; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public NotificationClient getNotificationClient() { return notificationClient; }
    public void setNotificationClient(NotificationClient notificationClient) { this.notificationClient = notificationClient; }

    @Override
    public String toString() { return new Gson().toJson(this); }

}
