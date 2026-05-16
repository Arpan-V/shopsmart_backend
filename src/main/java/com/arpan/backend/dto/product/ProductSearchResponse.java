package com.arpan.backend.dto.product;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProductSearchResponse {

    private Long prodId;
    private String name;
    private String description;
    private String brand;
    private String category;
    private BigDecimal price;
    private int stockQuantity;
    private boolean productAvailable;
    private LocalDateTime releaseDate;
}