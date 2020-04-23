package com.raad.converter.model.repository;

import com.raad.converter.model.enums.Status;
import com.raad.converter.model.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

     public Optional<User> findByUsernameAndStatus(String username, Status status);

     public Optional<User> findByUsername(String username);


}
