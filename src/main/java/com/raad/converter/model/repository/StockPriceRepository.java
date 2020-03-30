package com.raad.converter.model.repository;

import com.raad.converter.model.beans.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
}
