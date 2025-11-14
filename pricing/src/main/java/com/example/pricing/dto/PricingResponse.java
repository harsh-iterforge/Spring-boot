// src/main/java/com/example/pricing/dto/PricingResponse.java
package com.example.pricing.dto;
import lombok.*;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PricingResponse {
    private Double basePrice;
    private Integer quantity;
    private Double totalBasePrice;
    private List<AppliedPromotion> appliedPromotions;
    private Double totalDiscount;
    private Double finalPrice;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AppliedPromotion {
        private Long promotionId;
        private String name;
        private String type; // "PERCENTAGE", "FIXED", "BUY_X_GET_Y", ...
        private String description;
        private Double discountAmount;
    }
}
