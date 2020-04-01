package com.raad.converter.model.repository;

import com.raad.converter.model.beans.SocketClientInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocketClientInfoRepository  extends JpaRepository<SocketClientInfo, Long> {

    SocketClientInfo findByToken(String token);
}
