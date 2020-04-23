package com.raad.converter.model.repository;

import com.raad.converter.model.pojo.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    public Optional<Authority> findByRole(String role);

}
