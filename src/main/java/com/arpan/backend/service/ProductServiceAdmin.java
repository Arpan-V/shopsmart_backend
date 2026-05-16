package com.arpan.backend.service;

import com.arpan.backend.dto.product.ProductResponseAdmin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductServiceAdmin {
    Page<ProductResponseAdmin> getAllProducts(Pageable pageable);

}
