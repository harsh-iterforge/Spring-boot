// src/main/java/com/example/pricing/dto/CartResponse.java
package com.example.pricing.dto;
import lombok.*;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CartResponse {
    private List<PricingResponse> items;
    private Double cartBaseTotal;
    private Double cartDiscountTotal;
    private Double cartFinalTotal;
}
