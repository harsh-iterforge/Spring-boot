package com.example.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank
    private String name;

    @Positive
    private Double price;

    @Min(0)
    private Integer quantity;

    private Long categoryId;
}
