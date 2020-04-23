package com.raad.converter.api;

import com.raad.converter.model.dto.ResponseDTO;
import com.raad.converter.model.dto.UserDto;
import com.raad.converter.model.dto.UserTokenDto;
import com.raad.converter.model.enums.ApiCode;
import com.raad.converter.model.enums.Status;
import com.raad.converter.model.pojo.NotificationClient;
import com.raad.converter.model.pojo.User;
import com.raad.converter.model.service.NotificationClientService;
import com.raad.converter.model.service.UserService;
import com.raad.converter.security.JwtAuthenticationRequest;
import com.raad.converter.security.TokenHelper;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = { "Barco-Auth := Barco-Auth EndPoint" })
public class AuthRestApi {

    public Logger logger = LogManager.getLogger(AuthRestApi.class);

    @Value("${jwt.expires_in}")
    private int EXPIRES_IN;

    @Autowired
    private TokenHelper tokenHelper;

    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private NotificationClientService notificationClientService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody JwtAuthenticationRequest authenticationReq) {
        try {
            final Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationReq.getUsername(), authenticationReq.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User login_user = (User) authentication.getPrincipal();
            logger.info("User Found In Db With Id {}", login_user.getId());
            String jwtToken = this.tokenHelper.generateToken(login_user.getUsername());
            UserTokenDto userTokenDto = new UserTokenDto();
            if(login_user.getId() != null) {
                userTokenDto.setMemberId(login_user.getId());
            }
            if(login_user.getFirstName() != null) {
                userTokenDto.setFirstName(login_user.getFirstName());
            }
            if(login_user.getLastName() != null) {
                userTokenDto.setLastName(login_user.getLastName());
            }
            userTokenDto.setJwtToken(jwtToken);
            userTokenDto.setExpiresTime(Long.valueOf(EXPIRES_IN));
            Optional<NotificationClient> notificationClientPresent = this.notificationClientService
                    .findByMemberId(login_user, Status.Active);
            if(notificationClientPresent.isPresent()) {
                NotificationClient notificationClient = notificationClientPresent.get();
                if(notificationClient.getTopicId() != null) {
                    userTokenDto.setTopicId(notificationClient.getTopicId());
                }
                if(notificationClient.getClientPath() != null) {
                    userTokenDto.setClientPath(notificationClient.getClientPath());
                }
            }
            return ResponseEntity.ok()
                .body(new ResponseDTO(ApiCode.SUCCESS,"Successfully login.", userTokenDto));
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                 .body(new ResponseDTO(ApiCode.ERROR, ex.getMessage()+"."));
        }
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ResponseEntity<?> signup(@RequestBody UserDto userDto) {
        try {
            if(this.isValidateDetail(userDto)) {
                if(this.userService.findByUsername(userDto.getUsername()).isPresent()) {
                    return ResponseEntity.badRequest()
                        .body(new ResponseDTO(ApiCode.INVALID_REQUEST,"User already exist."));
                } else {
                    User user = this.userService.save(userDto);
                    userDto.setMemberId(user.getId());
                    userDto.setPassword(null);
                    return ResponseEntity.ok()
                        .body(new ResponseDTO(ApiCode.SUCCESS,"User create successfully.", userDto));
                }
            } else {
                return ResponseEntity.badRequest()
                    .body(new ResponseDTO(ApiCode.INVALID_REQUEST,"Request not valid.", userDto));
            }
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                .body(new ResponseDTO(ApiCode.ERROR, ex.getMessage()));
        }
    }

    public boolean isValidateDetail(UserDto userDto) {
        if(StringUtils.isEmpty(userDto.getFirstName()) || StringUtils.isEmpty(userDto.getLastName()) ||
            StringUtils.isEmpty(userDto.getUsername()) || StringUtils.isEmpty(userDto.getPassword())) {
            return false;
        }
        return true;
    }

}