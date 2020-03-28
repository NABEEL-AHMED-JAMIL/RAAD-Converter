package com.raad.converter.domain.repository;

import com.raad.converter.domain.pojo.RaadConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaadConverterRepository extends JpaRepository<RaadConverter, Long> { }
