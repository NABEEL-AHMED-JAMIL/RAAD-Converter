package com.raad.converter.model.service;

import com.raad.converter.model.enums.Status;
import com.raad.converter.model.pojo.NotificationClient;
import com.raad.converter.model.pojo.User;
import com.raad.converter.model.repository.NotificationClientRepository;
import com.raad.converter.model.repository.NotificationDetailRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Scope("prototype")
public class NotificationClientService {

    public Logger logger = LogManager.getLogger(NotificationClientService.class);

    @Autowired
    private NotificationClientRepository notificationClientRepository;
    @Autowired
    private NotificationDetailRepository notificationDetailRepository;

    public Optional<NotificationClient> findByMemberId(User user, Status status) {
        logger.info("Finding Notification By MemberId {} .", user.getId());
       return notificationClientRepository.findByMemberIdAndStatus(user, status);
    }

    public NotificationClient addNewNotificationClient(NotificationClient notification) {
        logger.info("Adding New Notification Client {} {} ", notification.getTopicId(), notification.getClientPath());
        return notificationClientRepository.save(notification);
    }

}
