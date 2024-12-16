package com.sparta.stockalarmservice.controller;

import com.sparta.stockalarmservice.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/products/{productId}/notifications/re-stock")
    public ResponseEntity<?> reStockAlarm(@PathVariable("productId") Long productId) {
        try {
            stockService.sendRestockNotification(productId, "auto");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/admin/products/{productId}/notifications/re-stock")
    public ResponseEntity<?> manualReStockAlarm(@PathVariable("productId") Long productId) {
        try {
            stockService.sendRestockNotification(productId, "manual");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}


