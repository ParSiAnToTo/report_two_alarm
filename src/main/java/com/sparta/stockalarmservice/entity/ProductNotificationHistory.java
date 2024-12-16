package com.sparta.stockalarmservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product_notification_history")
public class ProductNotificationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "restock_round", nullable = false)
    private Long restockRound;

    @Column(name = "notification_status", nullable = false)
    private String notificationStatus;

    @Column(name = "last_notified_user_id")
    private Long lastNotifiedUserId;

}




