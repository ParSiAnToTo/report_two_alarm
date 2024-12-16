package com.sparta.stockalarmservice.repository;

import com.sparta.stockalarmservice.entity.ProductNotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductNotificationHistoryRepository extends JpaRepository<ProductNotificationHistory, Long> {
    Optional<ProductNotificationHistory> findTopByProductIdAndRestockRoundOrderByIdDesc(Long productId, Long restockRound);

}
