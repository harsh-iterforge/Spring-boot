// src/main/java/com/example/pricing/controller/PricingController.java
package com.example.pricing.controller;

import com.example.pricing.dto.*;
import com.example.pricing.service.PromotionEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PromotionEngineService engine;

    @PostMapping("/price")
    public ResponseEntity<PricingResponse> price(@RequestBody PricingRequest request) {
        // If cart is supplied, compute first item as example
        if (request.getCart() != null && !request.getCart().isEmpty()) {
            PricingRequest.LineItem item = request.getCart().get(0);
            PricingResponse resp = engine.calculateUnitPrice(item.getProductId(), item.getPrice(),
                    item.getQuantity(), request.getCouponCode(), request.getCustomerSegment());
            return ResponseEntity.ok(resp);
        }

        PricingResponse resp = engine.calculateUnitPrice(request.getProductId(), request.getPrice(),
                request.getQuantity() == null ? 1 : request.getQuantity(),
                request.getCouponCode(), request.getCustomerSegment());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/cart")
    public ResponseEntity<CartResponse> cart(@RequestBody PricingRequest request) {
        if (request.getCart() == null) return ResponseEntity.badRequest().build();

        List<PricingResponse> items = request.getCart().stream().map(item ->
                engine.calculateUnitPrice(item.getProductId(), item.getPrice(), item.getQuantity(), request.getCouponCode(), request.getCustomerSegment())
        ).collect(Collectors.toList());

        double base = items.stream().mapToDouble(PricingResponse::getTotalBasePrice).sum();
        double discount = items.stream().mapToDouble(PricingResponse::getTotalDiscount).sum();
        double finalTotal = items.stream().mapToDouble(PricingResponse::getFinalPrice).sum();

        CartResponse resp = CartResponse.builder()
                .items(items)
                .cartBaseTotal(Math.round(base*100)/100.0)
                .cartDiscountTotal(Math.round(discount*100)/100.0)
                .cartFinalTotal(Math.round(finalTotal*100)/100.0)
                .build();

        return ResponseEntity.ok(resp);
    }
}
