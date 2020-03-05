package com.raad.converter.domain.service;

import com.raad.converter.domain.pojo.RaadConverter;
import com.raad.converter.domain.repository.RaadConverterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Scope("prototype")
public class RaadConverterService {

    public Logger logger = LoggerFactory.getLogger(RaadConverterService.class);

    @Autowired
    private RaadConverterRepository raadConverterRepository;

    public void saveFileInfo(RaadConverter raadConverter) {
        logger.info("Raad Convert File Detail :- " + raadConverter);
        this.raadConverterRepository.save(raadConverter);
    }
}
