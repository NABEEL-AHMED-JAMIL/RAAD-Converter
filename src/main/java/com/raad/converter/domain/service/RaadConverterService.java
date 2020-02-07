package com.raad.converter.domain.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class RaadConverterService {

    public Logger logger = LogManager.getLogger(RaadConverterService.class);
}
