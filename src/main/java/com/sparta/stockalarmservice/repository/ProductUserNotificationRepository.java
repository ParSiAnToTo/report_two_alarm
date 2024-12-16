package com.sparta.stockalarmservice.repository;

import com.sparta.stockalarmservice.entity.ProductUserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductUserNotificationRepository extends JpaRepository<ProductUserNotification, Long> {
    @Query("SELECT u FROM ProductUserNotification u JOIN FETCH u.product WHERE u.product.id = :productId AND u.isActive = true")
    List<ProductUserNotification> findUserByProductAndIsActive(@Param("productId") Long productId);

    @Query("SELECT u FROM ProductUserNotification u WHERE u.product.id = :productId AND u.isActive = true AND u.userId > :lastNotifiedUserId ORDER BY u.userId ASC")
    List<ProductUserNotification> findUserAfterLastNotified(Long productId, Long lastNotifiedUserId);
}
