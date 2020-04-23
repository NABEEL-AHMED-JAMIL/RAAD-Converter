package com.raad.converter.model.mapper;

import com.raad.converter.model.dto.UserDto;
import com.raad.converter.model.enums.Status;
import com.raad.converter.model.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper extends Mapper<UserDto, User> {

    public Logger logger = LogManager.getLogger(UserMapper.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserDto userDto;
    private User user;

    @Override
    public UserDto mapToVo(User user) {
        return null;
    }

    @Override
    public User mapToEntity(UserDto userDto) {
        this.user = new User();
        if(!StringUtils.isEmpty(userDto.getFirstName())) {
            this.user.setFirstName(userDto.getFirstName());
        }
        if(!StringUtils.isEmpty(userDto.getLastName())) {
            this.user.setLastName(userDto.getLastName());
        }
        if(!StringUtils.isEmpty(userDto.getUsername())) {
            this.user.setUsername(userDto.getUsername());
        }
        if(!StringUtils.isEmpty(userDto.getPassword())) {
            this.user.setPassword(this.passwordEncoder
                .encode(userDto.getPassword()));
        }
        this.user.setStatus(Status.Pending);
        return this.user;
    }
}
