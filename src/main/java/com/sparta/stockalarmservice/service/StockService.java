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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;
    private final ProductNotificationHistoryRepository productNotificationHistoryRepository;
    private final ProductUserNotificationRepository productUserNotificationRepository;
    private final ProductUserNotificationHistoryRepository productUserNotificationHistoryRepository;


    @Transactional
    public void sendRestockNotification(Long productId, String manualInput) {
        int batchSize = 500;
        Long ProcessDelay = 1000L;

        Product product = productRepository.findByIdWithLock(productId).orElseThrow(() -> new IllegalArgumentException("Product Not Found"));

        ProductNotificationHistory history = null;
        if (manualInput.equals("auto")) {
            log.info("알람 발송 시작 : {}, {}, {}", product.getId(), product.getRestockRound(), product.getStockStatus());
            product.setRestockRound(product.getRestockRound() + 1);
            product.setStockStatus(product.getStockStatus() + 500);
            log.info("인포 업데이트 : {}, {}, {}", product.getId(), product.getRestockRound(), product.getStockStatus());
        }
        else if (manualInput.equals("manual")) {
            log.info("알람 재발송 시작 : {}, {}, {}", product.getId(), product.getRestockRound(), product.getStockStatus());
            Optional<ProductNotificationHistory> historyOptional = productNotificationHistoryRepository.findTopByProductIdAndRestockRoundOrderByIdDesc(productId, product.getRestockRound());

            if (historyOptional.isEmpty()) {
                throw new IllegalArgumentException("Notification History Not Found");
            } else if (historyOptional.get().getLastNotifiedUserId() == null) {
                log.info("더 이상 보낼 유저가 없습니다 : {}", productId);
                throw new IllegalArgumentException("Re-stock Notification is already Finished");
            } else {
                history = historyOptional.get();
            }

        } else {
            throw new IllegalArgumentException("Invalid Mode");
        }

        ProductNotificationHistory notificationLog = ProductNotificationHistory.builder()
                .product(product)
                .restockRound(product.getRestockRound())
                .notificationStatus("IN_PROGRESS")
                .build();

        productNotificationHistoryRepository.save(notificationLog);
        log.info("알림 상태 갱신 : {}, {}", notificationLog.getProduct().getId(), notificationLog.getNotificationStatus());


        List<ProductUserNotification> activeAlarm = List.of();
        if (manualInput.equals("auto")) {
            activeAlarm = productUserNotificationRepository.findUserByProductAndIsActive(productId);
        } else if (manualInput.equals("manual")) {
            log.info("다음 알림 대상 인덱스 : {}", history.getLastNotifiedUserId());
            activeAlarm = productUserNotificationRepository.findUserAfterLastNotified(productId, history.getLastNotifiedUserId());
        }

        log.info("알람 활성화 수 : {}", activeAlarm.size());


        Long currentStock = product.getStockStatus();
        String currentStatus = "IN_PROGRESS";
        Long lastNotifiedUser = null;

        outer:
        for (int i = 0; i < activeAlarm.size(); i += batchSize) {
            Long startTime = System.currentTimeMillis();

            int end = Math.min(i + batchSize, activeAlarm.size());
            List<ProductUserNotification> batch = activeAlarm.subList(i, end);
            log.info("처리 인덱스: {} - {}", i, end);

            for (ProductUserNotification notification : batch) {
                try {
                    if (currentStock == 0) {
                        log.info("empty stock");
                        currentStatus = "CANCELED_BY_SOLD_OUT";
                        break outer;
                    }
                    alarmProcess(product, notification);
                    currentStock--;
                    lastNotifiedUser = notification.getUserId();

                } catch (Exception e) {
                    log.error("알림 발송 실패 : {}", notification.getUserId());
                    currentStatus = "CANCELED_BY_ERROR";
                    break outer;
                }
            }

            Long endTime = System.currentTimeMillis();
            Long processTime = endTime - startTime;
            log.info("배치 처리 시간: {} ms", processTime);

            if (processTime < ProcessDelay) {
                try {
                    Thread.sleep(ProcessDelay - processTime);
                    log.info("대기 시간: {} ms", ProcessDelay - processTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("대기 중 인터럽트 발생");
                }
            }
        }

        log.info("알람 전송 프로세스 종료");
        if (currentStatus.equals("IN_PROGRESS")) {
            currentStatus = "COMPLETED";
        }

        notificationLog.setNotificationStatus(currentStatus);
        notificationLog.setLastNotifiedUserId(lastNotifiedUser);
        log.info("상품 알람 기록 업데이트 : {}, {}", notificationLog.getProduct().getId(), notificationLog.getNotificationStatus());
    }

    @Transactional
    public void alarmProcess(Product product, ProductUserNotification notification) {
        sendToUser(notification);
        ProductUserNotificationHistory userNotificationHistory = ProductUserNotificationHistory.builder()
                .product(product)
                .userId(notification.getUserId())
                .restockRound(product.getRestockRound())
                .notifiedAt(LocalDateTime.now())
                .build();

        productUserNotificationHistoryRepository.save(userNotificationHistory);
    }

    public void sendToUser(ProductUserNotification notification) {
        if (Math.random() < 0.01) {
            throw new RuntimeException("Failed to send Notification");
        }
    }
}
