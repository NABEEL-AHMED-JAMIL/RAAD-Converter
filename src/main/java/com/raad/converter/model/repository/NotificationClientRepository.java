package com.raad.converter.model.repository;

import com.raad.converter.model.enums.Status;
import com.raad.converter.model.pojo.NotificationClient;
import com.raad.converter.model.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface NotificationClientRepository extends JpaRepository<NotificationClient, Long> {

    public Optional<NotificationClient> findByMemberIdAndStatus(User user, Status status);
}
