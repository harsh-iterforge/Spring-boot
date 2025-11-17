package com.example.pricing.service;

import com.example.pricing.client.ProductClient;
import com.example.pricing.dto.PricingResponse;
import com.example.pricing.entity.Promotion;
import com.example.pricing.repo.PromotionRepository;
import com.example.userservice.model.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PromotionEngineService {

    private static final Logger log = LoggerFactory.getLogger(PromotionEngineService.class);

    private final PromotionRepository promotionRepository;
    private final ProductClient productClient;

    /**
     * Calculate final price for given productId / fallbackPrice / quantity with promotions applied.
     * Logs Feign call details and the entire decision process to the console (via SLF4J).
     */
    public PricingResponse calculateUnitPrice(Long productId, Double fallbackPrice, int quantity,
                                              String couponCode, String customerSegment) {
        log.debug("calculateUnitPrice called with productId={}, fallbackPrice={}, quantity={}, couponCode={}, customerSegment={}",
                productId, fallbackPrice, quantity, couponCode, customerSegment);

        // --- Fetch product info via Feign and log details ---
        ProductResponse product = null;
        if (productId != null) {
            try {
                log.debug("Calling product service via Feign for id {}", productId);
                // If your ProductClient returns ResponseEntity<ProductDto> change accordingly.
                product = productClient.getById(productId).getBody();
                log.debug("Received ProductDto from product service: {}", product);
            } catch (Exception ex) {
                // Log full stacktrace so you can see Feign error in console
                log.error("Exception when calling product service for id {}: {}", productId, ex.getMessage(), ex);
            }
        } else {
            log.debug("productId is null, will use fallbackPrice if provided");
        }

        // Determine base price
        double basePrice;
        if (product != null && product.getPrice() != null) {
            basePrice = product.getPrice();
            log.debug("Using price from product service: {}", basePrice);
        } else if (fallbackPrice != null) {
            basePrice = fallbackPrice;
            log.debug("Using provided fallbackPrice: {}", basePrice);
        } else {
            basePrice = 0.0;
            log.warn("No price available from product service or fallback; defaulting basePrice to 0.0");
        }

        // --- Get promotions and filter/apply them, logging decisions ---
        List<Promotion> allPromotions = promotionRepository.findAll();
        log.debug("Loaded {} promotions from repository", allPromotions.size());

        Instant now = Instant.now();
        List<PricingResponse.AppliedPromotion> applied = new ArrayList<>();
        double totalDiscount = 0.0;

        for (Promotion p : allPromotions) {
            log.debug("Evaluating promotion id={} name={} active={} type={}", p.getId(), p.getName(), p.getActive(), p.getType());

            // basic checks
            if (!Boolean.TRUE.equals(p.getActive())) {
                log.debug("Skipping promotion {} because it's not active", p.getId());
                continue;
            }
            if (p.getStartAt() != null && p.getStartAt().isAfter(now)) {
                log.debug("Skipping promotion {} because it hasn't started yet (startAt={})", p.getId(), p.getStartAt());
                continue;
            }
            if (p.getEndAt() != null && p.getEndAt().isBefore(now)) {
                log.debug("Skipping promotion {} because it already ended (endAt={})", p.getId(), p.getEndAt());
                continue;
            }

            // coupon checks
            if (p.getCouponCode() != null) {
                if (couponCode == null) {
                    log.debug("Skipping promotion {} because it requires coupon '{}' and none provided", p.getId(), p.getCouponCode());
                    continue;
                }
                if (!p.getCouponCode().equalsIgnoreCase(couponCode)) {
                    log.debug("Skipping promotion {} because coupon mismatch (expected='{}' provided='{}')", p.getId(), p.getCouponCode(), couponCode);
                    continue;
                }
            }

            // product applicability
            if (p.getApplicableProductIds() != null && !p.getApplicableProductIds().isEmpty()) {
                if (productId == null || !p.getApplicableProductIds().contains(productId)) {
                    log.debug("Skipping promotion {} because it's not applicable to productId {}", p.getId(), productId);
                    continue;
                }
            }

            // customer segment applicability
            if (p.getApplicableCustomerSegments() != null && !p.getApplicableCustomerSegments().isEmpty()) {
                if (customerSegment == null || !p.getApplicableCustomerSegments().contains(customerSegment)) {
                    log.debug("Skipping promotion {} because it's not applicable to customerSegment {}", p.getId(), customerSegment);
                    continue;
                }
            }

            double discountForPromotion = 0.0;
            String type = p.getType() == null ? "" : p.getType().toUpperCase(Locale.ROOT);
            switch (type) {
                case "PERCENTAGE": {
                    double pct = p.getValue() == null ? 0.0 : p.getValue();
                    discountForPromotion = pct / 100.0 * basePrice * quantity;
                    log.debug("Promotion {} is PERCENTAGE: {}% -> discount {}", p.getId(), pct, discountForPromotion);
                    break;
                }
                case "FIXED": {
                    double fixed = p.getFixedAmount() == null ? 0.0 : p.getFixedAmount();
                    discountForPromotion = fixed * quantity;
                    log.debug("Promotion {} is FIXED: {} per unit -> discount {}", p.getId(), fixed, discountForPromotion);
                    break;
                }
                case "BUY_X_GET_Y": {
                    int b = p.getBuyQty() == null ? 0 : p.getBuyQty();
                    int g = p.getGetQty() == null ? 0 : p.getGetQty();
                    if (b > 0 && g > 0) {
                        int freeItems = (quantity / (b + g)) * g;
                        discountForPromotion = freeItems * basePrice;
                        log.debug("Promotion {} is BUY_X_GET_Y: buy {} get {} -> freeItems={} discount={}", p.getId(), b, g, freeItems, discountForPromotion);
                    } else {
                        log.debug("Promotion {} BUY_X_GET_Y has invalid buy/get values b={} g={}", p.getId(), b, g);
                    }
                    break;
                }
                case "TIERED": {
                    double tierPct = p.getValue() == null ? 0.0 : p.getValue();
                    discountForPromotion = tierPct / 100.0 * basePrice * quantity;
                    log.debug("Promotion {} is TIERED (treated as {}% for now) -> discount {}", p.getId(), tierPct, discountForPromotion);
                    break;
                }
                default: {
                    log.debug("Promotion {} has unsupported type '{}', skipping calculation", p.getId(), p.getType());
                    break;
                }
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
                log.debug("Applied promotion {} -> discount {} ; running totalDiscount={}", p.getId(), discountForPromotion, totalDiscount);
            } else {
                log.debug("No discount computed for promotion {}", p.getId());
            }
        }

        double totalBase = basePrice * quantity;
        double finalPrice = Math.max(0.0, totalBase - totalDiscount);

        PricingResponse result = PricingResponse.builder()
                .basePrice(round(basePrice))
                .quantity(quantity)
                .totalBasePrice(round(totalBase))
                .appliedPromotions(applied)
                .totalDiscount(round(totalDiscount))
                .finalPrice(round(finalPrice))
                .build();

        log.debug("Pricing result computed: {}", result);
        return result;
    }

    private double round(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
