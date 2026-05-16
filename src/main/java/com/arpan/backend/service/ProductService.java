package com.arpan.backend.service;

import com.arpan.backend.dto.product.ProductRequest;
import com.arpan.backend.dto.product.ProductResponse;
import com.arpan.backend.dto.product.ProductSearchResponse;
import com.arpan.backend.entity.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    Page<ProductResponse> getAllProducts(Pageable pageable);

    ProductResponse getProductById(Long prodId);

    void deleteProduct(Long prodId);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    Products getProductEntity(Long id);

    Page<ProductSearchResponse> searchProducts(String keyword, Pageable pageable);
}
