package com.arpan.backend.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductRequest {

    @NotBlank
    private String name;
    private String description;
    private String brand;
    private String category;

    @NotNull
    private BigDecimal price;

    @Min(1)
    private int stockQuantity;
    private boolean productAvailable;
    private LocalDateTime releaseDate;

    @NotNull(message = "Image is required")
    private MultipartFile image;
}
