package com.raad.converter.model.service;

import com.raad.converter.model.dto.UserDto;
import com.raad.converter.model.enums.Status;
import com.raad.converter.model.mapper.UserMapper;
import com.raad.converter.model.pojo.Authority;
import com.raad.converter.model.pojo.NotificationClient;
import com.raad.converter.model.pojo.User;
import com.raad.converter.model.repository.AuthorityRepository;
import com.raad.converter.model.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Scope("prototype")
public class UserService {

    public Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private NotificationClientService notificationClientService;


    public Optional<User> findByUsernameAndStatus(String username) {
        logger.info("Finding User By Username and Status");
        return this.userRepository.findByUsernameAndStatus(username, Status.Active);
    }

    public Optional<User> findByUsername(String username) {
        logger.info("Finding User By Username");
        return this.userRepository.findByUsername(username);
    }

    public User save(UserDto userDto) {
        User user = this.userMapper.mapToEntity(userDto);
        if(!StringUtils.isEmpty(userDto.getRole())) {
            Optional<Authority> authority = this.authorityRepository.findByRole(userDto.getRole());
            if(authority.isPresent()) {
                List<Authority> authorityList = new ArrayList<>();
                authorityList.add(authority.get());
                user.setAuthorities(authorityList);
            }
        }
        user = this.userRepository.save(user);
        if(StringUtils.isNotEmpty(userDto.getTopicId()) && StringUtils.isNotEmpty(userDto.getClientPath())) {
            NotificationClient notificationClient = new NotificationClient();
            notificationClient.setTopicId(userDto.getTopicId());
            notificationClient.setClientPath(userDto.getClientPath());
            notificationClient.setMemberId(user);
            notificationClient.setStatus(Status.Active);
            this.notificationClientService.addNewNotificationClient(notificationClient);
        }
        return user;
    }
}