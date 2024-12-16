package com.sparta.stockalarmservice.repository;

import com.sparta.stockalarmservice.entity.ProductNotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductNotificationHistoryRepository extends JpaRepository<ProductNotificationHistory, Long> {
}
