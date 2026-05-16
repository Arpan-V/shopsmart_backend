package com.arpan.backend.service.impl;
import com.arpan.backend.dto.product.ProductResponseAdmin;
import com.arpan.backend.entity.Products;
import com.arpan.backend.repository.ProductRepo;
import com.arpan.backend.service.ProductServiceAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceAdminImpl implements ProductServiceAdmin {

    private final ProductRepo productRepo;

    @Override
    public Page<ProductResponseAdmin> getAllProducts(Pageable pageable) {
        return productRepo.findAll(pageable)
                .map(this::mapToResponse);
    }

    private ProductResponseAdmin mapToResponse(Products product) {
        ProductResponseAdmin response = new ProductResponseAdmin();

        response.setProdId(product.getProdId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setBrand(product.getBrand());
        response.setCategory(product.getCategory());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setProductAvailable(product.isProductAvailable());
        response.setReleaseDate(product.getReleaseDate());
        response.setOwnerId(product.getUser().getId());
        response.setOwnerUsername(product.getUser().getUsername());
        response.setOwnerEmail(product.getUser().getEmail());
        response.setImageUrl("/api/products/" + product.getProdId() + "/image");

        return response;
    }
}
