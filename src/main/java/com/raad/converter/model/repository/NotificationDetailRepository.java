package com.raad.converter.model.repository;

import com.raad.converter.model.pojo.NotificationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationDetailRepository extends JpaRepository<NotificationDetail, Long> {

}
