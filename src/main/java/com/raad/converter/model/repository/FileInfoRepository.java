package com.raad.converter.model.repository;

import com.raad.converter.model.beans.FileInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import javax.transaction.Transactional;
import java.util.List;


@Repository
@Transactional
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    @Query("SELECT fileInfo FROM FileInfo fileInfo WHERE fileInfo.id IN (:ids)")
    List<FileInfo> findByFileInfoIdIn(@Param("ids")List<Long> ids);

    Page<FileInfo> findAll(Pageable pageable);

}
