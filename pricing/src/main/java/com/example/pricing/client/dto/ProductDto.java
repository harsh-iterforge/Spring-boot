package com.example.pricing.client.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private Double price;
    private Integer quantity;
    private String categoryName;
}
