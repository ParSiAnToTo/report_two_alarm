package com.sparta.stockalarmservice.service;

import com.sparta.stockalarmservice.entity.Product;
import com.sparta.stockalarmservice.entity.ProductNotificationHistory;
import com.sparta.stockalarmservice.entity.ProductUserNotification;
import com.sparta.stockalarmservice.entity.ProductUserNotificationHistory;
import com.sparta.stockalarmservice.repository.ProductNotificationHistoryRepository;
import com.sparta.stockalarmservice.repository.ProductRepository;
import com.sparta.stockalarmservice.repository.ProductUserNotificationHistoryRepository;
import com.sparta.stockalarmservice.repository.ProductUserNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;
    private final ProductNotificationHistoryRepository productNotificationHistoryRepository;
    private final ProductUserNotificationRepository productUserNotificationRepository;
    private final ProductUserNotificationHistoryRepository productUserNotificationHistoryRepository;


    @Transactional
    public void sendRestockNotification(Long productId) {
        Long startTime = System.currentTimeMillis();

        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product Not Found"));

        log.info("알람 발송 시작 : {}, {}, {}", product.getId(), product.getRestockRound(), product.getStockStatus());

        product.setRestockRound(product.getRestockRound() + 1);
        product.setStockStatus(product.getStockStatus() + 500);

        log.info("인포 업데이트 : {}, {}, {}", product.getId(), product.getRestockRound(), product.getStockStatus());

        ProductNotificationHistory notificationLog = ProductNotificationHistory.builder()
                .product(product)
                .restockRound(product.getRestockRound())
                .notificationStatus("IN_PROGRESS")
                .build();

        productNotificationHistoryRepository.save(notificationLog);
        log.info("알림 상태 갱신 : {}, {}", notificationLog.getProduct().getId(), notificationLog.getNotificationStatus());


        List<ProductUserNotification> activeAlarm = productUserNotificationRepository.findUserByProductAndIsActive(productId);

        log.info("알람 활성화 수 : {}", activeAlarm.size());


        Long currentStock = product.getStockStatus();
        String currentStatus = "IN_PROGRESS";
        Long lastNotifiedUser = null;
        for (ProductUserNotification notification : activeAlarm) {
            currentStock--;
            if (currentStock >= 0) {
                ProductUserNotificationHistory userNotificationHistory = ProductUserNotificationHistory.builder()
                        .product(product)
                        .userId(notification.getUserId())
                        .restockRound(product.getRestockRound())
                        .notifiedAt(LocalDateTime.now())
                        .build();
                productUserNotificationHistoryRepository.save(userNotificationHistory);
                lastNotifiedUser = notification.getUserId();
            } else {
                log.info("empty stock");
                currentStatus = "CANCELED_BY_SOLD_OUT";
                break;
            }
        }

        log.info("알람 발송 완료");
        if (currentStatus.equals("IN_PROGRESS")) {
            currentStatus = "COMPLETED";
        }

        ProductNotificationHistory afterNotificationLog = ProductNotificationHistory.builder()
                .product(product)
                .restockRound(product.getRestockRound())
                .notificationStatus(currentStatus)
                .lastNotifiedUserId(lastNotifiedUser)
                .build();

        productNotificationHistoryRepository.save(afterNotificationLog);
        log.info("상품 알람 기록 업데이트 : {}, {}", afterNotificationLog.getProduct().getId(), afterNotificationLog.getNotificationStatus());

        Long endTime = System.currentTimeMillis();
        log.info("처리 시간 : {} ms", endTime - startTime);

    }
}
