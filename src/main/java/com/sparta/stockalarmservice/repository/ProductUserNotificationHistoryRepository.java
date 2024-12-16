package com.sparta.stockalarmservice.repository;

import com.sparta.stockalarmservice.entity.ProductUserNotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUserNotificationHistoryRepository extends JpaRepository<ProductUserNotificationHistory, Long> {
}
