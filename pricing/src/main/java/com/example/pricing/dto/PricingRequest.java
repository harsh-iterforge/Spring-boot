// src/main/java/com/example/pricing/dto/PricingRequest.java
package com.example.pricing.dto;
import lombok.*;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PricingRequest {
    private Long productId;         // optional if price provided
    private Double price;           // optional (fallback)
    private Integer quantity = 1;
    private String couponCode;      // optional
    private String customerSegment; // optional e.g. "VIP"
    private List<LineItem> cart;    // if calculating cart totals, supply items instead of productId
    @Data public static class LineItem {
        private Long productId;
        private Double price; // optional
        private Integer quantity = 1;
    }
}
