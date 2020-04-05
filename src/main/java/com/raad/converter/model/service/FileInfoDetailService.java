package com.raad.converter.model.service;

import com.raad.converter.domain.FileInfoDto;
import com.raad.converter.model.beans.FileInfo;
import com.raad.converter.model.repository.FileInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Scope(value="prototype")
public class FileInfoDetailService {

    public Logger logger = LoggerFactory.getLogger(FileInfoDetailService.class);

    @Autowired
    private FileInfoRepository fileInfoRepository;

    public List<FileInfo> findByFileInfoIdIn(List<Long> ids) {
        return this.fileInfoRepository.findByFileInfoIdIn(ids);
    }

    public List<FileInfo> save(List<FileInfoDto> fileInfoDtos) {
        return this.fileInfoRepository.saveAll(fileInfoDtos.stream()
            .map(fileInfoDto -> {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileName(fileInfoDto.getFile_name());
                fileInfo.setS3path(fileInfoDto.getS3path());
                return fileInfo;
        }).collect(Collectors.toList()));
    }

    public List<FileInfo> fetchDetail(int page, int size) {
        return this.fileInfoRepository.findAll(PageRequest.of(page, size)).getContent();
    }

}
