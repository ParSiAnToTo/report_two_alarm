package com.sparta.stockalarmservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false,name = "product_id")
    private Long id;

    @Column(nullable = false, name = "restock_round")
    private Long restockRound;

    @Column(nullable = false, name = "stock_status")
    private Long stockStatus;
}