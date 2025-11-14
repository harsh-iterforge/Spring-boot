// src/main/java/com/example/pricing/service/PromotionEngineService.java
package com.example.pricing.service;

import com.example.pricing.client.ProductClient;
import com.example.pricing.client.dto.ProductDto;
import com.example.pricing.dto.*;
import com.example.pricing.entity.Promotion;
import com.example.pricing.repo.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PromotionEngineService {

    private final PromotionRepository promotionRepository;
    private final ProductClient productClient;

    public PricingResponse calculateUnitPrice(Long productId, Double fallbackPrice, int quantity,
                                              String couponCode, String customerSegment) {
        // fetch product info
        ProductDto product = null;
        if (productId != null) {
            try { product = productClient.getProductById(productId); } catch (Exception ignored) {}
        }
        double basePrice = (product != null && product.getPrice() != null) ? product.getPrice() : (fallbackPrice != null ? fallbackPrice : 0.0);

        // Get active promotions
        List<Promotion> activePromotions = promotionRepository.findAll(); // we'll filter below
        Instant now = Instant.now();

        List<PricingResponse.AppliedPromotion> applied = new ArrayList<>();
        double totalDiscount = 0.0;

        for (Promotion p : activePromotions) {
            if (!Boolean.TRUE.equals(p.getActive())) continue;
            if (p.getStartAt() != null && p.getStartAt().isAfter(now)) continue;
            if (p.getEndAt() != null && p.getEndAt().isBefore(now)) continue;
            if (p.getCouponCode() != null && couponCode != null && !p.getCouponCode().equalsIgnoreCase(couponCode)) continue;
            if (p.getCouponCode() != null && couponCode == null) continue; // coupon required
            if (p.getApplicableProductIds() != null && productId != null && !p.getApplicableProductIds().isEmpty()
                    && !p.getApplicableProductIds().contains(productId)) {
                continue;
            }
            if (p.getApplicableCustomerSegments() != null && customerSegment != null &&
                    !p.getApplicableCustomerSegments().isEmpty() &&
                    !p.getApplicableCustomerSegments().contains(customerSegment)) {
                continue;
            }

            double discountForPromotion = 0.0;
            switch (p.getType()) {
                case "PERCENTAGE":
                    discountForPromotion = (p.getValue() != null ? p.getValue() : 0.0) / 100.0 * basePrice * quantity;
                    break;
                case "FIXED":
                    discountForPromotion = (p.getFixedAmount() != null ? p.getFixedAmount() : 0.0) * quantity;
                    break;
                case "BUY_X_GET_Y":
                    // apply for full multiples of buyQty
                    int b = p.getBuyQty() == null ? 0 : p.getBuyQty();
                    int g = p.getGetQty() == null ? 0 : p.getGetQty();
                    if (b > 0 && g > 0) {
                        int times = quantity / (b + g);
                        // Alternatively, some prefer floor(quantity / b) -> free items = times * g etc.
                        int freeItems = (quantity / (b + g)) * g;
                        discountForPromotion = freeItems * basePrice;
                    }
                    break;
                case "TIERED":
                    // simple example: p.value holds percent, but you could have tiers stored elsewhere
                    discountForPromotion = (p.getValue() != null ? p.getValue() : 0.0) / 100.0 * basePrice * quantity;
                    break;
                default:
                    break;
            }

            if (discountForPromotion > 0) {
                applied.add(PricingResponse.AppliedPromotion.builder()
                        .promotionId(p.getId())
                        .name(p.getName())
                        .type(p.getType())
                        .description(p.getDescription())
                        .discountAmount(round(discountForPromotion))
                        .build());
                totalDiscount += discountForPromotion;
            }
        }

        double totalBase = basePrice * quantity;
        double finalPrice = Math.max(0.0, totalBase - totalDiscount);

        return PricingResponse.builder()
                .basePrice(round(basePrice))
                .quantity(quantity)
                .totalBasePrice(round(totalBase))
                .appliedPromotions(applied)
                .totalDiscount(round(totalDiscount))
                .finalPrice(round(finalPrice))
                .build();
    }

    private double round(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
