// src/main/java/com/example/pricing/entity/Promotion.java
package com.example.pricing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.Set;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Promotion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    // types: PERCENTAGE, FIXED, BUY_X_GET_Y, TIERED
    private String type;

    // For percentage: e.g. 10.0 -> 10%
    @Column(name = "discount_value")
    private Double value;

    // For fixed: fixed amount off per unit or total
    private Double fixedAmount;

    // For buy X get Y: store as "buy" and "get"
    private Integer buyQty;
    private Integer getQty;

    // List of applicable product IDs/ categories / customer segments (in prod use normalized relation; here keep simple)
    @ElementCollection
    private Set<Long> applicableProductIds;

    @ElementCollection
    private Set<String> applicableCustomerSegments;

    private Instant startAt;
    private Instant endAt;

    private Boolean active = true;

    // coupon code (optional)
    private String couponCode;
}
